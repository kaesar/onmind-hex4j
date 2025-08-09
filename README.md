# Hex - Hexagonal Architecture with Kotlin and Micronaut

Este proyecto implementa una arquitectura hexagonal usando Kotlin, Micronaut Framework y Gradle KTS, con una variación donde los puertos se ubican en la capa de dominio.

## Arquitectura

### Variación de Arquitectura Hexagonal

Esta implementación presenta una variación de la arquitectura hexagonal tradicional:

- **Dominio**: Contiene los puertos (interfaces) y el servicio de dominio con operaciones de comandos y queries
- **Aplicación**: Actúa como articulador con handlers/casos de uso de aplicación y mappers
- **Infraestructura**: Implementa los adaptadores que conectan con sistemas externos

### Estructura del Proyecto

```
src/
├── main/kotlin/co/onmind/hex/
│   ├── domain/
│   │   ├── model/           # Modelos de dominio (Role)
│   │   ├── ports/           # Puertos/Interfaces (RoleRepository, NotificationPort)
│   │   └── service/         # Servicios de dominio (RoleService)
│   ├── application/
│   │   ├── dto/             # DTOs para requests/responses
│   │   ├── mapper/          # Mappers entre dominio y DTOs
│   │   └── handler/         # Handlers HTTP (RoleHandler)
│   └── infrastructure/
│       ├── persistence/     # Adaptadores de persistencia
│       └── notification/    # Adaptadores de notificación
└── test/kotlin/             # Tests unitarios e integración
```

## Tecnologías

- **Kotlin 1.9.20**: Lenguaje de programación moderno
- **Java 21**: JVM con características modernas (Virtual Threads, Pattern Matching, etc.)
- **Micronaut 4.2.1**: Framework web reactivo y ligero
- **Gradle KTS**: Gestor de dependencias y build con Kotlin DSL
- **H2 Database**: Base de datos en memoria para desarrollo
- **Micronaut Data JDBC**: ORM reactivo y eficiente
- **JUnit 5**: Framework de testing moderno
- **Mockito Kotlin**: Mocking para tests en Kotlin

## Características

- ✅ **Arquitectura hexagonal** con puertos en dominio
- ✅ **Separación clara de responsabilidades** por capas
- ✅ **Inyección de dependencias** con Micronaut
- ✅ **Validación de datos** con Bean Validation
- ✅ **Manejo de errores HTTP** mejorado
- ✅ **Tests unitarios e integración** (70% cobertura)
- ✅ **Base de datos H2** con inicialización automática
- ✅ **Logging estructurado** para monitoreo
- ✅ **Virtual Threads (Java 21)** para notificaciones asíncronas
- ✅ **Pattern matching mejorado** para manejo de errores
- ✅ **Código moderno** aprovechando características de Java 21

## Endpoints API

### Roles
- `POST /api/v1/roles` - Crear rol
- `GET /api/v1/roles` - Obtener todos los roles
- `GET /api/v1/roles/{id}` - Obtener rol por ID
- `PUT /api/v1/roles/{id}` - Actualizar rol
- `DELETE /api/v1/roles/{id}` - Eliminar rol
- `GET /api/v1/roles/search?name={pattern}` - Buscar roles por patrón
- `GET /api/v1/roles/count` - Contar roles

## Ejecución

### Requisitos
- **Java 21+** (recomendado para aprovechar Virtual Threads y nuevas características)
- **Gradle 8+** con soporte para Kotlin DSL

### Comandos

```bash
# Compilar
./gradlew build

# Ejecutar tests
./gradlew test

# Ejecutar aplicación
./gradlew run

# Generar reporte de cobertura
./gradlew jacocoTestReport
```

La aplicación se ejecuta en `http://localhost:8081`

## Ejemplos de Uso

### Crear un rol
```bash
curl -X POST http://localhost:8081/api/v1/roles \
  -H "Content-Type: application/json" \
  -d '{"name": "DEVELOPER"}'
```

### Obtener todos los roles
```bash
curl http://localhost:8081/api/v1/roles
```

### Buscar roles
```bash
curl "http://localhost:8081/api/v1/roles/search?name=ADMIN"
```

## Principios de Diseño

1. **Inversión de Dependencias**: El dominio no depende de la infraestructura
2. **Separación de Responsabilidades**: Cada capa tiene una responsabilidad específica
3. **Testabilidad**: Fácil testing mediante mocking de puertos
4. **Flexibilidad**: Fácil intercambio de adaptadores de infraestructura
5. **Mantenibilidad**: Código limpio y bien estructurado
6. **Performance Moderna**: Uso de Virtual Threads para operaciones asíncronas
7. **Robustez**: Manejo de errores mejorado con pattern matching
8. **Escalabilidad**: Arquitectura preparada para microservicios

## Características de Java 21 Implementadas

### Virtual Threads
- **Notificaciones asíncronas**: Las notificaciones a sistemas externos se ejecutan en Virtual Threads para mejor rendimiento
- **No bloqueo**: Las operaciones de I/O no bloquean el hilo principal
- **Escalabilidad**: Soporte para miles de operaciones concurrentes con bajo overhead

### Pattern Matching Mejorado
- **Manejo de errores**: Uso de `when` expressions para manejo elegante de excepciones
- **Código más limpio**: Menos boilerplate en el manejo de diferentes tipos de errores
- **Mejor legibilidad**: Lógica de control más expresiva

### Ejemplo de Virtual Threads:
```kotlin
// Notificación asíncrona sin bloquear el hilo principal
Thread.startVirtualThread {
    try {
        notificationPort.notifyRoleCreated(savedRole)
    } catch (e: Exception) {
        println("Failed to send notification: ${e.message}")
    }
}
```

### Ejemplo de Pattern Matching:
```kotlin
// Manejo elegante de múltiples tipos de excepción
when (e) {
    is RoleNotFoundException -> HttpResponse.notFound()
    is RoleAlreadyExistsException -> HttpResponse.status(HttpStatus.CONFLICT)
    is SystemRoleException -> HttpResponse.status(HttpStatus.FORBIDDEN)
    else -> HttpResponse.serverError()
}
```

## Cobertura de Tests

El proyecto mantiene una cobertura de tests del 70% aproximadamente, incluyendo:
- Tests unitarios de dominio
- Tests de servicios
- Tests de mappers
- Tests de adaptadores
- Tests de integración mínimos