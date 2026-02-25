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

## 📊 Performance

We benchmarked the `/nearby` endpoint with **1 million records** in the database. Each test performed 1000 requests. Below are the results under different concurrency levels:

| Threads | Total Time (ms) | QPS     | Avg (ms) | Max (ms) | P95 (ms) | P99 (ms) | Error Rate |
|---------|-----------------|---------|----------|----------|----------|----------|------------|
| 10      | 663             | 1508.30 | 6.57     | 85.95    | 12.39    | 20.36    | 0.00%      |
| 50      | 952             | 1050.42 | 43.13    | 217.73   | 121.11   | 180.30   | 0.00%      |
| 100     | 1968            | 508.13  | 194.13   | 877.72   | 683.67   | 747.59   | 0.00%      |

**Performance analysis with Arthas:**  
Using the `trace` command, we identified the main bottleneck. The trace below shows a typical request (18ms total):


```shell
---ts=2026-02-26 00:32:53.434;thread_name=http-nio-8080-exec-3;id=126;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@34b87182
    `---[18.059667ms] com.persons.finder.application.PersonService$$EnhancerBySpringCGLIB$$6c99b310:findNearby()
        `---[99.23% 17.92125ms ] org.springframework.cglib.proxy.MethodInterceptor:intercept()
            `---[83.92% 15.039541ms ] com.persons.finder.application.PersonService:findNearby()
                +---[0.03% 0.004667ms ] org.springframework.data.domain.Pageable:getPageNumber() #33
                +---[14.65% 2.202625ms ] org.slf4j.Logger:info() #33
                +---[0.53% 0.080125ms ] com.persons.finder.infrastructure.util.GeoUtils:calculateBoundingBox() #35
                +---[1.18% 0.176792ms ] org.springframework.data.domain.Pageable:getPageNumber() #37
                +---[0.06% 0.008916ms ] org.springframework.data.domain.Pageable:getPageSize() #37
                +---[0.07% 0.011125ms ] org.springframework.data.domain.PageRequest:of() #37
                +---[0.05% 0.007166ms ] com.persons.finder.infrastructure.util.GeoUtils$BoundingBox:minLat() #39
                +---[0.04% 0.006625ms ] com.persons.finder.infrastructure.util.GeoUtils$BoundingBox:maxLat() #39
                +---[0.04% 0.00625ms ] com.persons.finder.infrastructure.util.GeoUtils$BoundingBox:minLon() #39
                +---[0.84% 0.126708ms ] com.persons.finder.infrastructure.util.GeoUtils$BoundingBox:maxLon() #39
                +---[79.20% 11.911375ms ] com.persons.finder.domain.repository.PersonRepository:findNearbyEfficiently() #38
                +---[0.97% 0.146209ms ] org.springframework.data.domain.Slice:map() #40
                +---[0.02% 0.002584ms ] org.springframework.data.domain.Slice:getNumberOfElements() #42
                `---[0.91% 0.1365ms ] org.slf4j.Logger:debug() #42
```
**The database query (`findNearbyEfficiently`) takes ~79% of the total time**, even with the bounding‑box pre‑filter. The rest is mainly logging and mapping.

**Planned optimization:**  
For production, we would replace the manual Haversine calculation with **PostGIS** native spatial indexes. Using `ST_DWithin` and `ST_DistanceSphere` would reduce the query time to single‑digit milliseconds, especially under high concurrency.
