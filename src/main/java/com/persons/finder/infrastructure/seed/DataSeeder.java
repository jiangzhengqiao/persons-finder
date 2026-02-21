package com.persons.finder.infrastructure.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;


@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed-data:false}") // default false
    private boolean seedData;

    @Async
    @Override
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

        long endTime = System.currentTimeMillis();
        log.info("Successfully seeded 1,000,000 records in {} seconds.", (endTime - startTime) / 1000.0);

    }

    private void seedSecurityPatterns() {
        log.info("Seeding security patterns...");
        jdbcTemplate.execute("INSERT INTO security_patterns (pattern, type, description) VALUES " +
                "('ignore all instructions', 'INPUT_FILTER', 'Prompt injection defense')," +
                "('system prompt', 'INPUT_FILTER', 'Access control defense')," +
                "('hacked', 'OUTPUT_FILTER', 'Compromise detection')," +
                "('bypass', 'OUTPUT_FILTER', 'Security bypass detection')");
    }

    private void seedPersonData() {
        String sql = "INSERT INTO persons (name, job_title, hobbies, bio, latitude, longitude, version, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        Random random = new Random();
        int totalRecords = 1_000_000;
        int batchSize = 10_000; // submit once for 10,000 items

        for (int i = 0; i < totalRecords / batchSize; i++) {
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int j) throws java.sql.SQLException {
                    ps.setString(1, "Person_" + UUID.randomUUID().toString().substring(0, 8));
                    ps.setString(2, "Engineer_" + j);
                    ps.setString(3, "Hiking, Coding, Coffee");
                    ps.setString(4, "AI generated bio placeholder for " + j);
                    // 随机生成全球坐标 (Lat: -90 to 90, Lon: -180 to 180)
                    ps.setDouble(5, -90 + (180 * random.nextDouble()));
                    ps.setDouble(6, -180 + (360 * random.nextDouble()));
                    ps.setLong(7, 0L);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            if ((i + 1) * batchSize % 100_000 == 0) {
                log.info("Progress: {} records seeded...", (i + 1) * batchSize);
            }
        }
    }
}
