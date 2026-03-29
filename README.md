# Wallet - Crypto Portfolio Manager

A robust Spring Boot solution for managing cryptocurrency portfolios, integrated in real-time with the CoinCap API. This project was developed focusing on scalability, financial precision, and modern architectural best practices.

---

## Getting Started

### Prerequisites

- Java 21+
- Docker and Docker Compose

### Quick Run

The simplest way to spin up the complete environment (Application + Postgres + Redis):

```bash
docker compose -f docker/compose.yaml up -d
```

If you prefer to run the application locally (with only database and cache in Docker):

```bash
docker compose -f docker/compose-local.yaml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Spring profiles are defined in two different yaml files:

```
src/main/resources/
├── application.yaml          # To be used on production environments
└── application-local.yaml    # Overrides the previous file for local development (profile: local)
```

- Access: http://localhost:8080
- Swagger Docs: http://localhost:8080/swagger-ui.html
- Postman Collection: A Postman collection has been provided in the repository to facilitate testing and API exploration during development.

---

## Project Structure

The project follows a modular structure organized by responsibilities:

```
src/main/java/com/example/wallet/
├── config/           # Cache, Redis, CoinCap config
├── controller/       # REST endpoints
├── service/          # Business logic
├── repository/       # JPA repositories
├── entity/           # Database entities
├── mapper/           # DTO mappers
├── exception/        # Error handling
└── coincap/          # CoinCap integration
```

---

## Architectural Decisions and Trade-offs

To my limited knowledge in financial systems, every decision impacts data reliability. Below, I detail the technical paths I chose:

### 1. Data Precision and Performance

- BigDecimal over Double: To avoid floating-point rounding errors, I used BigDecimal. In finance, precision is non-negotiable, even with a few slight computational overhead.
- SQL-driven Aggregation: I opted to perform sum and grouping calculations directly in PostgreSQL via JPQL/SQL. Processing thousands of assets in Java memory (Heap) would be inefficient; delegating this to the database reduces network traffic and leverages indexing.
- Strategic Pagination: I implemented pagination for price history to handle potentially long lists in the response. This approach prevents performance bottlenecks and facilitates navigation for future front-end implementations, ensuring a smoother user experience.

### 2. Scalability and Resilience

- Distributed Caching with Redis: Instead of a local cache, I used Redis with a 5-minute TTL. This allows the application to scale horizontally across multiple instances while maintaining price consistency.
- Asynchronous Processing: External API updates (CoinCap) run in the background. I configured a dedicated Thread Pool (Core: 3, Max: 3, Queue: 500) so that slow external calls do not block user requests.
- Read-Only Transactions: I applied @Transactional(readOnly = true) to query methods. Beyond being a security best practice, it allows the persistence provider to optimize performance.

### 3. Scope and Software Design

- Infrastructure and Docker Strategy: I provided two Docker Compose configurations, both located in the `docker/` folder. The `docker/compose-local.yaml` is used to spin up only the infrastructure (Postgres/Redis) for local development. Additionally, `docker/compose.yaml` is provided to run the entire stack (including the application container), allowing the project to be executed in a fully isolated environment without requiring a local IDE or JDK installation. Docker Compose support was explicitly disabled in the application.yaml, assuming that in a production-ready environment, these services would be managed externally.
- Database Schema Management: For the scope of this challenge, I relied on Hibernate's ddl-auto property for automatic schema generation. In a production-grade environment, I would replace this with a database migration tool like Flyway or Liquibase to ensure versioned, reproducible, and safe schema evolutions across different environments.
- Configuration Strategy (@Value vs @ConfigurationProperties): For this challenge, I used @Value to inject environment variables and properties due to its straightforward implementation. In a long-term production project, I would favor @ConfigurationProperties classes to provide type-safety, validation (JSR-303), and better grouping of related settings.
- AOP for Centralized Logging: Implemented Aspect-Oriented Programming (AOP) to handle cross-cutting logging concerns. This ensures that execution time, method entries, and exits are logged consistently across the service layer without cluttering the business logic.
- Selective CRUD Implementation: Instead of implementing a full suite of generic CRUD operations for every entity, I deliberately focused on the core valuation and performance features.
- Java Records: Used for DTOs to ensure data immutability and cleaner code, focusing strictly on data transfer without the boilerplate of getters and setters.
- Global Error Handling: I used @RestControllerAdvice to centralize exception logic. This ensures the API always returns a standardized JSON format, improving the integration experience for Frontend and Mobile clients.
- PostgreSQL: Chosen for its robustness in complex transactional operations and superior support for financial data types compared to MySQL or H2.

---

## Testing Strategy

The focus was on validating the application core and critical architectural flows:

- Valuation Service: Unit tests for wallet performance calculations and historical value tracking.
- Scheduler and Async: Integration tests for the scheduled price refresh. Validation of thread pool behavior and the price refresh orchestrator.
- Persistence: Unit tests on wallet valuation service for custom aggregation queries.

Note: I prioritized delivering advanced features and architecture patterns over 100% unit test coverage, focusing on areas I considered important and with the highest business risk.

---

## Useful Commands

Run Tests:
./mvnw test

Check Logs:
docker compose -f docker/compose.yaml logs -f wallet-app

Stop Services:
docker compose -f docker/compose.yaml down
