# WiFiRent BD - Backend Setup

## Overview
Spring Boot 3.5.0 backend with Java 21, PostgreSQL, Flyway, Spring Security JWT.

## Prerequisites
- Java 21 JDK
- Maven (or use ./mvnw wrapper)
- PostgreSQL 14+

## Configuration

Copy `.env.example` to `.env` and fill in values:
```powershell
Copy-Item .env.example .env
# Edit .env with notepad or your editor
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DB_URL | jdbc:postgresql://localhost:5432/wifirent_bd | PostgreSQL JDBC URL |
| DB_USERNAME | wifirent | Database username |
| DB_PASSWORD | password | Database password |
| JWT_SECRET | change-me-in-production | JWT signing secret (min 32 chars) |
| CORS_ALLOWED_ORIGINS | http://localhost:* | Allowed CORS origins |
| GATEWAY_MODE | mock | Gateway adapter mode (mock) |
| PAYMENT_MODE | mock | Payment provider mode (mock) |

## Running on Windows (PowerShell)

### 1. Start PostgreSQL
Ensure PostgreSQL is running on localhost:5432 with the configured database.

### 2. Run the backend
```powershell
cd E:\wifirent-bd\backend
./mvnw spring-boot:run
```

### 3. Run with custom profile
```powershell
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

### 4. Run tests
```powershell
./mvnw test
```

### 5. Build JAR
```powershell
./mvnw clean package
```

## Application Profiles

- **local** (default): H2 not used, connects to PostgreSQL via application.properties
- **test**: Uses H2 in-memory database via application-test.properties

## Security

- Passwords hashed with BCrypt
- JWT access tokens (15 min expiry) + refresh tokens (7 day expiry)
- Role-based authorization (CUSTOMER, HOST, ADMIN)
- Gateway credentials never returned to clients
- CORS configurable via environment

## Actuator Endpoints

- http://localhost:8081/actuator/health
- http://localhost:8081/actuator/info

Restricted to health and info only.

## Database Migrations

Flyway manages schema. Migration file:
- `src/main/resources/db/migration/V1__initial_schema.sql`

To reset schema, drop and recreate the database, then restart.

## Mock Mode

Both gateway and payment are in mock mode by default. This is for development only:
- MockPaymentProvider simulates payments (NOT real money)
- MockGatewayAdapter simulates gateway authorization (NOT real network control)

Never use mock mode in production.
