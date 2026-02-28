package com.persons.finder.infrastructure.seed;

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
    private static final String GEO_KEY = "person_locations";

    @Value("${app.seed-data:false}") // default false
    private boolean seedData;

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
        seedPersonData();

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

    public void seedPersonData() {
        redisTemplate.delete(GEO_KEY);
        jdbcTemplate.execute("TRUNCATE TABLE persons RESTART IDENTITY");

        // id(1), name(2), job_title(3), hobbies(4), bio(5), lon(6), lat(7), version(8)
        String sql = "INSERT INTO persons (id, name, job_title, hobbies, bio, location, version, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ST_SetSRID(ST_Point(?, ?), 4326), ?, CURRENT_TIMESTAMP)";

        Random random = new Random();
        int totalRecords = 1_000_000;
        int batchSize = 1000;

        log.info("Starting seed 1 million records...");

        for (int i = 0; i < totalRecords / batchSize; i++) {
            List<Object[]> dbBatch = new ArrayList<>();
            Map<String, Point> redisBatch = new HashMap<>();

            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT nextval('persons_id_seq') FROM generate_series(1, " + batchSize + ")", Long.class);

            for (int j = 0; j < batchSize; j++) {
                Long id = ids.get(j);

                double lon = -180 + (360 * random.nextDouble());

                double lat = -85 + (170 * random.nextDouble());

                dbBatch.add(new Object[]{
                        id, "Person_" + id, "Engineer_" + j, "Hiking, Coding", "Bio", lon, lat, 0L
                });

                redisBatch.put(id.toString(), new Point(lon, lat));
            }

            // batch
            jdbcTemplate.batchUpdate(sql, dbBatch);
            redisTemplate.opsForGeo().add(GEO_KEY, redisBatch);

            if ((i + 1) * batchSize % 100_000 == 0) {
                log.info("Progress: {}/{} records seeded...", (i + 1) * batchSize, totalRecords);
            }
        }
        log.info("Seeding completed!");
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
