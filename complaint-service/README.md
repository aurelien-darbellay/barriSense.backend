# Barrisense Complaint Service

CRUD and analytics for neighborhood complaints. Exposes public read endpoints and a protected create endpoint. Uses H2 + JPA, seeds neighborhoods from SQL, and provides Swagger UI.

## Overview
- Spring Boot Web (MVC), Data JPA, Validation, H2.
- Endpoints under `/api/complaints`; writes are under `/api/complaints/protected/**` so the gateway enforces JWT + CSRF.
- H2 in‑memory DB with schema and seed SQL for neighborhoods.
- Structured logging with custom Logback configuration.

## Endpoints
Base path: `/api/complaints`
- GET `/` — list all complaints.
- GET `/{id}` — get complaint by ID.
- GET `/by-neighborhood/{hoodId}` — list complaints for a neighborhood ID.
- GET `/count/by-neighborhood/{hoodId}` — count complaints for a neighborhood ID.
- GET `/count/by-neighborhood/all` — counts for all neighborhoods.
- POST `/protected` — create a new complaint (protected by the gateway).

Controller: `complaint-service/src/main/java/com/barrisense/backend/complaint/controller/ComplaintController.java:1`

### Complaint body (POST /protected)
Fields from entity: `complaint-service/src/main/java/com/barrisense/backend/complaint/entity/Complaint.java:1`
- `userId` (UUID, required)
- `hoodId` (number, required)
- `hoodName` (string, required)
- `content` (string, required, <=1000)

Example (through gateway; requires cookies and CSRF header):
```bash
curl -X POST http://localhost:8080/api/complaints/protected \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: <csrf-token>" \
  --cookie "XSRF-TOKEN=<csrf-token>; JWT=<your-jwt>" \
  -d '{
        "userId":"11111111-2222-3333-4444-555555555555",
        "hoodId":31,
        "hoodName":"La Vila de Gràcia",
        "content":"Demasiado ruido por la noche."
      }'
```

## Persistence
- Entities:
  - Complaint: `complaint-service/src/main/java/com/barrisense/backend/complaint/entity/Complaint.java:1`
  - Neighborhood: `complaint-service/src/main/java/com/barrisense/backend/complaint/entity/Neighborhood.java:1`
- Schema & data:
  - `schema.sql` creates `barrios` and `neighborhood_postal_codes` so `data.sql` can load neighborhoods.
    - `complaint-service/src/main/resources/schema.sql:1`
    - `complaint-service/src/main/resources/data.sql:1`
- Repositories:
  - ComplaintRepository (custom counts and finders): `complaint-service/src/main/java/com/barrisense/backend/complaint/repository/ComplaintRepository.java:1`
  - NeighborhoodRepository: `complaint-service/src/main/java/com/barrisense/backend/complaint/repository/NeighborhoodRepository.java:1`
- Optional data seeding for complaints: `complaint-service/src/main/java/com/barrisense/backend/complaint/config/DataInitializer.java:1` populates random complaints when DB is empty.

## Configuration
- File: `complaint-service/src/main/resources/application.yml:1`
- Port: `8082`
- DB: H2 in‑memory (`jdbc:h2:mem:barrisensedb`) with `spring.sql.init.mode=always` to run `schema.sql`/`data.sql`.
- JPA: `ddl-auto=update`, `show-sql=true`, `format_sql=true`.
- Dev CORS filter: `complaint-service/src/main/java/com/barrisense/backend/complaint/config/DevCorsConfig.java:1` (direct dev access). In production, access via gateway.
- Logging: custom Logback with JSON and file rolling: `complaint-service/src/main/resources/logback-spring.xml:1`.

## Swagger / OpenAPI
- Direct (service): `http://localhost:8082/swagger-ui/index.html`
- Via gateway aggregation: `http://localhost:8080/swagger-ui/index.html`

## Run Locally
From repo root:
```bash
./gradlew :complaint-service:bootRun
# Windows
# gradlew.bat :complaint-service:bootRun
```
Build jar:
```bash
./gradlew :complaint-service:bootJar
```
Access:
- Service: `http://localhost:8082`
- Swagger: `http://localhost:8082/swagger-ui/index.html`
- H2 console: `http://localhost:8082/h2-console`

## Docker
Build (multi‑module aware; pass service name):
```bash
docker build \
  -f complaint-service/Dockerfile \
  --build-arg SERVICE_NAME=complaint-service \
  -t ghcr.io/barrisense/complaint-service:1.0.2 .
```
Run:
```bash
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=dev \
  ghcr.io/barrisense/complaint-service:1.0.2
```

## Notes
- Only the create endpoint is under `/protected`; all reads are public. When calling through the gateway, protected writes require `JWT` cookie and `X-XSRF-TOKEN` header.
- Controller and service use enum‑based structured logs; see `ComplaintControllerLogEvent` and `ComplaintServiceLogEvent` for messages and levels.
- Neighborhood catalog is loaded at startup from `data.sql`. Adjust or replace with an external source as needed.

