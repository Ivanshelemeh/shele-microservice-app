# CLAUDE.md

## Project Overview

Микросервисная платформа на Java 25 + Spring Boot 3.3. Сервисы общаются через REST (синхронно) и Kafka (асинхронно).

## Tech Stack

- **Java**: 25 (используем virtual threads, records, sealed interfaces, pattern matching)
- **Spring Boot**: 3.3.x
- **Spring Data JPA**: Hibernate 6, PostgreSQL, MYSQL, Redis
- **Kafka**: spring-kafka, Confluent Schema Registry, Avro
- **Build**: Maven multi-module
- **Containers**: Docker
- **CI/CD**: GitHub Actions
- **Monitoring**: Micrometer + Prometheus + Grafana, Loki (logs), Tempo (traces)



## Code Conventions

### General
- Java 25 features: records for DTOs, sealed interfaces for domain types, pattern matching in switch
- Virtual threads enabled: `spring.threads.virtual.enabled=true`
- Constructor injection everywhere — NO field injection, NO @Autowired on fields
- Lombok: @Slf4j, @RequiredArgsConstructor, @Builder only. NO @Data (use records instead)
- Package structure per module: `config/`, `entities/`, `repositories/`, `service/`,  `dto/`, `integration/`, `model/`, `data/`,`api/`, `exception/`, `utils/`, `aspects/`, `web/`
- @Transactional  not in http request 

### Naming
- Services: `BikeOrderService`, `BikeCustomerService`
- Kafka consumers: `PaymentEventConsumer`
- Kafka producers: `BookingEventProducer`
- DTOs: records with suffix `Request`, `Response`, `Event`, `Command`
- Entities: no suffix, singular noun (`Booking`, `Payment`, `User`)
- Repositories: `BookingRepository extends JpaRepository<Booking, UUID>`
- Configs: `KafkaConfig`, `AiConfig`

### REST API
- Endpoints: `/api/v1/{resource}` (plural nouns)
- Use records for request/response: `record CreateBookingRequest(@NotBlank String userId, ...)`
- Always `@Valid` on @RequestBody
- Return `ResponseEntity<T>` with proper HTTP status codes
- Global exception handler via `@RestControllerAdvice`

### Database / Spring Data JPA
- Entity IDs: Long with `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Always use `@CreationTimestamp`, `@UpdateTimestamp`
- Named queries or `@Query` with JPQL — no native SQL unless performance-critical
- N+1 prevention:  `JOIN FETCH` , `@BathcSize`
- Migrations:  Flyway
- Testcontainers for integration tests against real PostgreSQL

### Kafka
- Consumer group: `{service-name}-group`
- Exactly-once: idempotent producer + transactional outbox pattern for critical events
- Error handling: retry 3x → dead letter topic (`{topic}.dlt`)
- Consumer config: manual ack, concurrency = number of partitions
- Producer: always async with callback logging

### Error Handling
- Custom exceptions extend `RuntimeException`: `BikeCustomerNotFoundException`, `BikeOrderFailedException`
- `@RestControllerAdvice` with `@ExceptionHandler` for REST
- Kafka: log + send to DLT, never swallow exceptions silently

### Logging
- SLF4J via `@Slf4j`
- Structured logging with MDC: `traceId`, `userId`, `bookingId`
- Log levels: ERROR (incidents), WARN (recoverable), INFO (business events), DEBUG (dev only)
- Never log sensitive data (passwords, tokens, card numbers)
- Format: JSON in prod, plain text in local

### Testing
- Unit tests: JUnit 5 + Mockito. Test naming: `should_createBooking_when_validRequest()`
- Integration tests: `@SpringBootTest` + Testcontainers (PostgreSQL, Kafka)
- Test coverage target: 80%+ on service layer
- No `@SpringBootTest` for unit tests — use `@ExtendWith(MockitoExtension.class)`

### Resilience
- Resilience4j for circuit breaker, retry, rate limiter
- Circuit breaker on all external calls (partner APIs, LLM, payment providers)
- Timeout:  10s for external HTTP, 30s for LLM
- HikariCP: `leak-detection-threshold: 30000`, `maximum-pool-size: 20`
- Health checks: `/actuator/health` with liveness + readiness probes

### Security
- NEVER concatenate strings for SQL — always use parameterized queries
- NEVER log or expose API keys, tokens, passwords
- Input validation on all endpoints via `@Valid` + Jakarta Validation
- Rate limiting on public endpoints

## Configuration Profiles

- `local` — docker-compose PostgreSQL/Kafka/Redis on localhost
- `dev` — shared dev environment in K8s
- `staging` — pre-prod with production-like data
- `prod` — production, all security enabled

## Common Patterns

### Transactional Outbox (Kafka reliability)
```java
@Transactional
public Booking createBooking(CreateBookingCommand cmd) {
    Booking booking = bookingRepository.save(mapToEntity(cmd));
    outboxRepository.save(new OutboxEvent("booking.created.v1", booking.getId(), serialize(booking)));
    return booking;
}
// Separate scheduler polls outbox → publishes to Kafka → marks as sent
```

### Kafka Consumer
```java
@KafkaListener(topics = "payment.completed.v1", groupId = "${spring.kafka.consumer.group-id}")
public void handle(PaymentCompletedEvent event, Acknowledgment ack) {
    try {
        bookingService.confirmPayment(event.bookingId());
        ack.acknowledge();
    } catch (Exception e) {
        log.error("Failed to process payment event: {}", event.bookingId(), e);
        throw e; // → retry → DLT
    }
}
```

## Important Notes
- PR reviews обязательны. Минимум 1 approve.
- Каждый сервис деплоится независимо через свой Helm chart.
- Kafka schema evolution: BACKWARD compatibility в Schema Registry.
- При создании нового сервиса — копировать структуру из `bike-custom-service` как шаблон.