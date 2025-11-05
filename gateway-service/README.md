# Barrisense Gateway Service

Spring Cloud Gateway (WebFlux + Security) that fronts all backend microservices, centralizes security concerns, and aggregates OpenAPI/Swagger docs.

## Overview
- Reactive gateway using Spring Cloud Gateway and Spring WebFlux.
- JWT authentication sourced from an HttpOnly `JWT` cookie.
- CSRF double‑submit protection via `XSRF-TOKEN` cookie + `X-XSRF-TOKEN` header.
- CORS configured via env var (`CORS_ALLOWED_ORIGINS`).
- Swagger UI aggregates service docs behind the gateway.

## Tech Stack
- Spring Boot 3.x (Java 21)
- Spring Cloud Gateway (2023.x BOM)
- Spring Security (WebFlux)
- JJWT (HS256/HS512)
- Springdoc OpenAPI (WebFlux UI)

## Routing
Local profile (`src/main/resources/application.yml`):
- `/api/auth/**` → `http://localhost:8081`
- `/api/complaints/**` → `http://localhost:8082`
- `/api/users/**` → `http://localhost:8083`

Docker profile (`src/main/resources/application-docker.yml`):
- `/api/auth/**` → `http://auth-service:8081`
- `/api/complaints/**` → `http://complaint-service:8082`
- `/api/users/**` → `http://user-service:8083`

Global filters:
- `RemoveResponseHeader=Server`
- `DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials`
- Response header rewrite for `Set-Cookie` domain (auth route).
- Path rewrites under Docker for `/v3/api-docs` and `/h2-console`.

## Security
### Authentication (JWT)
- Custom WebFilter reads `JWT` cookie, validates via `jwt.secret`, and builds an authenticated principal with roles claim.
- Skips `/public/**` paths; all other paths are inspected but only enforced where authorization requires it.

### Authorization
- Any request whose path contains `/protected` requires an authenticated user.
- All other requests are permitted by the gateway (downstream services may also authorize).

### CSRF (double‑submit)
- Cookie: `XSRF-TOKEN`
- Header: `X-XSRF-TOKEN`
- Endpoint to mint a token and set cookie: `GET /csrf` (returns `{ "token": "..." }` and sets `XSRF-TOKEN` cookie).
- For any protected write (e.g., `POST/PUT/PATCH/DELETE` under a `/protected` path), include:
  - Header `X-XSRF-TOKEN: <token>`
  - Cookies `XSRF-TOKEN=<token>; JWT=<jwt>`

### CORS
- Allowed origins come from `CORS_ALLOWED_ORIGINS` (comma‑separated).
- Defaults for dev: `http://localhost:5173,http://127.0.0.1:5173`.
- Allowed methods: `GET, POST, PUT, DELETE, PATCH, OPTIONS`.
- Allowed headers: `Content-Type, Authorization, X-XSRF-TOKEN`.

## Swagger / OpenAPI
- UI: `http://localhost:8080/swagger-ui/index.html`
- Local profile aggregates:
  - Gateway: `/v3/api-docs`
  - Auth: `http://localhost:8081/v3/api-docs`
  - Complaint: `http://localhost:8082/v3/api-docs`
  - User: `http://localhost:8083/v3/api-docs`
- Docker profile proxies through the gateway:
  - Auth: `/api/auth/v3/api-docs`
  - Complaint: `/api/complaints/v3/api-docs`
  - User: `/api/users/v3/api-docs`

## Configuration & Profiles
- Port: `8080`
- Profiles:
  - `dev` (default): local service URIs.
  - `docker`: container service URIs and path rewrites.

### Environment variables
- `CORS_ALLOWED_ORIGINS` (e.g., `http://localhost:5173,http://127.0.0.1:5173`)
- `JWT_SECRET` (required in Docker; defaults to a dev key if not set in `application-docker.yml`).

## Run Locally
From repo root:
```bash
./gradlew :gateway-service:bootRun
# Windows
# gradlew.bat :gateway-service:bootRun
```
Build the jar:
```bash
./gradlew :gateway-service:bootJar
```
Access:
- Gateway: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Docker
Build (multi‑module aware; pass service name):
```bash
docker build \
  -f gateway-service/Dockerfile \
  --build-arg SERVICE_NAME=gateway-service \
  -t ghcr.io/barrisense/gateway-service:1.0.4 .
```
Run:
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e JWT_SECRET=<your-secret> \
  ghcr.io/barrisense/gateway-service:1.0.4
```
Or orchestrate with the repo’s `compose-smart.sh` and `docker-compose-dev.yml` to run all services.

## Quickstart: CSRF + JWT
1) Get CSRF token and cookie:
```bash
curl -i http://localhost:8080/csrf
```
2) Use in protected requests (example):
```bash
curl -X POST http://localhost:8080/api/complaints/protected/create \
  -H "X-XSRF-TOKEN: <csrf-token>" \
  -H "Content-Type: application/json" \
  --cookie "XSRF-TOKEN=<csrf-token>; JWT=<your-jwt>" \
  -d '{"message":"Hello"}'
```

## Notable Files
- `src/main/java/.../config/SecurityConfig.java` — CSRF, auth rules, CORS wiring, filter chain.
- `src/main/java/.../security/JwtAuthenticationFilter.java` — JWT cookie parsing and authentication.
- `src/main/java/.../config/CorsConfig.java` — CORS origins from env.
- `src/main/java/.../controller/CsrfController.java` — CSRF token mint endpoint.
- `src/main/resources/application.yml` — local routes and Swagger aggregation.
- `src/main/resources/application-docker.yml` — Docker routes, rewrites, and Swagger aggregation.
- `Dockerfile` — multi‑stage build for the subproject jar.

## Notes
- Gateway treats routes as public unless their path contains `/protected`. Downstream services should still enforce their own authorization as needed.
- Actuator starter is included; expose/secure management endpoints per your needs.
- For frontend apps, ensure cookies include `SameSite=Lax` compatibility (already set in CSRF controller) and send `X-XSRF-TOKEN` header for protected writes.

