# Notification Service

A REST API for managing user notification preferences and delivering notifications based on category subscriptions. Built with Spring Boot 3.5, Java 21, and PostgreSQL.

---

> **Recommendations – Best Practices**
>
> The following improvements can be considered as part of best practices:
>
> 1. **Base package** – The base package is currently `de.dkb.api.codeChallenge` (camelCase). It can be renamed to `de.dkb.api.codechallenge` (all lowercase) to align with Java package naming conventions.
>
> 2. **API endpoints** – The endpoints `POST /api/v1/register` and `POST /api/v1/notify` can be renamed to `POST /api/v1/subscriptions` and `POST /api/v1/notifications` respectively for clearer REST resource naming.

---

## Overview

Users subscribe to notification categories (Category A or Category B) and receive notifications based on those preferences. The service supports:

- **Category A** – notification types: `type1`, `type2`, `type3`, `type6`
- **Category B** – notification types: `type4`, `type5`

Subscribing to any type in a category allows receiving all notifications within that category (category-based subscription model).

---

## Tech Stack

| Component        | Technology                          |
|-----------------|--------------------------------------|
| Language        | Java 21                             |
| Framework       | Spring Boot 3.5                     |
| Database        | PostgreSQL (Liquibase migrations)   |
| API Documentation | SpringDoc OpenAPI (Swagger UI)    |
| Security        | Spring Security                     |
| Observability   | Micrometer, Prometheus, Actuator    |
| Build           | Gradle 8.x                          |

---

## Prerequisites

- **Java 21** or later
- **Docker** and **Docker Compose**
- **Gradle** (wrapper included)

---

## Quick Start with Docker Compose

### 1. Build the JAR

```bash
./gradlew build
```

### 2. Start all services

```bash
docker compose up -d
```

This starts:
- **PostgreSQL** (port 5432)
- **Application** (port 8080)
- **Prometheus** (port 9090)
- **Grafana** (port 3000)

### 3. Stop services

```bash
docker compose stop
```

Or to stop and remove containers:

```bash
docker compose down
```

### 4. Verify

- **Health:** http://localhost:8080/actuator/health  
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html  
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs  

---

## Database – Verify Liquibase & Data

### Connect to PostgreSQL

```bash
docker exec -it codechallenge_postgres psql -U postgres -d codechallenge_db
```

### List tables

```
\dt
```

### List data in users table

```sql
SELECT * FROM users;
```

Or to check Liquibase changelog history:

```sql
SELECT id, author, filename, dateexecuted FROM databasechangelog ORDER BY dateexecuted;
```

---

## Alternative: Local Development (Gradle)

For local development without Docker for the app:

```bash
# 1. Start PostgreSQL only
docker compose up -d postgres

# 2. Run application with Gradle
./gradlew bootRun --args='--spring.profiles.active=dev'
```

---

## API Endpoints

### Register User

**POST** `/api/v1/register`

Register a user with notification preferences.

**Request:**
```json
{
  "userId": "bcce103d-fc52-4a88-90d3-9578e9721b36",
  "notifications": ["type1", "type5"]
}
```

**Response (201 Created):**
```json
{
  "timestamp": "2025-03-04T12:00:00Z",
  "traceId": "abc123",
  "message": "User registered successfully",
  "data": {
    "userId": "bcce103d-fc52-4a88-90d3-9578e9721b36",
    "registeredTypes": ["type1", "type5"]
  }
}
```

### Send Notification

**POST** `/api/v1/notify`

Send a notification to a user.

**Request:**
```json
{
  "userId": "bcce103d-fc52-4a88-90d3-9578e9721b36",
  "notificationType": "type5",
  "message": "Your app rocks!"
}
```

**Response (200 OK):**
```json
{
  "timestamp": "2025-03-04T12:00:00Z",
  "traceId": "abc123",
  "message": "Notification processed successfully",
  "data": {
    "userId": "bcce103d-fc52-4a88-90d3-9578e9721b36",
    "notificationType": "type5",
    "status": "SENT"
  }
}
```

---

## Postman Collection

A Postman collection covering all API use cases is in `postman/Notification-API.postman_collection.json`. Import it into Postman to test:

- **Register**: success (multiple types, single type, Category B), 400 (invalid/unsupported type, empty body, invalid UUID)
- **Notify**: success (subscribed user, category delivery), 404 (user not found), 403 (not subscribed), 400 (invalid type, validation, invalid UUID)
- **Full flow**: Register → Notify (runs Step 1, captures userId, runs Step 2)

Set `baseUrl` to `http://localhost:8080` (or your server). For notify success cases, run the corresponding register request first with the same `userId`.

---

## Example cURL Commands

```bash
# Register user with preferences
curl -X POST -H "Content-Type: application/json" \
  http://localhost:8080/api/v1/register \
  -d '{"userId": "bcce103d-fc52-4a88-90d3-9578e9721b36", "notifications": ["type1","type5"]}'

# Send notification
curl -X POST -H "Content-Type: application/json" \
  http://localhost:8080/api/v1/notify \
  -d '{"userId": "bcce103d-fc52-4a88-90d3-9578e9721b36", "notificationType": "type5", "message": "Your app rocks!"}'
```

---

## Project Structure

```
src/main/java/de/dkb/api/codeChallenge/
├── CodeChallengeApplication.java
└── notification/
    ├── common/           # GenericResponse, ApiMessages
    ├── config/           # Security, OpenAPI, filters, startup validator
    ├── controller/       # REST endpoints
    │   ├── dto/           # request, response, validation
    │   └── exception/     # ErrorCode, domain exceptions
    ├── domain/            # Category, NotificationType
    ├── entity/            # JPA User entity
    ├── handler/           # GlobalExceptionHandler
    ├── kafka/             # Kafka consumer (optional, disabled by default)
    ├── mapper/            # Entity ↔ DTO conversion
    ├── repository/        # UserRepository
    └── service/           # Business logic, strategies, processor
```

---

## Application Profiles

| Profile | Purpose                    | Database            |
|---------|----------------------------|---------------------|
| `dev`   | Local development          | PostgreSQL (docker) |
| `test`  | Integration tests          | H2 in-memory        |
| `qa`    | QA environment            | Configured externally |
| `prod`  | Production                 | Configured externally |

---

## Running Tests

```bash
./gradlew test
```

Integration tests use H2 with the `test` profile.

---

## Error Responses

Errors use a consistent format:

```json
{
  "timestamp": "2025-03-04T12:00:00Z",
  "traceId": "abc123",
  "errorCode": "USR-404",
  "message": "User not found",
  "data": null
}
```

| Error Code   | HTTP Status | Description                                         |
|--------------|-------------|-----------------------------------------------------|
| `VAL-001`    | 400         | Validation failed (invalid UUID, enum, or payload)  |
| `USR-404`    | 404         | User not found                                      |
| `USR-403`    | 403         | User not subscribed to notification                 |
| `NOT-401`    | 400         | Unsupported notification type                      |

---

## Observability

- **Health:** http://localhost:8080/actuator/health  
- **Info:** http://localhost:8080/actuator/info  
- **Metrics:** http://localhost:8080/actuator/metrics  
- **Prometheus:** http://localhost:8080/actuator/prometheus  

Requests include an `X-Trace-Id` header for tracing.

---

## Code Challenge

This project is part of the DKB API code challenge. For full context and requirements, see [CODE_CHALLENGE.md](./CODE_CHALLENGE.md).

---

## License

Internal use – DKB Code Challenge.
