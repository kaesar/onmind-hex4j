# Hex4j - Hexagonal Architecture - Java & Spring Boot

This project implements a hexagonal architecture approach using Java 25, Spring Boot Framework and Gradle, with ports and use cases located in the application layer.

## Requirements

- **Java 25** (`sdk use java 25.0.2-graalce`) — required, not just recommended
- **Gradle 9+** (the wrapper pins 9.7.1. Gradle ≥ 9.1 is needed to run the daemon on Java 25)

## Architecture

### Hexagonal Architecture Variation

This implementation presents a variation of traditional hexagonal architecture:

- **Domain**: Contains the model, the domain service with business rules, and domain exceptions
- **Application**: Acts as orchestrator with use cases and mappers, defining input/output ports to access the domain service and infrastructure adapters
- **Infrastructure**: Implements adapters that connect with external systems

### Project Structure

```
  ________________
./ co.onmind.hex /
├── domain/
│   ├── models/          # Domain models (Role, ScriptResult, ScriptWhitelist, StoreItem)
│   ├── services/        # Domain services (RoleService)
│   └── exceptions/      # Domain exceptions
├── application/
│   ├── dto/             # DTOs for requests/responses (in/, out/)
│   ├── mappers/         # Mappers between domain and DTOs
│   ├── ports/           # Ports/Interfaces (RoleRepositoryPort, ScriptingPort, AbcPort, ...)
│   │   ├── in/          # Input ports (CreateRoleTrait, GetRoleTrait, ExecuteScriptTrait, ...)
│   │   └── out/         # Output ports (RoleRepositoryPort, ScriptingPort, StorePort, ...)
│   └── usecases/        # Application use cases (RoleUseCase, ScriptingUseCase, ...)
├── infrastructure/
│   ├── controllers/     # REST controllers or end-points (RoleController)
│   ├── persistence/     # Database persistence adapters
│   ├── notification/    # Notification adapters
│   ├── scripts/         # Scripting adapters (QuickJS)
│   ├── storage/         # Object storage adapters (S3)
│   ├── events/          # Event adapters (Kafka, RabbitMQ, SQS, SNS, EventBridge)
│   ├── webclients/      # Web clients for external services (XDB)
│   └── configuration/   # Spring configurations
└── transverse/          # Cross-cutting concerns
```

## Technologies

- **Java 25**: Modern programming language (virtual threads, JEP 491 un-pinning, scoped values)
- **Spring Boot 4.1.1**: Lightweight web framework on virtual threads
- **Gradle**: Dependency manager and build tool
- **H2 Database**: In-memory database for development
- **JUnit 5**: Modern testing framework
- **Bean Validation**: Data validation framework

## Features

- **Hexagonal architecture** with ports in application
- **Clear separation of responsibilities** by layers
- **Dependency injection** with Spring Boot
- **Data validation** with Bean Validation
- **Enhanced HTTP error handling**
- **H2 database** with automatic initialization
- **Structured logging** for monitoring
- **Sandboxed scripting** with QuickJS (Java 25)
- **Virtual threads** with per-downstream circuit breaker, bulkhead and rate limiter
- **Unit and integration tests** (JUnit 5 + Mockito)

## Getting Started

### Running the Application

```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080`.

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

Coverage report:

```bash
./gradlew test jacocoTestReport
```

### H2 Console

Enabled in the `dev` profile, at `http://localhost:8080/h2-console`:

- JDBC URL: `jdbc:h2:mem:hex4j`
- Username: `sa`
- Password: _(empty)_

## API Endpoints

### Roles
- `POST /api/v1/roles` - Create role
- `GET /api/v1/roles` - Get all roles
- `GET /api/v1/roles/{id}` - Get role by ID
- `PUT /api/v1/roles/{id}` - Update role
- `DELETE /api/v1/roles/{id}` - Delete role
- `GET /api/v1/roles/search?name={pattern}` - Search roles by pattern
- `GET /api/v1/roles/count` - Count roles

### Scripts
- `POST /api/v1/script/execute` - Execute whitelisted script

### XDB
- `GET /api/v1/xdb/sheet` - Get XDB sheet

### Store
- `GET /api/v1/store/items?bucket={name}` - List store items

## Usage Examples

### Create a role
```bash
curl -X POST http://localhost:8080/api/v1/roles \
  -H "Content-Type: application/json" \
  -d '{"name": "DEVELOPER"}'
```

### Get all roles
```bash
curl http://localhost:8080/api/v1/roles
```

### Search roles
```bash
curl "http://localhost:8080/api/v1/roles/search?name=ADMIN"
```

## Design Principles

1. **Dependency Inversion**: Domain doesn't depend on infrastructure
2. **Separation of Concerns**: Each layer has a specific responsibility
3. **Testability**: Easy testing through port mocking
4. **Flexibility**: Easy swapping of infrastructure adapters
5. **Maintainability**: Clean and well-structured code
6. **Robustness**: Enhanced error handling with proper exceptions
7. **Scalability**: Architecture ready for microservices

## Concurrency

The application is **blocking end to end** — Spring MVC, JPA/Hibernate, synchronous Redis and
synchronous AWS SDK clients — but it runs on **virtual threads**
(`spring.threads.virtual.enabled=true`). Every request gets its own virtual thread instead of one
of Tomcat's 200 platform workers, so blocking I/O no longer consumes a worker.

Because the web server no longer caps concurrency, each outbound dependency has an explicit
limit: a circuit breaker, a bulkhead and a rate limiter composed per downstream, plus a
visibility timeout for SQS that outlasts the maximum script execution time.

> Design details: [HELP.md](./HELP.md) — "Hilos Virtuales", "Resilience" and the per-adapter
> configuration sections.

## Extending the Template

1. Create domain model in `domain/models/`
2. Create domain exceptions in `domain/exceptions/`
3. Define input/output ports in `application/ports/`
4. Create domain service in `domain/services/`
5. Define DTOs in `application/dto/`
6. Create mapper in `application/mappers/`
7. Implement use case in `application/usecases/`
8. Create JPA entity in `infrastructure/persistence/entities/`
9. Implement repository adapter in `infrastructure/persistence/adapters/`
10. Create REST controller in `infrastructure/controllers/`
11. Add comprehensive tests

The template includes a complete Role management example demonstrating the domain model with
business logic, use cases for CRUD operations, REST API endpoints, JPA persistence and
comprehensive testing.

## Documentation

- **English**: this file
- **Español**: [HELP.md](./HELP.md) — detailed architecture, configuration of every adapter
  (Kafka, SQS, RabbitMQ, SNS, EventBridge, Redis, gRPC), virtual threads, resilience, testing
  and extension guide

## License

[MIT](./LICENSE)
