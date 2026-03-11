# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Микросервисная платформа на Java + Spring Boot для управления заказами велосипедов (bike customer order management). Сервисы общаются через REST (синхронно), Kafka (асинхронно) и обнаруживаются через Eureka.

## Architecture

```
Client → SpringApiGetaway (8443, Gateway + JWT + Rate Limit)
           │
           ├─► Bikemicroservices (8013, MySQL) — core business service
           │     ├─ REST: /rest/api/v1/customers/**, /rest/api/v1/order/**
           │     ├─ Kafka producer/consumer (bike-order, recommendation-events)
           │     └─ Saga orchestrator (BikeOrderSaga)
           │
           ├─► ServiceRecommendation (8013, MongoDB, WebFlux) — reactive recommendations
           │     └─ REST: /rest/api/v1/recommendation/**
           │
           └─► composite-service (7001, WebFlux, Gradle) — aggregator pattern
                 └─ Aggregates customer + order + recommendation data
```

**Infrastructure services:**
- **BikeCustomerDiscoveryService** (8010) — Eureka server
- **MainConfigServer** (8012) — Spring Cloud Config (Git + native), RabbitMQ bus
- **config-repo** — centralized config files served by config server

## Tech Stack (Current vs Target)

| Aspect | Current State | Target (arc-rule.md) |
|--------|--------------|---------------------|
| Java | 11–17 (mixed across services) | 25 (virtual threads, records, sealed interfaces) |
| Spring Boot | 2.6.1–2.7.6 (most services), 3.5.7 (composite) | 3.3.x |
| Build | Maven (most), Gradle (composite-service) | Maven multi-module |
| DB | MySQL (Bike), MongoDB (Recommendation), Redis (cache/sessions) | PostgreSQL, MySQL, Redis |
| Messaging | Kafka + RabbitMQ (config bus) | Kafka + Confluent Schema Registry + Avro |

## Build & Run Commands

### Bikemicroservices (Maven, core service)
```bash
cd Bikemicroservices/IdeaProjects
mvn clean install                    # build
mvn test                             # all tests
mvn test -Dtest=ClassName            # single test class
mvn test -Dtest=ClassName#methodName # single test method
mvn spring-boot:run                  # run locally
```

### composite-service (Gradle)
```bash
cd composite-service
./gradlew build                      # build
./gradlew test                       # all tests
./gradlew bootRun                    # run locally
```

### Other services (Maven)
```bash
cd <ServiceDirectory>
mvn clean install && mvn spring-boot:run
```

### Infrastructure (Docker)
```bash
cd shele-mirroservice-deploy/docker
docker-compose up -d                 # MySQL, MongoDB, Redis, Kafka, Zookeeper, Schema Registry, RabbitMQ, Keycloak
```

**Docker ports:** MySQL 3307, MongoDB 27017, Redis 6379, Kafka 9092, Schema Registry 8084, RabbitMQ 5673/15673, Keycloak 8080, Zookeeper 32181

## Code Conventions (from arc-rule.md)

### Mandatory Rules
- **Constructor injection everywhere** — NO field injection, NO `@Autowired` on fields
- **Lombok limited to:** `@Slf4j`, `@RequiredArgsConstructor`, `@Builder`. NO `@Data` (use records)
- **DTOs:** records with suffix `Request`, `Response`, `Event`, `Command`
- **Entities:** no suffix, singular noun; ID type `Long` with `IDENTITY` strategy
- **`@Transactional` NOT in HTTP request layer** (controllers)
- Always `@Valid` on `@RequestBody`; return `ResponseEntity<T>`
- Endpoints: `/api/v1/{resource}` (plural nouns)
- Parameterized queries only — NEVER string concatenation for SQL
- No native SQL unless performance-critical; use JPQL with `JOIN FETCH` / `@BatchSize`

### Naming
- Services: `BikeOrderService`, Consumers: `PaymentEventConsumer`, Producers: `BookingEventProducer`
- Repositories: `BookingRepository extends JpaRepository<Booking, UUID>`
- Configs: `KafkaConfig`, `AiConfig`
- Exceptions: extend `RuntimeException` — `BikeCustomerNotFoundException`, `BikeOrderFailedException`
- Test methods: `should_createBooking_when_validRequest()`

### Package Structure per Module
`config/`, `entities/`, `repositories/`, `service/`, `dto/`, `integration/`, `model/`, `data/`, `api/`, `exception/`, `utils/`, `aspects/`, `web/`

### Kafka Conventions
- Consumer group: `{service-name}-group`
- Idempotent producer + transactional outbox for critical events
- Retry 3x → dead letter topic (`{topic}.dlt`)
- Manual ack, concurrency = number of partitions
- Producer: always async with callback logging

### Resilience
- Resilience4j circuit breaker on all external calls
- Timeout: 10s external HTTP, 30s LLM
- Global exception handler via `@RestControllerAdvice`

## Key Kafka Topics
- `bike-order` — order events (3 partitions, 3 replicas)
- `recommendation-events` — recommendation updates
- `recommendations-fail-events` — failed recommendation processing

## Saga Pattern
`BikeOrderSaga` (in Bikemicroservices) orchestrates order processing:
1. Listens to `recommendation-events`
2. Processes `RecommendationUpdateCommand` via `BikeOrderRecommendationProcess`
3. On success → publishes to `bike-order`
4. On failure → publishes `OrderRecUpdateFailCommand` to `recommendations-fail-events`

## Database Migrations
Bikemicroservices uses **Flyway** (V1.0–V1.14). Tables: `bike_customer`, `bike_order` (with order items), `customer_transaction`.

## Testing
- **Unit tests:** JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)` — no `@SpringBootTest`
- **Integration tests:** `@SpringBootTest` + Testcontainers (PostgreSQL, Kafka)
- **Bikemicroservices tests** use H2 in-memory DB with parallel execution (fixed parallelism: 2)
- Coverage target: 80%+ on service layer

## CI/CD
GitHub Actions workflow in `Bikemicroservices/.github/workflows/commit-stage.yml`:
- Triggers on push to `master`/`main` + weekly schedule
- Build with Java 11/Maven → package Docker image → publish to Docker Hub (`Ivanshelemeh/iashele/Bikemicroservises`)

## Configuration Profiles
- `dev` — shared dev environment

## Logging
- SLF4J via `@Slf4j`, structured logging with MDC (`traceId`, `userId`, `bookingId`)
- JSON in prod, plain text in local
- Never log sensitive data

## Important Notes
- When creating a new service, copy structure from `bike-custom-service` as template
- Kafka schema evolution: BACKWARD compatibility in Schema Registry
- Each service deploys independently
- PR reviews mandatory (min 1 approve)
