package com.persons.finder.performance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.ai.mock=true")
class NearbyPerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final List<TestResult> results = new ArrayList<>();

    // 固定查询参数
    private static final double LAT = -41.2865;
    private static final double LON = 174.7762;
    private static final double RADIUS = 100.0;
    private static final int PAGE_SIZE = 10;
    private static final String URL = "/api/v1/persons/nearby?lat=%f&lon=%f&radius=%f&page=0&size=%d";

    @Test
    void nearbyTest_10_threads() throws Exception {
        runTest(10, 1000);
    }

    @Test
    void nearbyTest_50_threads() throws Exception {
        runTest(50, 1000);
    }

    @Test
    void nearbyTest_100_threads() throws Exception {
        runTest(100, 1000);
    }

    private void runTest(int threadCount, int totalRequests) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger errors = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        String url = String.format(URL, LAT, LON, RADIUS, PAGE_SIZE);

        Instant start = Instant.now();

        try {
            for (int i = 0; i < totalRequests; i++) {
                executor.submit(() -> {
                    long begin = System.nanoTime();
                    try {
                        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            errors.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    } finally {
                        long duration = System.nanoTime() - begin;
                        responseTimes.add(duration);
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } finally {
            executor.shutdown();
        }

        long totalTimeMillis = Duration.between(start, Instant.now()).toMillis();
        analyze(threadCount, totalRequests, totalTimeMillis, responseTimes, errors.get());
    }

    private void analyze(int threadCount,
                         int totalRequests,
                         long totalTimeMillis,
                         List<Long> responseTimes,
                         int errors) {
        List<Long> sorted = responseTimes.stream().sorted().toList();
        int size = sorted.size();
        double avg = sorted.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
        double max = sorted.get(size - 1) / 1_000_000.0;
        double p95 = sorted.get(Math.min((int) (size * 0.95), size - 1)) / 1_000_000.0;
        double p99 = sorted.get(Math.min((int) (size * 0.99), size - 1)) / 1_000_000.0;
        double qps = totalRequests / (totalTimeMillis / 1000.0);
        double errorRate = (errors * 100.0) / totalRequests;

        results.add(new TestResult(threadCount, totalRequests, totalTimeMillis, avg, max, p95, p99, qps, errorRate));
    }

    @AfterAll
    static void generateReport() throws IOException {
        StringBuilder report = new StringBuilder("===== Nearby Performance Report =====\n\n");
        for (TestResult r : results) {
            report.append(r).append("\n\n");
        }
        try (FileWriter writer = new FileWriter("nearby-performance-report.txt")) {
            writer.write(report.toString());
        }
        System.out.println(report);
    }

    static class TestResult {
        int threads;
        int totalRequests;
        long totalTimeMillis;
        double avgResponseMs;
        double maxResponseMs;
        double p95Ms;
        double p99Ms;
        double qps;
        double errorRate;

        public TestResult(int threads, int totalRequests, long totalTimeMillis,
                          double avgResponseMs, double maxResponseMs,
                          double p95Ms, double p99Ms, double qps, double errorRate) {
            this.threads = threads;
            this.totalRequests = totalRequests;
            this.totalTimeMillis = totalTimeMillis;
            this.avgResponseMs = avgResponseMs;
            this.maxResponseMs = maxResponseMs;
            this.p95Ms = p95Ms;
            this.p99Ms = p99Ms;
            this.qps = qps;
            this.errorRate = errorRate;
        }

        @Override
        public String toString() {
            return """
                    Threads: %d
                    Requests: %d
                    TotalTime(ms): %d
                    QPS: %.2f
                    Avg(ms): %.2f
                    Max(ms): %.2f
                    P95(ms): %.2f
                    P99(ms): %.2f
                    ErrorRate: %.2f%%
                    """.formatted(threads, totalRequests, totalTimeMillis, qps, avgResponseMs, maxResponseMs, p95Ms, p99Ms, errorRate);
        }
    }
}