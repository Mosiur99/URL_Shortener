# Redis URL Shortener

A micro URL shortener with click analytics built with Java, Spring Boot, JSP, and Redis.

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker and Docker Compose

## Quick Start

### 1. Start Redis

```bash
docker compose up -d
docker exec -it redis-url-shortener-db redis-cli ping
```

Expected response: `PONG`

### 2. Run the application

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

### 3. Build and test

```bash
mvn test
mvn clean package
```

The WAR file is produced at `target/redis-url-shortener-1.0.0-SNAPSHOT.war`.

## Configuration

| Property | Default | Description |
|---|---|---|
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6382` | Redis port |
| `APP_BASE_URL` | `http://localhost:8080` | Base URL for generated short links |

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/` | Home page with URL form |
| `POST` | `/api/urls` | Create a short URL |
| `GET` | `/{code}` | Redirect to original URL (302) |
| `GET` | `/analytics` | All links analytics page (JSP) |
| `GET` | `/analytics/{code}` | Single link analytics page (JSP) |
| `GET` | `/api/urls/analytics` | All links analytics JSON |
| `GET` | `/api/urls/{code}/analytics` | Single link analytics JSON |
| `DELETE` | `/api/urls/{code}` | Delete short URL and analytics |

Duplicate URLs return the existing short link (`200 OK`) instead of creating a new one (`201 Created`).

## curl Examples

```bash
# Create a short URL
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://redis.io/docs/latest/"}'

# Redirect (replace CODE with the generated code)
curl -i http://localhost:8080/CODE

# Check analytics
curl http://localhost:8080/api/urls/CODE/analytics

# Delete the link
curl -i -X DELETE http://localhost:8080/api/urls/CODE
```

## Inspect Redis

```bash
docker exec -it redis-url-shortener-db redis-cli
```

```redis
SCAN 0 MATCH "*"
GET url:CODE
HGETALL metadata:CODE
HGETALL analytics:CODE
HGETALL analytics:CODE:daily
```

## Redis Key Model

| Key | Type | Purpose |
|---|---|---|
| `url:{code}` | String | Original URL |
| `metadata:{code}` | Hash | `createdAt` timestamp (UTC ISO-8601) |
| `analytics:{code}` | Hash | `totalClicks` counter |
| `analytics:{code}:daily` | Hash | UTC `YYYY-MM-DD` daily click counts |
| `lookup:{sha256}` | String | Maps original URL hash to short code (deduplication) |
| `urls:index` | Set | All active short codes |
