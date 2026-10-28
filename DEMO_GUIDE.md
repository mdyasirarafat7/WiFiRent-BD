# WiFiRent BD - Demo Guide for University Presentation

## IMPORTANT: This is a DEMO-ONLY System

All credits are **virtual demo credits** with no real monetary value.
No real payment gateways (bKash, Nagad, Stripe, cards, bank APIs) are integrated.
Demo credits cannot be withdrawn or redeemed for cash.

---

## 1. Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Java (JDK) | 21+ | Backend runtime |
| Maven | 3.8+ (wrapper included) | Build tool |
| PostgreSQL | 14+ | Database |
| Flutter SDK | Any stable | Mobile frontend (optional) |
| Git | Any | Version control |

---

## 2. Configure PostgreSQL on Windows

### Option A: Install PostgreSQL locally

1. Download PostgreSQL from https://www.postgresql.org/download/windows/
2. Install with default settings
3. Remember the `postgres` superuser password you set during installation

### Option B: Use Docker (if available)

```powershell
cd E:\wifirent-bd
docker-compose up -d
```

---

## 3. Create the Database

Open PowerShell as Administrator and run:

```powershell
# Connect to PostgreSQL as the postgres superuser
# Replace 'your_postgres_password' with the password you set during installation
psql -U postgres -c "CREATE DATABASE wifirent_bd;"
psql -U postgres -c "CREATE USER wifirent WITH PASSWORD 'wifirent_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE wifirent_bd TO wifirent;"
psql -U postgres -c "GRANT ALL ON SCHEMA public TO wifirent;"
```

If you get a connection error, you may need to:
- Start the PostgreSQL service: `net start postgresql`
- Or use the PostgreSQL GUI tool (pgAdmin)

---

## 4. Configure Environment Variables

### 4.1 Copy the example file

```powershell
cd E:\wifirent-bd\backend
Copy-Item .env.example .env
```

### 4.2 Edit .env

Open `.env` in Notepad and update the values:

```text
# Database (update password to match what you set in step 3)
DB_URL=jdbc:postgresql://localhost:5432/wifirent_bd
DB_USERNAME=wifirent
DB_PASSWORD=wifirent_password

# JWT secret (change this to a random string for the demo)
JWT_SECRET=demo-secret-key-for-university-presentation-2026

# CORS (allow localhost for demo)
CORS_ALLOWED_ORIGINS=http://localhost:*

# Webhook secret (not used in demo - payments are demo-only)
WEBHOOK_SECRET=demo-webhook-secret

# Scheduler delay (milliseconds)
SCHEDULER_EXPIRATION_DELAY_MS=60000

# Demo wallet configuration
WALLET_INITIAL_DEMO_BALANCE=5000
WALLET_PLATFORM_COMMISSION_RATE_BPS=100
```

Save the file.

---

## 5. Start the Backend

```powershell
cd E:\wifirent-bd\backend
./mvnw spring-boot:run
```

Wait for the output to show something like:
```
Started WifirentBdApplication in X seconds
```

The API will be available at: **http://localhost:8080/api**

Actuator health check: **http://localhost:8081/actuator/health**

---

## 6. Test the Backend API (Without Frontend)

You can test all demo workflows using PowerShell's `Invoke-RestMethod` or any API client (Postman, Insomnia, etc.).

### Step 1: Register a new customer

```powershell
$body = @{
    name = "Demo Customer"
    email = "customer@demo.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body $body
```

Save the `accessToken` from the response - you'll need it for subsequent requests.

### Step 2: Log in

```powershell
$body = @{
    email = "customer@demo.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body $body
```

### Step 3: Check wallet balance (requires auth token)

```powershell
$token = "YOUR_ACCESS_TOKEN_FROM_LOGIN"

Invoke-RestMethod -Uri "http://localhost:8080/api/wallet/balance" -Method Get -Headers @{Authorization = "Bearer $token"}
```

You should see `5000` demo credits (the initial balance).

### Step 4: Browse hotspots (public endpoint, no auth needed)

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/hotspots" -Method Get
```

Initially there will be no hotspots. You need to create one as a host.

### Step 5: Create a host user and hotspot

First, register a host user:

```powershell
$body = @{
    name = "Demo Host"
    email = "host@demo.com"
    password = "password123"
} | ConvertTo-Json

$hostResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body $body
$hostToken = $hostResponse.accessToken
```

Then create a hotspot (you need to be logged in as the host):

```powershell
$body = @{
    name = "University Cafe Hotspot"
    description = "Free Wi-Fi near the computer science building"
    latitude = 23.8103
    longitude = 90.4125
    ssid = "UniCafe-WiFi"
    approximateArea = "Computer Science Building"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/host/hotspots" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $hostToken"} -Body $body
```

Note the hotspot ID from the response.

### Step 6: Create a package for the hotspot

```powershell
$body = @{
    priceMinorUnits = 500
    packageType = "TIME_BASED"
    durationMinutes = 60
    dataQuotaBytes = null
    validityMinutes = null
    speedLimitKbps = null
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/host/hotspots/HOTSPOT_ID/packages" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $hostToken"} -Body $body
```

Replace `HOTSPOT_ID` with the actual ID from step 5. Note the package ID from the response.

### Step 7: Purchase a package as a customer (uses demo credits)

```powershell
$body = @{
    hotspotId = HOTSPOT_ID
    packageId = PACKAGE_ID
    idempotencyKey = "purchase-001"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/wallet/purchase" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $customerToken"} -Body $body
```

Replace `HOTSPOT_ID` and `PACKAGE_ID` with actual IDs. Replace `$customerToken` with the customer's access token.

### Step 8: Verify balance decreased

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/wallet/balance" -Method Get -Headers @{Authorization = "Bearer $customerToken"}
```

The balance should be 5000 - 500 = 4500 demo credits.

### Step 9: Check wallet transaction history

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/wallet/transactions" -Method Get -Headers @{Authorization = "Bearer $customerToken"}
```

You should see a PURCHASE transaction showing the debit.

---

## 7. Frontend Situation

**There is NO React + TypeScript frontend in this repository.**

The repository contains only:
- **Backend**: Spring Boot Java 21 API (fully working)
- **Frontend**: Flutter mobile app (Dart) - SDK not available on this system

### Quickest viable routes for a demo:

**Option A: Test the API directly (recommended for immediate demo)**
Use PowerShell `Invoke-RestMethod` or Postman/Insomnia to call the backend endpoints as shown in section 6. This demonstrates all backend functionality without needing a frontend.

**Option B: Install Flutter SDK and run the mobile app**
1. Download Flutter SDK from https://docs.flutter.dev/get-started/install
2. Extract to a folder (e.g., `C:\flutter`)
3. Add `C:\flutter\bin` to your PATH
4. Run `flutter doctor` to verify installation
5. Run `flutter pub get` in `E:\wifirent-bd\frontend\wifirent_app`
6. Run `flutter run` to start the app on an emulator or device

The Flutter app already has screens for:
- Splash, Login, Register
- Customer home with wallet balance
- Wallet top-up and transaction history
- Package purchase

---

## 8. Sample Data for Demo

To make the demo look complete, create:
1. **1 customer user** (register normally)
2. **1 host user** (register normally)
3. **1-2 hotspots** owned by the host (with realistic names like "Campus Library Wi-Fi")
4. **2-3 packages** per hotspot (e.g., ৳5 for 1 hour, ৳10 for 1 GB, ৳50 for 24 hours)
5. **Initial demo balance**: 5000 credits per user (automatically granted on registration)

---

## 9. Common Errors and Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `Connection refused: localhost:5432` | PostgreSQL not running | `net start postgresql` or start Docker container |
| `Authentication failed` | Wrong DB credentials | Check `.env` DB_USERNAME and DB_PASSWORD |
| `Flyway validation failed` | Migration mismatch | Drop the database and recreate it |
| `401 Unauthorized` | Missing or expired JWT token | Log in again and use the new access token |
| `403 Forbidden` | Wrong role for endpoint | Ensure you're using the correct user type (customer vs host vs admin) |
| `Insufficient demo credits` | Not enough wallet balance | Ask admin to top up, or register a new user |

---

## 10. What Is Simulated vs. Real

| Feature | Status | Notes |
|---------|--------|-------|
| User registration/login | Real | JWT authentication works |
| Wallet balance | Real (demo credits) | Stored in PostgreSQL, immutable ledger |
| Package purchase | Real (demo credits) | Atomic transaction, balance updates correctly |
| Hotspot discovery | Real | Backend API returns hotspot data |
| Wi-Fi access control | Simulated | Mock gateway adapter only, no real router |
| Payment processing | Simulated | No real money, demo credits only |
| Captive portal | Not implemented | Requires OpenWrt router + openNDS |
| Real money payouts | Not implemented | Demo credits are not withdrawable |

---

## 11. Exact Demo Steps for Teacher Presentation

1. **Start the backend**: `./mvnw spring-boot:run` in `E:\wifirent-bd\backend`
2. **Register a customer**: POST `/api/auth/register`
3. **Log in**: POST `/api/auth/login`, save the access token
4. **Show wallet balance**: GET `/api/wallet/balance` (shows 5000 demo credits)
5. **Browse hotspots**: GET `/api/hotspots` (create some first as a host)
6. **Create a host account and hotspot**: Register as host, POST `/api/host/hotspots`
7. **Create a package**: POST `/api/host/hotspots/{id}/packages`
8. **Purchase a package**: POST `/api/wallet/purchase` (show balance decrease)
9. **Show transaction history**: GET `/api/wallet/transactions`
10. **Show insufficient balance error**: Try to purchase something expensive

All balances are labeled "DEMO_CREDITS" and include the note: "Demo credits - no real monetary value, not withdrawable."

---

## 12. Backend URLs

| Service | URL |
|---------|-----|
| API Base | http://localhost:8080/api |
| Health Check | http://localhost:8081/actuator/health |
| Swagger/OpenAPI | Not configured |

---

## 13. Files Changed in This Project

- **Backend**: All source files under `E:\wifirent-bd\backend\src\main\java\com\wifirent\bd\`
- **Migrations**: `V1__initial_schema.sql`, `V2__add_token_version.sql`, `V3__add_wallet_and_transactions.sql`
- **Configuration**: `application.properties`, `.env.example`
- **Tests**: 73 unit tests under `src/test/java/com/wifirent/bd/`
- **Frontend**: Flutter app under `E:\wifirent-bd\frontend\wifirent_app\` (SDK not available to compile)
- **Documentation**: `README.md`, `DEMO_GUIDE.md` (this file), `SECURITY_CHECKLIST.md`, `IMPLEMENTATION_STATUS.md`, `WINDOWS_SETUP.md`
