# WiFiRent BD - Windows PowerShell Setup

## Prerequisites

### 1. Install Java 21
Download from https://adoptium.net/ or use your preferred JDK distributor.
Verify: `java -version`

### 2. Install PostgreSQL
Download from https://www.postgresql.org/ or use the E:\Postgre folder if available.
Create database `wifirent_bd` and user `wifirent`.

### 3. Verify Maven Wrapper
The project includes `./mvnw` - no system Maven installation needed.

## Quick Start

```powershell
# 1. Navigate to project
cd E:\wifirent-bd\backend

# 2. Copy environment template
Copy-Item .env.example .env

# 3. Edit .env with your credentials
notepad .env

# 4. Start PostgreSQL (if installed as service)
net start postgresql

# 5. Run the backend
./mvnw spring-boot:run

# 6. In another terminal, run tests
./mvnw test
```

## Troubleshooting

### Port 8080 already in use
```powershell
netstat -ano | findstr :8080
# Kill the process
taskkill /PID <pid> /F
```

### Build fails
```powershell
./mvnw clean compile
```

### Database connection fails
Verify PostgreSQL is running and credentials in .env match.

## Project Structure

```
backend\
├── mvnw                   # Maven wrapper
├── pom.xml                # Build configuration
├── .env.example           # Environment template
├── src\
│   ├── main\
│   │   ├── java\com\wifirent\bd\
│   │   │   ├── auth\              # JWT authentication
│   │   │   ├── config\            # Security & JPA config
│   │   │   ├── controller\        # REST controllers
│   │   │   ├── domain\            # JPA entities
│   │   │   ├── dto\               # Data transfer objects
│   │   │   ├── exception\         # Global exception handler
│   │   │   ├── gateway\           # Gateway adapter interface + mock
│   │   │   ├── payment\           # Payment enums
│   │   │   ├── security\          # JWT filter
│   │   │   └── service\           # Business services
│   │   └── resources\
│   │       ├── application.properties
│   │       └── db\migration\V1__initial_schema.sql
│   └── test\
│       └── java\com\wifirent\bd\
│           ├── auth\AuthServiceTest.java
│           ├── service\*Test.java
│           └── gateway\MockGatewayAdapterTest.java
└── target\                # Build output
```

## Test Results

37 unit tests covering:
- Authentication (register, login)
- Role-based access control
- Hotspot CRUD and nearby search
- Package validation (time/data/combined)
- Rental order lifecycle
- Payment processing (mock, idempotency, duplicates)
- Session management
- Gateway mock adapter
- Host earnings ledger
