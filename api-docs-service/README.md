# Barrisense API Docs Service

Small Spring Boot 3.3.4 service that aggregates Swagger / OpenAPI definitions from the different Barrisense microservices and exposes a single Swagger UI.

## Services aggregated

- 🔐 Auth Service → `http://localhost:8081/v3/api-docs`
- 📢 Complaint Service → `http://localhost:8082/v3/api-docs`
- 👥 User Service → `http://localhost:8083/v3/api-docs`
- 🚪 Gateway Service → `http://localhost:8080/v3/api-docs`

## Run

```bash
./gradlew bootRun
```

Then open:

👉 http://localhost:8089/swagger-ui.html
```

## Notes

- You can add/remove services by editing `src/main/resources/application.yml`.
- This service does **not** need DB nor security for internal usage.
