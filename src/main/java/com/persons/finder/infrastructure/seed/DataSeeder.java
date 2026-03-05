package com.persons.finder.infrastructure.seed;

import com.persons.finder.utils.GeoShardingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;


@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.seed-data:false}") // default false
    private boolean seedData;

    // 在类中定义常量
    private static final List<City> CITIES = List.of(
            // --- AUSTRALIA (AU) ---
            new City("Sydney", -33.8688, 151.2093, 545.0),
            new City("Melbourne", -37.8136, 144.9631, 535.0),
            new City("Brisbane", -27.4705, 153.0260, 271.0),
            new City("Perth", -31.9505, 115.8605, 214.0),
            new City("Adelaide", -34.9285, 138.6007, 139.0),
            new City("Gold Coast", -28.0167, 153.4000, 60.8),
            new City("Canberra", -35.2809, 149.1300, 38.1),
            new City("Newcastle", -32.9283, 151.7817, 32.2),
            new City("Hobart", -42.8821, 147.3272, 22.2),
            new City("Darwin", -12.4634, 130.8456, 13.2),

            // --- NEW ZEALAND (NZ) ---
            new City("Auckland", -36.8485, 174.7633, 147.0),
            new City("Christchurch", -43.5321, 172.6362, 38.3),
            new City("Wellington", -41.2865, 174.7762, 21.6),
            new City("Hamilton", -37.7870, 175.2793, 17.6),
            new City("Tauranga", -37.6878, 176.1651, 15.1),
            new City("Dunedin", -45.8788, 170.5028, 10.6),
            new City("Napier", -39.4928, 176.9120, 6.5)
    );

    private final double totalWeight = CITIES.stream().mapToDouble(City::weight).sum();

    //    @Async
    @Override
    @Async("seedExecutor")
    public void run(String... args) throws Exception {
        if (!seedData) {
            return;
        }

        // 1. check prevent repeated execution
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM persons", Integer.class);
        if (count != null && count > 0) {
            log.info("Database already seeded with {} records. Skipping seeder.", count);
            return;
        }

        log.info("Starting high-performance data seeding for 1,000,000 records...");
        long startTime = System.currentTimeMillis();

        // 2. init blacklist
        seedSecurityPatterns();

        // 3. write person data in batches
        seedTenMillionRecords();

        // 4.
        createSpatialIndex();

        long endTime = System.currentTimeMillis();
        log.info("Successfully seeded 1,000,000 records in {} seconds.", (endTime - startTime) / 1000.0);

    }

    private void seedSecurityPatterns() {
        log.info("Seeding security patterns...");
        jdbcTemplate.execute("INSERT INTO security_patterns (pattern, type, description) VALUES " +
                "('ignore all instructions', 'INPUT_FILTER', 'Prompt injection defense')," +
                "('system prompt', 'INPUT_FILTER', 'Access control defense')," +
                "('hacked', 'OUTPUT_FILTER', 'Compromise detection')," +
                "('bypass', 'OUTPUT_FILTER', 'Security bypass detection')" +
                "ON CONFLICT (pattern) DO NOTHING;");
    }

    public void seedTenMillionRecords() {
        log.info("Starting massive seed with Sharding: 10 million records...");

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        jdbcTemplate.execute("TRUNCATE TABLE persons RESTART IDENTITY");

        String sql = "INSERT INTO persons (name, job_title, hobbies, bio, location, version, created_at) " +
                "VALUES (?, ?, ?, ?, ST_SetSRID(ST_Point(?, ?), 4326), 0, CURRENT_TIMESTAMP)";

        Random random = new Random();
        int totalRecords = 10_000_000;
        int batchSize = 2500;

        // Redis shard buffer：Map<ShardKey, Map<MemberId, Point>>
        Map<String, Map<String, Point>> redisShardedBuffer = new HashMap<>();

        for (int i = 0; i < totalRecords / batchSize; i++) {
            List<Object[]> dbBatch = new ArrayList<>();
            long startId = (long) i * batchSize + 1;

            for (int j = 0; j < batchSize; j++) {
                City city = getRandomCity(random);
                double lat = city.lat() + (random.nextGaussian() * 0.15);
                double lon = city.lon() + (random.nextGaussian() * 0.15);
                String userId = String.valueOf(startId + j);

                // --- Database part ---
                dbBatch.add(new Object[]{
                        "User_" + userId, "Engineer", "Coding", "Hi from " + city.name(), lon, lat
                });

                // --- Redis sharding logic ---
                String shardKey = GeoShardingUtil.getShardKey(lon, lat);
                // If this bucket has not been created yet, initialize it
                redisShardedBuffer.computeIfAbsent(shardKey, k -> new HashMap<>())
                        .put(userId, new Point(lon, lat));
            }

            // 1. Perform database batch insertion
            jdbcTemplate.batchUpdate(sql, dbBatch);

            // 2. Check the Redis buffer, perform writing and clean up
            // For the sake of performance, we don’t need to clear all buckets every time it loops. We can process it every few batches, or according to the bucket size.
            if (i % 10 == 0) { // 每 10 个 DB batch (即 25,000 条数据) 处理一次 Redis 提交
                flushRedisShards(redisShardedBuffer);
            }

            if ((i + 1) * batchSize % 100_000 == 0) {
                log.info("Progress: {} / 10M ({}%)", (i + 1) * batchSize, ((i + 1) * batchSize * 100 / totalRecords));
            }
        }

        // flush all remaining data into Redis
        flushRedisShards(redisShardedBuffer);
        log.info("10 Million records with Sharding seeded successfully!");
    }

    // flush shard data in memory into Redis
    private void flushRedisShards(Map<String, Map<String, Point>> buffer) {
        if (buffer.isEmpty()) return;

        buffer.forEach((shardKey, points) -> {
            if (!points.isEmpty()) {
                redisTemplate.opsForGeo().add(shardKey, points);
            }
        });
        buffer.clear(); // 必须清空，否则内存会 OOM
    }

    private City getRandomCity(Random random) {
        double r = random.nextDouble() * totalWeight;
        double current = 0;
        for (City city : CITIES) {
            current += city.weight();
            if (r <= current) return city;
        }
        return CITIES.get(0);
    }

    private void createSpatialIndex() {
        try {
            //
            String indexName = "idx_persons_location_gist";
            String checkIndexSql = "SELECT indexname FROM pg_indexes WHERE indexname = ?";
            List<String> results = jdbcTemplate.queryForList(checkIndexSql, String.class, indexName);
            if (results.isEmpty()) {
                //
                String createIndexSql = "CREATE INDEX " + indexName + " ON persons USING GIST (location)";
                jdbcTemplate.execute(createIndexSql);
                log.info("Spatial index idx_location_point created successfully.");
            } else {
                log.info("Spatial index already exists, skipping creation.");
            }
        } catch (Exception e) {
            log.error("Failed to create spatial index", e);
        }
    }
}
