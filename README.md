# Hex4j - Hexagonal Architecture with Java and Spring Boot

This project implements a hexagonal architecture approach using Java 21, Spring Boot Framework and Gradle, with a variation where ports are located in the domain layer.

## Architecture

### Hexagonal Architecture Variation

This implementation presents a variation of traditional hexagonal architecture:

- **Domain**: Contains ports (interfaces) and domain service with command and query operations, plus the model
- **Application**: Acts as orchestrator with application handlers and mappers, injecting the service through the input port to access the domain service
- **Infrastructure**: Implements adapters that connect with external systems

### Project Structure

```
src/main/java/co/onmind/microhex/
├── domain/
│   ├── models/          # Domain models (Role)
│   ├── ports/           # Ports/Interfaces (RoleServicePort, RoleRepositoryPort, NotificationPort)
│   │   ├── in/          # Input ports (RoleServicePort)
│   │   └── out/         # Output ports (RoleRepositoryPort, NotificationPort)
│   ├── services/        # Domain services (RoleService)
│   └── exceptions/      # Domain exceptions
├── application/
│   ├── dto/             # DTOs for requests/responses
│   ├── mappers/         # Mappers between domain and DTOs
│   └── handlers/        # Handlers or application use cases (RoleHandler)
├── infrastructure/
│   ├── controllers/     # REST controllers or end-points (RoleController)
│   ├── persistence/     # Database persistence adapters
│   ├── notification/    # Notification adapters
│   └── configuration/   # Spring configurations
└── transverse/          # Cross-cutting concerns
```

## Technologies

- **Java 21**: Modern programming language with Virtual Threads support
- **Spring Boot 3.5.4**: Reactive and lightweight web framework
- **Gradle**: Dependency manager and build tool
- **H2 Database**: In-memory database for development
- **JUnit 5**: Modern testing framework
- **Bean Validation**: Data validation framework

## Features

- ✅ **Hexagonal architecture** with ports in domain
- ✅ **Clear separation of responsibilities** by layers
- ✅ **Dependency injection** with Spring Boot
- ✅ **Data validation** with Bean Validation
- ✅ **Enhanced HTTP error handling**
- ✅ **H2 database** with automatic initialization
- ✅ **Structured logging** for monitoring
- ✅ **Virtual Threads** for async notifications (Java 21)

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

### Running the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```
<!--
### Accessing H2 Console

When the application is running, you can access the H2 database console at:
`http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:microhex`
- Username: `sa`
- Password: `password`

## Example Implementation

The template includes a complete Role management example demonstrating:

- Domain model with business logic
- Use cases for CRUD operations
- REST API endpoints
- JPA persistence
- Comprehensive testing
-->
## Endpoints API

### Roles
- `POST /api/v1/roles` - Create role
- `GET /api/v1/roles` - Get all roles
- `GET /api/v1/roles/{id}` - Get role by ID
- `PUT /api/v1/roles/{id}` - Update role
- `DELETE /api/v1/roles/{id}` - Delete role
- `GET /api/v1/roles/search?name={pattern}` - Search roles by pattern
- `GET /api/v1/roles/count` - Count roles

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

## Execution

### Requirements
- **Java 21+** (recommended to leverage new features)
- **Gradle 8+** with Kotlin DSL support

1. Create domain model in `domain/models/`
2. Create domain exceptions in `domain/exceptions/`
3. Define input/output ports in `domain/ports/`
4. Create domain service in `domain/services/`
5. Define DTOs in `application/dto/`
6. Create mapper in `application/mappers/`
7. Implement handler in `application/handlers/`
8. Create JPA entity in `infrastructure/persistence/entities/`
9. Implement repository adapter in `infrastructure/persistence/adapters/`
10. Create REST controller in `infrastructure/controllers/`
11. Add comprehensive tests

```bash
# Build application
./gradlew build

# Run application
./gradlew bootRun

# Generate coverage report
./gradlew test jacocoTestReport
```

> The application runs on `http://localhost:8080`  

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
