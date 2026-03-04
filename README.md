# Notification Service

A REST API for managing user notification preferences and delivering notifications based on category subscriptions. Built with Spring Boot 3.5, Java 21, and PostgreSQL.

> **Modifications from original code challenge:**
> 1. **Package name** – `de.dkb.api.codeChallenge` → `de.dkb.api.notificationhub` (non-standard convention).
> 2. **API name** – Endpoints updated per OpenAPI/REST conventions:  
>    - `POST /api/v1/register` → `POST /api/v1/subscriptions`  
>    - `POST /api/v1/notify` → `POST /api/v1/notifications`

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

## Quick Start

### 1. Build

```bash
./gradlew build
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Run Application (Dev Profile)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Or using an environment variable:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### 4. Verify

- **Health:** http://localhost:8080/actuator/health  
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html  
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs  

---

## Docker Commands (Start / Stop)

### PostgreSQL (Database)

| Action | Command |
|--------|---------|
| **Start** (background) | `docker compose up -d` |
| **Stop** | `docker compose stop` |
| **Stop and remove containers** | `docker compose down` |
| **View status** | `docker compose ps` |
| **View logs** | `docker compose logs -f postgres` |

### Spring Boot Application

The application runs via Gradle, not Docker. To stop it:

| Action | Command |
|--------|---------|
| **Stop** | Press `Ctrl+C` in the terminal where `bootRun` is running |
| **Stop (if running in background)** | `pkill -f NotificationServiceApplication` |

### Full Start / Stop Example

```bash
# Start everything
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=dev'

# Stop (in another terminal)
# 1. Stop the Spring Boot app: Ctrl+C (or pkill -f NotificationServiceApplication)
# 2. Stop PostgreSQL
docker compose stop
```

---

## API Endpoints

### Register User

**POST** `/api/v1/subscriptions`

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

**POST** `/api/v1/notifications`

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

## Example cURL Commands

```bash
# Register user with preferences
curl -X POST -H "Content-Type: application/json" \
  http://localhost:8080/api/v1/subscriptions \
  -d '{"userId": "bcce103d-fc52-4a88-90d3-9578e9721b36", "notifications": ["type1","type5"]}'

# Send notification
curl -X POST -H "Content-Type: application/json" \
  http://localhost:8080/api/v1/notifications \
  -d '{"userId": "bcce103d-fc52-4a88-90d3-9578e9721b36", "notificationType": "type5", "message": "Your app rocks!"}'
```

---

## Project Structure

```
src/main/java/de/dkb/api/notificationhub/
├── NotificationServiceApplication.java
├── common/               # Shared response models, messages
├── config/               # Security, OpenAPI, filters, startup validator
├── controller/           # REST endpoints
├── domain/               # Category, NotificationType
├── entity/               # JPA entities
├── exception/            # Domain exceptions, error codes, GlobalExceptionHandler
├── kafka/                # Kafka consumer (optional, disabled by default)
├── mapper/               # Entity ↔ DTO conversion (OpenAPI separation)
├── model/                # DTOs, request/response, validation
├── repository/           # JPA repositories
└── service/              # Business logic, strategies, processor
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

Integration tests use H2 with the `test` profile. Ensure `testRuntimeOnly 'com.h2database:h2'` is in `build.gradle`.

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

| Error Code   | HTTP Status | Description                           |
|--------------|-------------|---------------------------------------|
| `VAL-001`    | 400         | Validation failed                     |
| `USR-404`    | 404         | User not found                        |
| `USR-403`    | 403         | User not subscribed to notification   |
| `NOT-400`    | 400         | Invalid notification type             |
| `NOT-401`    | 400         | Unsupported notification type        |

---

## Observability

- **Health:** `http://localhost:8080/actuator/health`  
- **Info:** `http://localhost:8080/actuator/info`  
- **Metrics:** `http://localhost:8080/actuator/metrics`  
- **Prometheus:** `http://localhost:8080/actuator/prometheus`  

Requests include an `X-Trace-Id` header for tracing.

---

## Code Challenge

This project is part of the DKB API code challenge. For full context and requirements, see [CODE_CHALLENGE.md](./CODE_CHALLENGE.md).

---

## License

Internal use – DKB Code Challenge.
