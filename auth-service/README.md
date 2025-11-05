# Barrisense Auth Service

Authentication and user management microservice. Provides registration, login, refresh, and logout. Issues JWT access and refresh tokens via HttpOnly cookies and publishes user-created events to RabbitMQ.

## Overview
- Spring Boot Web (MVC), Security, Validation, JPA/H2, AMQP.
- JWT access/refresh with configurable expirations.
- HttpOnly cookies (`JWT`, `JWT_REFRESH`) with `SameSite=Lax`.
- Swagger UI for API exploration.
- Publishes `user.created` events for downstream services.

## Endpoints
- POST `/api/auth/register` — create user; returns `201` with message. No tokens set here.
- POST `/api/auth/login` — authenticate, sets `JWT` and `JWT_REFRESH` cookies; returns `200` with message.
- POST `/api/auth/protected/refresh` — uses `JWT_REFRESH` cookie, returns new `JWT` cookie.
- POST `/api/auth/protected/logout` — clears `JWT` and `JWT_REFRESH` cookies.

See controllers: `auth-service/src/main/java/com/barrisense/backend/auth/controller/PublicAuthController.java:1`, `auth-service/src/main/java/com/barrisense/backend/auth/controller/ProtectedAuthController.java:1`.

## Security
- Config: `auth-service/src/main/java/com/barrisense/backend/auth/config/SecurityConfig.java:1`
  - CSRF disabled (gateway handles CSRF).
  - All endpoints permitted (gateway/clients still enforce access at the edge).
  - BCrypt password encoder and `AuthenticationManager` wiring.
- User lookup: `auth-service/src/main/java/com/barrisense/backend/auth/service/CustomUserDetailsService.java:1`
- JWT service: `auth-service/src/main/java/com/barrisense/backend/auth/service/JwtServiceImpl.java:1`
  - HS256 signing using `jwt.secret`.
  - Separate expirations for access and refresh tokens.

## Persistence
- H2 in‑memory DB, JPA Hibernate auto DDL update.
- Entities: `auth-service/src/main/java/com/barrisense/backend/auth/domain/User.java:1`, roles enum `auth-service/src/main/java/com/barrisense/backend/auth/domain/Role.java:1`.
- Repository: `auth-service/src/main/java/com/barrisense/backend/auth/repository/UserRepository.java:1`.
- H2 Console: `/h2-console` (dev only).

## Messaging
- RabbitMQ AMQP integration: `auth-service/src/main/java/com/barrisense/backend/auth/config/RabbitConfig.java:1`.
- Constants: `auth-service/src/main/java/com/barrisense/backend/auth/messaging/RabbitConstants.java:1`.
- User event publisher: `auth-service/src/main/java/com/barrisense/backend/auth/messaging/UserEventPublisher.java:1` sends `user.created` to exchange `user.exchange`.

## Configuration
- File: `auth-service/src/main/resources/application.yml:1`
- Server: `port=8081`
- Database: in‑memory H2
- RabbitMQ: `localhost:5672` (user/pass `admin`/`admin` for dev)
- JWT:
  - `jwt.secret` (dev default provided; override per env)
  - `jwt.expiration-seconds` (default `3600`)
  - `jwt.refresh-expiration-seconds` (default `604800`)
- CORS (dev profile): `auth-service/src/main/java/com/barrisense/backend/auth/config/DevCorsConfig.java:1`

## Swagger / OpenAPI
- Direct (service): `http://localhost:8081/swagger-ui/index.html`
- Via gateway aggregation: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI UI powered by `springdoc-openapi-starter-webmvc-ui` (`auth-service/build.gradle:1`).

## Run Locally
From repo root:
```bash
./gradlew :auth-service:bootRun
# Windows
# gradlew.bat :auth-service:bootRun
```
Build jar:
```bash
./gradlew :auth-service:bootJar
```
Access:
- Service: `http://localhost:8081`
- Swagger: `http://localhost:8081/swagger-ui/index.html`
- H2 console: `http://localhost:8081/h2-console`

## Docker
Build (multi‑module aware; pass service name):
```bash
docker build \
  -f auth-service/Dockerfile \
  --build-arg SERVICE_NAME=auth-service \
  -t ghcr.io/barrisense/auth-service:1.0.2 .
```
Run:
```bash
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_RABBITMQ_HOST=host.docker.internal \
  -e SPRING_RABBITMQ_PORT=5672 \
  -e JWT_SECRET=<your-secret> \
  ghcr.io/barrisense/auth-service:1.0.2
```

## Example Flows (curl)
Login (sets cookies):
```bash
curl -i -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret"}'
```
Refresh access token (requires `JWT_REFRESH` cookie):
```bash
curl -i -X POST http://localhost:8081/api/auth/protected/refresh \
  --cookie "JWT_REFRESH=<your-refresh-token>"
```
Logout (clears cookies):
```bash
curl -i -X POST http://localhost:8081/api/auth/protected/logout
```
Register user:
```bash
curl -i -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret"}'
```

## Notable Files
- `src/main/java/.../controller/PublicAuthController.java` — register, login, cookie handling.
- `src/main/java/.../controller/ProtectedAuthController.java` — refresh, logout.
- `src/main/java/.../service/AuthServiceImpl.java` — business logic, token issuance, password hashing, events.
- `src/main/java/.../service/JwtServiceImpl.java` — token build/parse/validate.
- `src/main/java/.../service/CustomUserDetailsService.java` — user lookup mapping to Spring Security.
- `src/main/java/.../config/SecurityConfig.java` — security filter chain.
- `src/main/java/.../config/RabbitConfig.java` — AMQP setup.
- `src/main/resources/application.yml` — service config (DB, jwt, rabbit, swagger).
- `Dockerfile` — multi‑stage build for this subproject.

## Notes
- CSRF is disabled in this service because the gateway enforces CSRF for protected routes. When accessed directly in dev, no CSRF header is required.
- Cookies are `HttpOnly; SameSite=Lax`; adjust for your deployment and frontend domain as needed.
- Downstream services should still perform their own authorization checks beyond identity established by JWT.

