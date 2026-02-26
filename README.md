# Spatial-AI Person Finder

This is a Spring Boot project for managing person records with AI-generated bios and location-based search.

---

## Quick Start

### 1. Set AI API Key
This project requires an AI API Key. You can pass it as an environment variable named `APP_AI_API_KEY`.

**For Windows (PowerShell):**
```bash
$env:APP_AI_API_KEY="your_api_key_here"
```

**For Linux / macOS / Git Bash:**
```shell
export APP_AI_API_KEY="your_api_key_here"
```

### 2. Build and Run
Use Gradle to build and start the application.
```shell
# Build the project (skip tests for fast build)
./gradlew assemble -x test

# Run the application
java -jar build/libs/PersonsFinder-0.0.1-SNAPSHOT.jar
```

### 3. Testing
Our tests use Mocking, so they will NOT cost any money or AI credits.
```shell
./gradlew test
```

### 4. Run with Docker
You can also build and run the application inside a Docker container without installing any local JDK.
#### Build the Docker image
```shell
docker build -t persons-finder .
```

#### Run the container
```shell
docker run -p 8080:8080 -e APP_AI_API_KEY="your_api_key_here" persons-finder:latest
```
The application will be available at http://localhost:8080.

## API Endpoints

### 1. Create Person
**POST** `/api/v1/persons`
```json
{
  "name": "Alex",
  "jobTitle": "Engineer",
  "hobbies": "Coding, Hiking",
  "latitude": -36.8485,
  "longitude": 174.7633
}
```

### 2. Find Nearby People
**GET** /api/v1/persons/nearby?lat=-41.2865&lon=174.7762&radius=100&page=0&size=10
#### Response example:
```json
{
    "content": [
        {
            "id": 783740,
            "name": "Person_a05ea8e9",
            "jobTitle": "Engineer_3739",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 3739",
            "latitude": -41.13304868743817,
            "longitude": 174.8202869619625,
            "createdAt": "2026-02-20T15:17:16.694903"
        },
        {
            "id": 589200,
            "name": "Person_3aa0a65f",
            "jobTitle": "Engineer_9199",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 9199",
            "latitude": -41.48247584805092,
            "longitude": 174.83716475811735,
            "createdAt": "2026-02-20T15:17:12.698513"
        },
        {
            "id": 707149,
            "name": "Person_7e18368d",
            "jobTitle": "Engineer_7148",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 7148",
            "latitude": -41.458758392517666,
            "longitude": 174.6512795851774,
            "createdAt": "2026-02-20T15:17:15.233588"
        },
        {
            "id": 864970,
            "name": "Person_47b1562a",
            "jobTitle": "Engineer_4969",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 4969",
            "latitude": -41.38807089055353,
            "longitude": 174.97288886292012,
            "createdAt": "2026-02-20T15:17:18.201015"
        },
        {
            "id": 662124,
            "name": "Person_3cc18888",
            "jobTitle": "Engineer_2123",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 2123",
            "latitude": -41.05734074461198,
            "longitude": 174.75906357435133,
            "createdAt": "2026-02-20T15:17:14.339141"
        },
        {
            "id": 382952,
            "name": "Person_ae38f232",
            "jobTitle": "Engineer_2951",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 2951",
            "latitude": -41.34030741857188,
            "longitude": 175.08018065374387,
            "createdAt": "2026-02-20T15:17:08.581791"
        },
        {
            "id": 771037,
            "name": "Person_10bb6cc3",
            "jobTitle": "Engineer_1036",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 1036",
            "latitude": -41.042557705356224,
            "longitude": 174.97638554011144,
            "createdAt": "2026-02-20T15:17:16.452691"
        },
        {
            "id": 173846,
            "name": "Person_d4318a07",
            "jobTitle": "Engineer_3845",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 3845",
            "latitude": -41.40735726350256,
            "longitude": 174.45915934878997,
            "createdAt": "2026-02-20T15:17:03.453536"
        },
        {
            "id": 499130,
            "name": "Person_f52f5ce3",
            "jobTitle": "Engineer_9129",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 9129",
            "latitude": -41.472322466381804,
            "longitude": 175.1090630583904,
            "createdAt": "2026-02-20T15:17:10.838043"
        },
        {
            "id": 857988,
            "name": "Person_70c46522",
            "jobTitle": "Engineer_7987",
            "hobbies": "Hiking, Coding, Coffee",
            "bio": "AI generated bio placeholder for 7987",
            "latitude": -40.9481489841852,
            "longitude": 174.53384470462674,
            "createdAt": "2026-02-20T15:17:18.063125"
        }
    ],
    "pageable": {
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "pageNumber": 0,
        "pageSize": 10,
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": false,
    "totalElements": 45,
    "totalPages": 5,
    "first": true,
    "numberOfElements": 10,
    "size": 10,
    "number": 0,
    "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
    },
    "empty": false
}
```


### 3. Update Location
**PUT** /api/v1/persons/1/location
```json
{
  "latitude": -41.2865,
  "longitude": 174.7762
}
```

## 5. Tech Stack
- Java 17 & Spring Boot 2.x
- H2 Database (In-memory)
- Spring Data JPA (Spatial query with Bounding Box)
- Docker (Containerization)
- Mockito (For cost-free AI testing)

## Performance Benchmarking Report

### 1. Test Environment
   - Hardware: Apple M1 (8 Cores, 8GB RAM)
   - OS: macOS 26.3
   - Runtime: OpenJDK 17 (JVM max heap 2GB)
   - Data Volume: 1,000,000 (1M) Person Records
   - Service Deployment: All services (Redis, PostGIS, App) running natively on localhost to eliminate network jitter.

### 2. Comparative Analysis (1M Geo-Spatial Records)
The following metrics were captured using the ./run_bench.sh script (powered by wrk).

| Engine           | Metrics     | 10 Concurrency  | 50 Concurrency  | 100 Concurrency |
|------------------|-------------|-----------------|-----------------|-----------------|
| Redis GEO        | RPS         | 6,858.11        | 7,275.95        | 7,214.06        | 
| (Primary Cache)  | P99 Latency | 743.23 ms*      | 13.78 ms        | 324.23 ms       | 
| H2 (In-Memory)   | RPS         | 1,881.55        | 2,069.34        | 1,973.24        | 
| (Development)    | P99 Latency | 132.34 ms       | 198.66 ms       | 413.92 ms       | 
| PostGIS (Native) | RPS         | 1,627.74        | 1,337.70        | 1,362.82        | 
| (DB Fallback)    | P99 Latency | 1,210.00 ms*    | 202.31 ms       | 301.40 ms       | 

**Note:** Higher latencies at low concurrency are due to initial JIT compilation and connection pool warm-up.

### 3. How to Reproduce (Benchmark Execution)

#### Prerequisites
   - wrk: Must be installed on your Mac (brew install wrk)
   - Services: Ensure Redis and PostGIS are accessible (or use the provided docker-compose.yml).

#### Execution
1. Ensure the application is running on port 8080.
2. Run the automated benchmark script:
```shell
    chmod +x ./run_bench.sh
    ./run_bench.sh
```

#### Inside the Script (run_bench.sh)
The script executes wrk commands with the following structure:
```shell
    # Example command used in the script
    wrk -t12 -c100 -d30s "http://localhost:8080/persons/nearby?lat=-41.1923&lon=174.6139&radiusKm=10"
```
- -t12: 12 threads to utilize Apple M1 cores.
- -c100: Number of concurrent HTTP connections.
- -d30s: Duration of each test stage.

### 4. Architectural Summary
- High Availability: The fallback mechanism from Redis to PostGIS ensures system uptime. While PostGIS throughput is ~80% lower than Redis, it remains capable of handling >1.3k RPS on commodity hardware (Apple M1).

- Developer Productivity: H2 is utilized for local development and CI/CD pipelines, offering a balanced performance profile without requiring external infrastructure.