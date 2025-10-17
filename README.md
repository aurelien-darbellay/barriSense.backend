# barriSense.backend — Auth Microservice (Spring Boot 3, Java 21)

This is a minimal multi-module Gradle project containing **auth-service** only, ready to issue and validate **JWTs in HttpOnly cookies**, with **H2 in-memory DB** and **Swagger UI**.

## Run

```bash
cd barriSense.backend
./gradlew :auth-service:bootRun
```

Then open Swagger UI:
- http://localhost:8081/swagger-ui/index.html

H2 Console:
- http://localhost:8081/h2-console  (JDBC URL: `jdbc:h2:mem:authdb`, user: `sa`, pass: `password`)

## Auth Endpoints (via Swagger)
- `POST /auth/register`
- `POST /auth/login`  → sets `JWT` and `JWT_REFRESH` **HttpOnly** cookies
- `GET /auth/me`      → requires `JWT` cookie
- `POST /auth/refresh`→ uses `JWT_REFRESH` cookie to issue new `JWT`
- `POST /auth/logout` → clears cookies

## Notes
- Cookies are `HttpOnly; SameSite=Lax` and not `Secure` (dev only). Adjust for production.
- JWT secret and expirations are set in `auth-service/src/main/resources/application.yml`.


### 3️⃣ Testing flow

#### 1️⃣ Get CSRF Token
- Run `GET /auth/csrf`
- This request will store the token automatically in the environment variable `{{csrf_token}}`.

#### 2️⃣ Register or Login
- Run `POST /auth/register` or `POST /auth/login`
- The `X-XSRF-TOKEN` header is automatically added from the stored token.
- A successful login sets `JWT` and `JWT_REFRESH` cookies.

#### 3️⃣ Check Current User
- Run `GET /auth/me`
- Requires the `JWT` cookie from login.

#### 4️⃣ Refresh Token
- Run `POST /auth/refresh`
- Uses the `JWT_REFRESH` cookie to issue a new `JWT`.

#### 5️⃣ Logout
- Run `POST /auth/logout`
- Clears all cookies and invalidates the session.

