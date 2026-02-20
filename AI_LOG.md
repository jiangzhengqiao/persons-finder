# AI_LOG.md

This document records my collaboration with AI during the development of the Persons Finder project. I used AI as a co‑pilot to solve technical challenges, optimize performance, and improve code quality.

---

## Interaction 1: Fixing DataSeeder OOM in Tests

**My question:**  
I have a `DataSeeder` class that inserts 1 million records at startup. When I run multiple `@SpringBootTest` classes, each test starts a new Spring context and the seeder runs repeatedly, causing memory issues. How can I prevent this?

**AI's suggestion:**  
Use `@ActiveProfiles("test")` on all test classes and add `@Profile("!test")` to the `DataSeeder`. This way the seeder is only active in non‑test profiles. Also consider using `@TestPropertySource` to disable the seeder via a property.

**My adjustments:**  
I applied `@ActiveProfiles("test")` to every test class and kept the existing `@Profile("!test")` on `DataSeeder`. Additionally, I added a check in the seeder to skip execution if the table already contains data, which also helps when the same context is reused.

---

## Interaction 2: Optimizing the `/nearby` Query with Bounding Box

**My question:**  
I need to implement a “find people nearby” endpoint that works fast even with millions of records. The Haversine formula alone would require a full table scan. How can I make it efficient in H2?

**AI's advice:**  
First, create an index on `(latitude, longitude)`. Then use a **bounding box** pre‑filter: calculate the min/max latitude and longitude based on the desired radius and center point. Apply the exact Haversine formula only to points inside that box. This way the database uses the index to eliminate most rows before the heavy calculation.

**My adjustments:**  
I added a `GeoUtils.calculateBoundingBox` method and wrote a custom JPQL query in `PersonRepository` that uses the bounding box in the `WHERE` clause. I also added the `@Index` annotation on the entity. The query now runs in milliseconds even with 1 million records. I later confirmed the performance by testing with the seeded data.

---

These interactions show how AI helped me quickly prototype, optimize, and debug the application. In each case, I critically reviewed the AI’s suggestions and adapted them to fit the project’s needs.