# Hex - Hexagonal Architecture with Kotlin and Micronaut

This project implements a hexagonal architecture approach using Kotlin, Micronaut Framework and Gradle KTS, with a variation where ports are located in the domain layer.

## Architecture

### Hexagonal Architecture Variation

This implementation presents a variation of traditional hexagonal architecture:

- **Domain**: Contains ports (interfaces) and domain service with command and query operations, plus the model
- **Application**: Acts as orchestrator with application use cases (or handlers) and mappers, injecting the service through the input port to access the domain service
- **Infrastructure**: Implements adapters that connect with external systems

### Project Structure

```
src/
├── main/kotlin/co/onmind/hex/
│   ├── domain/
│   │   ├── model/           # Domain models (Role)
│   │   ├── ports/           # Ports/Interfaces (RoleServicePort, RoleRepository, NotificationPort)
│   │   └── service/         # Domain services (RoleService)
│   ├── application/
│   │   ├── dto/             # DTOs for requests/responses
│   │   ├── mapper/          # Mappers between domain and DTOs
│   │   └── handler/         # Handlers or application use cases (RoleHandler)
│   └── infrastructure/
│       ├── controller/      # Framework controller or end-points (RoleController)
│       ├── persistence/     # Database persistence adapters
│       └── webclient/       # Web client adapters
└── test/kotlin/             # Unit and integration tests
```

## Technologies

- **Kotlin 1.9.20**: Modern programming language
- **Micronaut 4.2.1**: Reactive and lightweight web framework
- **Gradle KTS**: Dependency manager and build tool with Kotlin DSL
- **H2 Database**: In-memory database for development
- **Micronaut Data JDBC**: Reactive and efficient ORM
- **JUnit 5**: Modern testing framework
- **Mockito Kotlin**: Mocking for Kotlin tests

## Features

- ✅ **Hexagonal architecture** with ports in domain
- ✅ **Clear separation of responsibilities** by layers
- ✅ **Dependency injection** with Micronaut
- ✅ **Data validation** with Bean Validation
- ✅ **Enhanced HTTP error handling**
- ✅ **Unit and integration tests** (75%+ coverage)
- ✅ **H2 database** with automatic initialization
- ✅ **Structured logging** for monitoring

## Endpoints API

### Roles
- `POST /api/v1/roles` - Create role
- `GET /api/v1/roles` - Get all roles
- `GET /api/v1/roles/{id}` - Get role by ID
- `PUT /api/v1/roles/{id}` - Update role
- `DELETE /api/v1/roles/{id}` - Delete role
- `GET /api/v1/roles/search?name={pattern}` - Search roles by pattern
- `GET /api/v1/roles/count` - Count roles

## Execution

### Requirements
- **Java 17+** (21+ recommended to leverage new features)
- **Gradle 8+** with Kotlin DSL support

### Commands

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run application
./gradlew run

# Generate coverage report
./gradlew test jacocoTestReport
```

> The application runs on `http://localhost:8080`  
> Jacoco Reports on: `ca`

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
6. **Robustness**: Enhanced error handling with pattern matching
7. **Scalability**: Architecture ready for microservices
