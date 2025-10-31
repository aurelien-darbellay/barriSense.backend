# 🧩 Barrisense Backend

A modular **microservice ecosystem** built with **Spring Boot 3.x (Java 21)**, fully **Dockerized** for local and remote
deployment.  
This backend powers the Barrisense platform (gateway, auth, user, complaint, feedback, etc.) and can be launched even if
you have **no Java or IDE** installed.

---

## 🧱 Microservice Architecture

- **gateway-service** → entry point, routes all API traffic and hosts aggregated Swagger UI
- **auth-service** → manages authentication, CSRF tokens, JWT, and registration/login
- **user-service** → handles user data, profiles, and CRUD operations
- **complaint-service** → manages complaints and related workflows
- **feedback-service** → handles feedback submissions and neighborhood insights
- **rabbitmq** → message broker for asynchronous communication between services
- *(shared library)* **error-response-core** → centralized error-handling and response models

---

## ⚙️ Prerequisites

- **Docker Desktop** (Windows, macOS, or Linux)
- **Git Bash** (for Windows users)
- Optional: **Postman** for API testing

---

## 🧩 Available Scripts

### 🟢 `run-all.sh`

Starts all microservices locally (via Gradle) in separate Git Bash windows.

```bash
./run-all.sh
```

Each service runs its own `bootRun` task and logs in its terminal.

### 🔴 `stop-all.sh`

Gracefully stops all microservices started with `run-all.sh`.

```bash
./stop-all.sh
```

---

## 🐳 Smart Docker Compose System

### 🔧 `compose-smart.sh`

The main orchestrator for Dockerized services.
You can start a single service or the full ecosystem (including RabbitMQ).

Usage:

```bash
./compose-smart.sh all
./compose-smart.sh user-service
```

Features:

- Ensures shared network `barrisense-net` exists.
- Starts RabbitMQ via `rabbit-compose.yml`.
- Builds local images if missing.
- Launches services defined in `docker-compose-dev.yml`.

### ⚙️ Make the scripts executable

If execution is denied:

```bash
chmod +x compose-smart.sh
chmod +x run-all.sh
chmod +x stop-all.sh
```

Then run:

```bash
./compose-smart.sh all
```

---

## 🧭 Swagger API Documentation

Each microservice exposes its own **Swagger UI**, an interactive web interface that displays and documents **all
available REST endpoints**, their HTTP methods, request parameters, and response schemas.

Swagger lets you:

- Browse each API endpoint (GET, POST, PUT, DELETE…)
- Inspect and test requests directly from the browser
- View validation rules, example payloads, and error formats

Depending on how you run the backend, access it as follows:

| Mode                     | URL                                                  | Description                                                     |
  |--------------------------|------------------------------------------------------|-----------------------------------------------------------------|
| Local (via IDE / Gradle) | `http://localhost:<port>/swagger-ui/index.html`      | Direct access to each service’s endpoints                       |
| Docker via Gateway       | `http://localhost:8080/swagger-ui/index.html`        | Aggregated Swagger that lists **all** services and their routes |
| Inside Docker Network    | `http://<service-name>:<port>/swagger-ui/index.html` | Internal cross-service access                                   |

Notes:

- The **gateway-service** hosts an aggregated Swagger UI that merges all microservice docs into one interface.
- Endpoints like `/api/users/v3/api-docs` or `/api/auth/v3/api-docs` are internally rewritten and proxied to
  `/v3/api-docs` by the gateway.
- When using Docker, only the **gateway’s Swagger** (`localhost:8080`) is reachable externally — all others are
  internal.

---

## 📬 Postman Collections

A Postman suite is included (folder postman), it includes:

**`Localhost_barriSense (env)`**
**`Complaints Collection`**
**`Gateway Collection`**
**`User Collection`**
**`Auth Collection`**

Key Features:

- Pre-request script auto-adds `X-XSRF-TOKEN` for all `/protected/` routes using your `csrf_token` env variable.
- Post-request scripts in **Register** and **Login** update the `username` environment variable automatically.

How to Use:

1. Open Postman → Import the file.
2. Create an environment with:
    - `base_url` = `http://localhost`
    - `gateway_port` = `8080`
3. Run **Get CSRF (via Gateway)** first → saves `csrf_token`.
4. Then run **Register** or **Login** → sets `username`.
5. Access any `/protected/` route → CSRF header added automatically.

---

## 🐇 RabbitMQ Management Console

Once RabbitMQ starts, open:
```
http://localhost:15672
```

Default credentials:

- **Username:** `admin`
  -**Password:** `admin`

You can monitor queues and message flow between microservices.

---

## 🧠 Running Without Java or IDE

If you have no Java environment:
1. Make scripts executable (first time only):
```bash
chmod +x compose-smart.sh run-all.sh stop-all.sh
```
2. Start the full backend:
```bash
./compose-smart.sh all
```
This will:
- Create the `barrisense-net` network
- Start RabbitMQ
- Build Docker images for each service
- Launch all containers in the background
3. Check running containers:
```bash
docker ps
```
4. Stop all containers:
```bash
docker compose -f docker-compose-dev.yml down
docker compose -f rabbit-compose.yml down
```

---

## ✅ Summary

- Use `run-all.sh` / `stop-all.sh` for direct Gradle runs.
- Use `compose-smart.sh` for fully Dockerized deployment.
- Swagger displays **all REST endpoints** interactively, per service or aggregated via gateway.
- Postman collection automates CSRF and authentication testing.
- Everything can run entirely on Docker — no JDK or IDE needed.
