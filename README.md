# Hexagonal Architecture Template for Java

This project demonstrates a complete implementation of hexagonal architecture (Ports and Adapters pattern) using Java and Spring Boot.

## Architecture Overview

![](Hexagonal.png)

The project follows the hexagonal architecture pattern with clear separation of concerns:

- **Domain Layer**: Contains business logic and domain models
- **Application Layer**: Contains use cases and defines ports (interfaces)
- **Infrastructure Layer**: Contains adapters that implement the ports

## Project Structure

```
src/main/java/co/onmind/microhex/
├── domain/
│   ├── models/         # Domain entities and value objects
│   └── services/       # Domain services with business logic
├── application/
│   ├── dto/
│   │   ├── in/         # Input DTOs
│   │   └── out/        # Output DTOs
│   ├── mappers/        # MapStruct mappers
│   ├── usecases/       # Use case implementations
│   └── ports/
│       ├── in/         # Input ports (interfaces)
│       └── out/        # Output ports (interfaces)
├── infrastructure/
│   ├── configuration/  # Spring configurations
│   ├── controllers/    # REST controllers
│   ├── persistence/    # JPA entities and repositories
│   └── webclients/     # External service clients
└── transverse/         # Cross-cutting concerns
```

## Technologies Used

- **Java 21**
- **Spring Boot 3.5.4** - Main framework
- **H2 Database** - In-memory database for development
- **MapStruct 1.5.5** - Object mapping
- **JUnit 5** - Testing framework
- **Gradle** - Build tool

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle (or use the included wrapper)

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
## API Endpoints (Coming in next tasks)

- `POST /api/v1/roles` - Create a new role
- `GET /api/v1/roles` - Get all roles
- `GET /api/v1/roles/{id}` - Get role by ID

## Extending the Template

To add a new entity (e.g., User):

1. Create domain model in `domain/models/`
2. Create domain service in `domain/services/`
3. Define input/output DTOs in `application/dto/`
4. Create mapper in `application/mappers/`
5. Define ports in `application/ports/`
6. Implement use cases in `application/usecases/`
7. Create JPA entity in `infrastructure/persistence/`
8. Implement repository adapter
9. Create REST controller in `infrastructure/controllers/`
10. Add comprehensive tests

## Best Practices

- Keep domain layer pure (no external dependencies)
- Use dependency inversion (depend on abstractions)
- Implement comprehensive testing at all layers
- Follow SOLID principles
- Use meaningful package structure
<!--
## License

This template is provided as-is for educational and development purposes.
-->