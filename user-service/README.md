# Barrisense User Service

User profile and management microservice. Persists user data, listens to user-created events from Auth Service, and exposes CRUD endpoints (protected via the gateway path convention).

## Overview
- Spring Boot Web (MVC), Data JPA, H2, AMQP (RabbitMQ).
- Receives `user.created` events to create initial user records.
- CRUD over users by ID/username. Endpoints are mounted under `/api/users/protected/**` so the gateway enforces authentication/CSRF.
- Swagger UI for API exploration.

## Endpoints
Base path: `/api/users/protected`
- GET `/{id}` — fetch by UUID.
- GET `/by-username/{username}` — fetch by username.
- POST `/new` — create new user (validates unique username/email). Returns `201` with the created user.
- PUT `/by-username/{username}` — update mutable fields (email, profilePictureUrl, active).
- DELETE `/by-username/{username}` — delete by username. Returns `204`.

Controller: `user-service/src/main/java/com/barrisense/backend/user/controller/UserController.java:1`

## Messaging
- Listens on queue `user.created.queue` for `user.created` events.
- Exchange: `user.exchange`, Routing key: `user.created`.
- Listener creates a corresponding user record with ID/username/roles.
- Files:
  - `user-service/src/main/java/com/barrisense/backend/user/messaging/UserCreatedListener.java:1`
  - `user-service/src/main/java/com/barrisense/backend/user/config/RabbitConfig.java:1`
  - `user-service/src/main/java/com/barrisense/backend/user/messaging/RabbitConstants.java:1`

## Persistence
- Entity: `user-service/src/main/java/com/barrisense/backend/user/entity/User.java:1`
  - UUID `id`, unique `username`, optional unique `email`, `profilePictureUrl`, `active`, `roles`.
  - `createdAt` via `@CreationTimestamp`.
- Repository: `user-service/src/main/java/com/barrisense/backend/user/repository/UserRepository.java:1`.
- H2 console exposed at `/h2-console` in dev.
- Optional Data seeding: `user-service/src/main/java/com/barrisense/backend/user/config/DataInitializer.java:1` populates sample users when DB is empty.

## Configuration
- File: `user-service/src/main/resources/application.yml:1`
- Server: `port=8083`
- Database: H2 in-memory (`jdbc:h2:mem:userdb`), Hibernate DDL auto `update`.
- RabbitMQ: `localhost:5672` with `admin/admin` in dev.
- Logging: `com.barrisense.backend.user=DEBUG`.
- Dev CORS filter: `user-service/src/main/java/com/barrisense/backend/user/config/DevCorsConfig.java:1` (for direct dev access). In production access via gateway.

## Swagger / OpenAPI
- Direct (service): `http://localhost:8083/swagger-ui/index.html`
- Via gateway aggregation: `http://localhost:8080/swagger-ui/index.html`

## Run Locally
From repo root:
```bash
./gradlew :user-service:bootRun
# Windows
# gradlew.bat :user-service:bootRun
```
Build jar:
```bash
./gradlew :user-service:bootJar
```
Access:
- Service: `http://localhost:8083`
- Swagger: `http://localhost:8083/swagger-ui/index.html`
- H2 console: `http://localhost:8083/h2-console`

## Docker
Build (multi‑module aware; pass service name):
```bash
docker build \
  -f user-service/Dockerfile \
  --build-arg SERVICE_NAME=user-service \
  -t ghcr.io/barrisense/user-service:1.0.3 .
```
Run:
```bash
docker run -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_RABBITMQ_HOST=host.docker.internal \
  -e SPRING_RABBITMQ_PORT=5672 \
  ghcr.io/barrisense/user-service:1.0.3
```

## Notes
- Endpoints are under `/api/users/protected/**` so the gateway’s security config enforces authentication and CSRF. When calling through the gateway, include `JWT` cookie and `X-XSRF-TOKEN` header for writes.
- Unique constraints are enforced at service level for `username` and `email`. Conflicts return 4xx with error messages.
- DataInitializer only seeds when the DB is empty to keep dev runs deterministic.

