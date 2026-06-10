# MicroURL

A production-style micro URL shortener with real-time click analytics, built on **Java**, **Spring Boot**, **JSP**, and **Redis**.

MicroURL turns long links into compact, shareable codes, tracks every redirect, and exposes analytics through both a modern web UI and a JSON API. Duplicate long URLs are detected automatically — the same link always resolves to the same short code.

---

## Features

- **URL shortening** — Accepts `http` and `https` URLs; generates cryptographically random 7-character codes
- **Smart deduplication** — Submitting an existing long URL returns the original short link (`200 OK`), not a new one
- **302 redirects** — Fast lookup and redirect via Redis
- **Click analytics** — Total clicks and UTC daily breakdown per link
- **Dashboard** — All-links overview page with per-link detail views
- **REST API** — Full JSON API for create, analytics, and delete operations
- **Input validation** — Rejects blank, malformed, and unsafe protocols (`javascript:`, `file:`, `ftp:`, etc.)
- **Automated tests** — Integration tests with Testcontainers and MockMvc

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3, Spring MVC |
| Views | JSP, JSTL |
| Data store | Redis 8 (Spring Data Redis, Lettuce) |
| Build | Maven (WAR packaging) |
| Tests | JUnit 5, Spring Boot Test, MockMvc, Testcontainers |
| Local infra | Docker Compose |

---

## Architecture

```
Browser (JSP UI)
       │
       ▼
Spring MVC Controllers
  ├── HomeController        → pages (home, analytics)
  ├── UrlShortenerController → JSON API
  └── RedirectController    → 302 redirects
       │
       ▼
UrlShortenerService
  ├── validate URL
  ├── deduplicate by original URL
  ├── generate / resolve short codes
  ├── record click analytics
  └── delete link records
       │
       ▼
StringRedisTemplate → Redis
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 17 or higher |
| Maven | 3.9+ |
| Docker | Latest stable |
| Docker Compose | v2+ |

Verify your environment:

```bash
java -version
mvn -version
docker --version
docker compose version
```

> **Note:** If Maven uses an older Java version, set `JAVA_HOME` before building:
>
> ```bash
> export JAVA_HOME=/usr/lib/jvm/java-19-openjdk-amd64
> ```

---

## Quick Start

### 1. Clone and enter the project

```bash
cd Redis_URL_Shortener
```

### 2. Start Redis

```bash
docker compose up -d
docker exec -it redis-url-shortener-db redis-cli ping
```

Expected output: `PONG`

Redis runs on host port **6382** (mapped to container port 6379).

### 3. Run the application

```bash
mvn spring-boot:run
```

Open the app in your browser:

**[http://localhost:8081](http://localhost:8081)**

### 4. Build and test

```bash
mvn test
mvn clean package
```

The deployable WAR is written to:

```
target/redis-url-shortener-1.0.0-SNAPSHOT.war
```

---

## Configuration

Settings are defined in `src/main/resources/application.properties` and can be overridden via environment variables.

| Property / Env Var | Default | Description |
|---|---|---|
| `server.port` | `8081` | HTTP port for the embedded Tomcat server |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6382` | Redis port |
| `APP_BASE_URL` | `http://localhost:8081` | Base URL used when building short links |

Example with custom values:

```bash
REDIS_PORT=6382 APP_BASE_URL=http://localhost:8081 mvn spring-boot:run
```

---

## Web Pages

| URL | Description |
|---|---|
| `/` | Home — shorten a URL, copy result, view analytics |
| `/analytics` | Dashboard — all shortened links with click totals |
| `/analytics/{code}` | Detail view — metadata and daily click breakdown |
| `/{code}` | Redirect — resolves to the original URL (HTTP 302) |

---

## REST API

### Create a short URL

```http
POST /api/urls
Content-Type: application/json

{
  "url": "https://redis.io/docs/latest/"
}
```

**Response — new link (`201 Created`):**

```json
{
  "code": "aB7xQ2m",
  "originalUrl": "https://redis.io/docs/latest/",
  "shortUrl": "http://localhost:8081/aB7xQ2m"
}
```

**Response — existing link (`200 OK`):**

Same JSON body. The original short code is returned when the URL was already shortened.

---

### Get analytics for one link

```http
GET /api/urls/{code}/analytics
```

```json
{
  "code": "aB7xQ2m",
  "originalUrl": "https://redis.io/docs/latest/",
  "shortUrl": "http://localhost:8081/aB7xQ2m",
  "createdAt": "2026-06-10T08:00:00Z",
  "totalClicks": 3,
  "dailyClicks": {
    "2026-06-10": 3
  }
}
```

---

### Get analytics for all links

```http
GET /api/urls/analytics
```

Returns a JSON array of analytics objects (one entry per unique original URL).

---

### Delete a short URL

```http
DELETE /api/urls/{code}
```

Returns `204 No Content` on success.

---

### API summary

| Method | Path | Status | Description |
|---|---|---|---|
| `POST` | `/api/urls` | `201` / `200` | Create or return existing short URL |
| `GET` | `/api/urls/analytics` | `200` | All links analytics |
| `GET` | `/api/urls/{code}/analytics` | `200` | Single link analytics |
| `DELETE` | `/api/urls/{code}` | `204` | Delete link and all related data |
| `GET` | `/{code}` | `302` | Redirect to original URL |

---

## curl Examples

```bash
# Create a short URL
curl -s -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://redis.io/docs/latest/"}'

# Submit the same URL again (returns existing code with 200)
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://redis.io/docs/latest/"}'

# Follow a redirect
curl -i http://localhost:8081/CODE

# Single-link analytics
curl -s http://localhost:8081/api/urls/CODE/analytics | jq

# All-links analytics
curl -s http://localhost:8081/api/urls/analytics | jq

# Delete a link
curl -i -X DELETE http://localhost:8081/api/urls/CODE
```

Replace `CODE` with the 7-character code returned by the create endpoint.

---

## Redis Data Model

All data is stored as strings and hashes via `StringRedisTemplate` — no Java serialization.

| Key | Type | Example | Purpose |
|---|---|---|---|
| `url:{code}` | String | `url:aB7xQ2m` | Original long URL |
| `metadata:{code}` | Hash | `createdAt` | UTC creation timestamp (ISO-8601) |
| `analytics:{code}` | Hash | `totalClicks` | Lifetime click counter |
| `analytics:{code}:daily` | Hash | `2026-06-10` | UTC daily click counts |
| `lookup:{sha256}` | String | → `aB7xQ2m` | Reverse index for URL deduplication |
| `urls:index` | Set | `{aB7xQ2m, …}` | Active short codes |

### Inspect Redis

```bash
docker exec -it redis-url-shortener-db redis-cli -p 6379
```

```redis
SCAN 0 MATCH "url:*"
GET url:CODE
HGETALL metadata:CODE
HGETALL analytics:CODE
HGETALL analytics:CODE:daily
GET lookup:SHA256_HASH
SMEMBERS urls:index
```

> Inside the container, Redis listens on port **6379**. From the host machine, connect on port **6382**.

---

## URL Deduplication

MicroURL ensures one short code per unique long URL:

1. Check the `lookup:{sha256}` reverse index
2. If missing, scan existing `url:*` keys (covers links created before deduplication was added)
3. Return the **oldest** matching code and backfill the lookup index
4. Only generate a new code when no match exists

The web UI displays **"Existing link returned"** when a duplicate is detected.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/urlshortener/
│   │   ├── UrlShortenerApplication.java
│   │   ├── config/          # Web MVC configuration
│   │   ├── controller/      # Home, API, and redirect controllers
│   │   ├── dto/             # Request / response contracts
│   │   ├── exception/       # Global error handling
│   │   ├── service/         # Business logic and Redis operations
│   │   └── util/            # Code generator and URL validator
│   ├── resources/
│   │   ├── application.properties
│   │   └── static/          # CSS, JS, images
│   └── webapp/WEB-INF/views/  # JSP templates
└── test/
    └── java/.../UrlShortenerApplicationTests.java
```

---

## Testing

The test suite covers:

- Application context loading
- Valid URL creation (`201`)
- Duplicate URL detection (`200`)
- Legacy entry deduplication (pre-lookup data)
- Blank and invalid protocol rejection
- Redirect (`302`) with correct `Location` header
- Unknown code handling (`404`)
- Analytics increment on redirect
- Analytics API responses
- Key cleanup on deletion
- All-links analytics page and API

```bash
export JAVA_HOME=/usr/lib/jvm/java-19-openjdk-amd64   # if needed
mvn test
```

---

## Deployment (Render)

This is a **Java / Maven** application — not Node.js. On Render, use **Docker** or the **Java** runtime. The app also requires a **managed Redis** instance (Render does not bundle Redis with web services).

### Step 1 — Push code to GitHub

```bash
git add .
git commit -m "Add deployment configuration"
git push origin master
```

### Step 2 — Create a managed Redis database

Use a free cloud Redis provider and note the connection details:

| Provider | Notes |
|---|---|
| [Upstash](https://upstash.com/) | Free tier, TLS supported — set `REDIS_SSL=true` |
| [Redis Cloud](https://redis.io/cloud/) | Free tier available |
| Any Redis 6+ host | Must be reachable from Render |

### Step 3 — Deploy on Render

#### Option A — Docker (recommended)

| Setting | Value |
|---|---|
| **Language** | `Docker` |
| **Branch** | `master` |
| **Root Directory** | *(leave empty)* |
| **Dockerfile Path** | `./Dockerfile` |
| **Instance Type** | Free |

Or use the included blueprint — in Render Dashboard: **New → Blueprint** and point to `render.yaml`.

#### Option B — Native Java

| Setting | Value |
|---|---|
| **Language** | `Java` |
| **Branch** | `master` |
| **Root Directory** | *(leave empty)* |
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar target/redis-url-shortener-1.0.0-SNAPSHOT.war` |
| **Instance Type** | Free |

> Do **not** use `yarn` or `yarn start` — those are for Node.js projects.

### Step 4 — Environment variables

Set these in **Render → your service → Environment**:

| Variable | Example | Required |
|---|---|---|
| `APP_BASE_URL` | `https://url-shortener.onrender.com` | Yes — **your real** Render URL from the dashboard (not a placeholder) |
| `REDIS_HOST` | `turkey-jewel-argent-68220.db.redis.io` | Yes — from Redis Cloud **Public endpoint** |
| `REDIS_PORT` | `14836` | Yes — port from **Public endpoint** |
| `REDIS_PASSWORD` | *(from Redis Cloud Connect dialog)* | Yes |
| `REDIS_SSL` | `false` | `true` for Upstash; `false` for Redis Cloud public endpoint on port 14836 |

**Redis Cloud:** Configuration → copy **Public endpoint** (`host:port`). Password → click **Connect** → copy from the connection string.

**Local dev:** copy `.env.example` to `.env`, fill in values, then run `./run.sh`.

Render injects `PORT` automatically — the app reads it via `server.port=${PORT:8081}`.

### Step 5 — Verify deployment

Find your real URL in **Render → your service → top of page** (e.g. `https://url-shortener.onrender.com`).  
Do **not** use placeholder text like `your-actual-app.onrender.com`.

```bash
# Replace with YOUR real Render URL from the dashboard
export RENDER_URL=https://url-shortener.onrender.com

# Must return 200 (if you see "Not Found" + x-render-routing: no-server, the app is not running)
curl -sI "$RENDER_URL/" | head -5

# Create a short URL
curl -s -X POST "$RENDER_URL/api/urls" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://redis.io/docs/latest/"}'

# Test redirect (use the code from the response above)
curl -sI "$RENDER_URL/CODE_HERE"
```

### Docker (local production test)

```bash
docker build -t microurl .
docker run -p 8081:8081 \
  -e REDIS_HOST=host.docker.internal \
  -e REDIS_PORT=6382 \
  -e APP_BASE_URL=http://localhost:8081 \
  microurl
```

---

## Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| `Port 8081 already in use` | Another process on the port | Stop the conflicting process or set `--server.port=8082` |
| `Port 6382 already in use` | Redis port conflict | Change the host port in `compose.yaml` and update `REDIS_PORT` |
| `Could not connect to Redis` | Redis container not running | Run `docker compose up -d` and verify with `redis-cli ping` |
| `release version 17 not supported` | Maven using old Java | Set `JAVA_HOME` to Java 17+ |
| Duplicate URL still creates new code | App not restarted after update | Restart the app; the fallback scan handles legacy entries automatically |
| Render build fails with `yarn` | Wrong language selected | Switch runtime to **Docker** or **Java**, not Node |
| Render app crashes on start | Redis not reachable | Verify `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, and `REDIS_SSL` |
| Short URLs show `localhost` | `APP_BASE_URL` not set | Set `APP_BASE_URL` to your public Render URL |

---

## License

This project is provided for educational and development purposes.
