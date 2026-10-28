# WiFiRent BD - Wi-Fi Rental Marketplace

**IMPORTANT: This is a DEMO-ONLY system using internal virtual credits. No real payment gateways are integrated. No real money is involved. Demo credits cannot be withdrawn or redeemed for cash.**

WiFiRent BD is a marketplace where people with existing home broadband connections can offer paid internet access to nearby customers through a managed Wi-Fi gateway.

---

## Project Structure

```
E:\wifirent-bd\
├── IMPLEMENTATION_STATUS.md     # Implementation progress tracking
├── SECURITY_CHECKLIST.md        # Security audit results (demo-only marking)
├── README.md                    # This file
├── ROUTER_INTEGRATION_GUIDE.md  # Router integration documentation
├── backend\                     # Spring Boot backend (Java 21 + Spring Boot 3.5)
│   ├── pom.xml                  # Maven build file
│   ├── .env.example             # Environment variables template
│   ├── src\
│   │   ├── main\                # Production source
│   │   │   ├── com\wifirent\bd\
│   │   │   │   ├── auth\                # JWT authentication
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── AppUserDetails.java
│   │   │   │   │   ├── AppUserDetailsService.java
│   │   │   │   │   └── JwtService.java
│   │   │   │   ├── config\                # Security & JPA config
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── JpaAuditConfig.java
│   │   │   │   │   └── DataSeeder.java
│   │   │   │   ├── controller\        # REST controllers
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── PublicHotspotController.java
│   │   │   │   │   ├── HostHotspotController.java
│   │   │   │   │   ├── PackageController.java
│   │   │   │   │   ├── RentalController.java
│   │   │   │   │   ├── WalletController.java
│   │   │   │   │   ├── GatewayController.java
│   │   │   │   │   ├── PaymentController.java
│   │   │   │   │   ├── SessionController.java
│   │   │   │   │   ├── HostEarningsController.java
│   │   │   │   │   └── AdminController.java
│   │   │   │   ├── domain\            # JPA entities
│   │   │   │   │   ├── BaseEntity.java
│   │   │   │   │   ├── user\              # User, HostProfile
│   │   │   │   │   ├── hotspot\           # Hotspot, GatewayDevice, GatewayCapability, GatewayIntegrationMode
│   │   │   │   │   ├── packageplan\       # PackagePlan, PackageType
│   │   │   │   │   ├── rental\            # RentalOrder, RentalOrderStatus
│   │   │   │   │   ├── payment\           # Payment, PaymentStatus, PaymentProvider, Refund, RefundStatus
│   │   │   │   │   ├── session\           # RentalSession, SessionStatus, UsageRecord
│   │   │   │   │   └── wallet\            # Wallet, WalletTransaction
│   │   │   │   ├── dto\                 # Data Transfer Objects
│   │   │   │   │   ├── AuthResponse, LoginRequest, RegisterRequest, MeResponse
│   │   │   │   │   ├── HotspotDto, PackagePlanDto, RentalOrderDto, SessionDto
│   │   │   │   │   ├── WalletBalanceDto, WalletTransactionDto, WalletTransactionListDto
│   │   │   │   │   ├── CreateRentalOrderRequest, PurchaseWithWalletRequest, PurchaseWithWalletResponse
│   │   │   │   │   ├── WalletTopUpRequest, WalletTransferRequest, WalletLedgerEntryDto
│   │   │   │   │   ├── GatewayRegisterRequest, GatewayDeviceDto, GatewayHealthDto
│   │   │   │   │   └── (and more)
│   │   │   │   ├── exception\           # Global exception handling
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── ErrorResponse.java
│   │   │   │   │   ├── BusinessException, ResourceNotFoundException, etc.
│   │   │   │   ├── gateway\             # Gateway integration
│   │   │   │   │   ├── GatewayAdapter.java
│   │   │   │   │   ├── MockGatewayAdapter.java
│   │   │   │   │   └── OpenWrtGatewayAdapter.java
│   │   │   │   ├── gateway\             # Gateway domain model
│   │   │   │   │   └── GatewayDevice.java
│   │   │   │   ├── payment\             # Payment processing
│   │   │   │   │   └── (entities)
│   │   │   │   └── wallet\              # Wallet & transactions
│   │   │   │   └── security\            # JWT security
│   │   │   │       └── JwtAuthFilter.java
│   │   │   │   ├── service\             # Business services
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── JwtService.java
│   │   │   │   │   ├── HotspotService.java
│   │   │   │   │   ├── PackageService.java
│   │   │   │   │   ├── RentalService.java
│   │   │   │   │   ├── WalletService.java
│   │   │   │   │   ├── SessionService.java
│   │   │   │   │   ├── HostEarningsService.java
│   │   │   │   │   ├── AdminService.java
│   │   │   │   │   ├── SessionExpirationScheduler.java
│   │   │   │   │   └── GatewayService.java
│   │   │   │   └── WifirentBdApplication.java
│   │   │   └── resources\
│   │   │   │       ├── application.properties
│   │   │   │       └── db\migration\
│   │   │   │           ├── V1__initial_schema.sql
│   │   │   │           ├── V2__add_token_version.sql
│   │   │   │           └── V3__add_wallet_and_transactions.sql
│   │   │   │           └── V4__add_gateway_capabilities.sql
│   │   │   └── test\
│   │   │   │       └── java\
│   │   │   │           └── com\wifirent\bd\service\* (73 tests, all passing)
│   │   └── pom.xml
│   └── .env.example
├── frontend\                    # Frontend applications
│   ├── react-app\               # React + TypeScript + Vite web frontend
│   │   ├── package.json         # npm dependencies
│   │   ├── vite.config.ts       # Vite config (proxies /api to localhost:8080)
│   │   ├── .env                 # VITE_API_URL environment variable
│   │   ├── tsconfig.json        # TypeScript config
│   │   ├── tailwind.config.js   # Tailwind CSS config (light theme)
│   │   ├── index.html           # HTML entry
│   │   └── src\
│   │       ├── main.tsx         # React entry point
│   │       ├── App.tsx          # Router configuration
│   │       ├── index.css        # Tailwind CSS + component styles (light theme)
│   │       ├── api\             # Centralized Axios API client
│   │       │   └── apiClient.ts # All API methods + JWT refresh logic
│   │       ├── context\         # React context providers
│   │       │   └── AuthContext.tsx # JWT auth state management
│   │       ├── components\      # Reusable UI components
│   │       │   ├── Layout.tsx   # Layout, Navbar, ProtectedRoute, RoleRoute
│   │       │   └── HotspotCard.tsx # HotspotCard, PackageCard, WalletBalance
│   │       ├── lib\             # Utility functions
│   │       │   └── utils.ts     # formatCurrency, formatBytes, getStatusColor
│   │       ├── pages\           # Page components (7 pages)
│   │       │   ├── LoginPage.tsx
│   │       │   ├── RegisterPage.tsx
│   │       │   ├── RentPage.tsx
│   │       │   ├── AccessPage.tsx
│   │       │   ├── HostDashboardPage.tsx
│   │       │   ├── ProfilePage.tsx
│   │       │   └── NotFoundPage.tsx
│   │       └── types\           # TypeScript type definitions
│   │           └── index.ts     # All API request/response types
│   └── wifirent_app\            # Flutter mobile app (reference only, not compiled)
│       ├── pubspec.yaml
│   │   ├── lib\
│   │   └── android\
```

---

## Backend API Mapping (All Endpoints Verified)

### Authentication (Public)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/auth/register` | POST | RegisterRequest | AuthResponse | No |
| `/api/auth/login` | POST | LoginRequest | AuthResponse | No |
| `/api/auth/refresh` | POST | TokenRefreshRequest | AuthResponse | No |
| `/api/auth/logout` | POST | - | 204 | Yes |
| `/api/auth/me` | GET | - | MeResponse | Yes |

### Hotspots (Public)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/hotspots` | GET | Pageable | PagedHotspotResponse | No |
| `/api/hotspots/{id}` | GET | - | HotspotDto | No |
| `/api/hotspots/nearby` | GET | lat, lng, radiusMeters | PagedHotspotResponse | No |
| `/api/hotspots/{id}/packages` | GET | - | Page<PackagePlanDto> | No |

### Host Hotspots (HOST/ADMIN)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/host/hotspots` | GET | Pageable | PagedHotspotResponse | Yes |
| `/api/host/hotspots` | POST | CreateHotspotRequest | HotspotDto | Yes |
| `/api/host/hotspots/{id}` | PUT | UpdateHotspotRequest | HotspotDto | Yes |
| `/api/host/hotspots/{id}/availability` | PATCH | AvailabilityRequest | HotspotDto | Yes |

### Packages (HOST/ADMIN)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/host/packages/hotspots/{hotspotId}` | POST | CreatePackageRequest | PackagePlanDto | Yes |
| `/api/host/packages/{packageId}` | PUT | UpdatePackageRequest | PackagePlanDto | Yes |
| `/api/host/packages/{packageId}` | DELETE | - | 204 | ADMIN |

### Demo Wallet (CUSTOMER/HOST/ADMIN)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/wallet/balance` | GET | - | WalletBalanceDto | Yes |
| `/api/wallet/topup` | POST | WalletTopUpRequest | WalletBalanceDto | ADMIN |
| `/api/wallet/transfer` | POST | WalletTransferRequest | WalletBalanceDto | Yes |
| `/api/wallet/transactions` | GET | Pageable | WalletTransactionListDto | Yes |
| `/api/wallet/purchase` | POST | PurchaseWithWalletRequest | PurchaseWithWalletResponse | CUSTOMER |
| `/api/wallet/refund` | POST | RefundRequest | RefundResponse | ADMIN |

### Sessions (CUSTOMER/HOST/ADMIN)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/sessions/active` | GET | - | ActiveSessionResponse | Yes |
| `/api/sessions/{id}` | GET | - | SessionDto | Yes |
| `/api/sessions/history` | GET | Pageable | Paged list | Yes |

### Rentals (CUSTOMER/HOST/ADMIN)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/rentals` | POST | CreateRentalOrderRequest | RentalOrderDto | Yes |
| `/api/rentals` | GET | Pageable | Paged list | Yes |
| `/api/rentals/{id}` | GET | - | RentalOrderDto | Yes |
| `/api/rentals/{id}/cancel` | POST | CancelRentalRequest | RentalOrderDto | Yes |

### Host Earnings (HOST/ADMIN)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/host/earnings` | GET | - | HostEarningsDto | Yes |
| `/api/host/ledger` | GET | Pageable | Paged HostLedgerEntryDto | Yes |
| `/api/host/payouts` | GET | Pageable | Paged list | Yes |

### Gateways (HOST/ADMIN)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/gateways/register` | POST | GatewayRegisterRequest | GatewaySessionResponse | HOST/ADMIN |
| `/api/gateways/{deviceId}/heartbeat` | POST | GatewayHeartbeatRequest | 204 | HOST/ADMIN |
| `/api/gateways/{deviceId}/events` | POST | String payload | 204 | HOST/ADMIN |
| `/api/gateways/{deviceId}/sessions/{sessionId}/authorize` | POST | GatewaySessionAuthorizeRequest | GatewaySessionResponse | HOST/ADMIN |
| `/api/gateways/{deviceId}/sessions/{sessionId}/revoke` | POST | - | 204 | HOST/ADMIN |
| `/api/gateways` | GET | - | List<GatewayDeviceDto> | HOST/ADMIN |
| `/api/gateways/{deviceId}` | GET | - | GatewayDeviceDto | HOST/ADMIN |
| `/api/gateways/{deviceId}/health` | GET | - | GatewayHealthDto | HOST/ADMIN |
| `/api/gateways/{deviceId}/test-connection` | POST | - | 204 | HOST/ADMIN |
| `/api/gateways/capabilities` | GET | - | String[] | HOST/ADMIN |

### Admin (ADMIN only)
| Endpoint | Method | Request | Response | Auth |
|----------|--------|---------|----------|------|
| `/api/admin/users` | GET | Pageable | Paged list | ADMIN |
| `/api/admin/hotspots` | GET | Pageable | PagedHotspotResponse | ADMIN |
| `/api/admin/hotspots/{id}/status` | PATCH | status param | HotspotDto | ADMIN |
| `/api/admin/payments` | GET | Pageable | Paged list | ADMIN |
| `/api/admin/sessions` | GET | Pageable | Paged list | ADMIN |
| `/api/admin/audit-logs` | GET | Pageable | Paged list | ADMIN |

### Deprecated (Real Payment - Removed)
| Endpoint | Method | Response | Notes |
|----------|--------|----------|-------|
| `/api/payments/checkout` | POST | 410 Gone | Use `/api/wallet/purchase` |
| `/api/payments/{id}` | GET | 410 Gone | Removed |

### CORS Configuration
| Setting | Value |
|---------|-------|
| `app.cors.allowed-origins` | `http://localhost:*` (development) |
| Backend port | 8080 |
| Context path | None (root) |
| Actuator port | 8081 (health, info only) |

---

## Frontend (React + TypeScript + Vite)

**Location:** `E:\wifirent-bd\frontend\react-app/`

**Tech Stack:**
- React 18 with TypeScript
- Vite (build tool + dev server)
- React Router DOM (routing)
- Axios (HTTP client with JWT auto-refresh)
- Tailwind CSS (light theme styling)
- Lucide React (icons)

**Routes:**

| Route | Page | Auth Required | Role |
|-------|------|---------------|------|
| `/login` | LoginPage | No | - |
| `/register` | RegisterPage | No | - |
| `/rent` | RentPage | Yes | CUSTOMER |
| `/rent/:hotspotId` | RentPage | Yes | CUSTOMER |
| `/access` | AccessPage | Yes | CUSTOMER |
| `/host` | HostDashboardPage | Yes | HOST/ADMIN |
| `/host/hotspots/:hotspotId` | HostDashboardPage | Yes | HOST/ADMIN |
| `/gateways` | GatewayManagementPage | Yes | HOST/ADMIN |
| `/gateways/:deviceId` | GatewayManagementPage | Yes | HOST/ADMIN |
| `/profile` | ProfilePage | Yes | All |

**API Client (`src/api/apiClient.ts`):**
- Axios instance with base URL from `VITE_API_URL` env var
- Automatic JWT access token injection on all requests
- Automatic token refresh on 401 (single concurrent refresh)
- Token stored in `localStorage` (`wifirent_access_token`, `wifirent_refresh_token`)

**Demo Credits Labeling:**
- All balances displayed as `DEMO_CREDITS`
- Profile page shows: "Demo credits are virtual currency used within WiFiRent BD. They have no real monetary value and cannot be withdrawn or redeemed for cash."

---

## Windows Commands to Start the Application

### Terminal 1: Start the Backend
```powershell
cd E:\wifirent-bd\backend
.\mvnw spring-boot:run
```
Backend starts on `http://localhost:8080/api`

### Terminal 2: Start the Frontend
**Prerequisites:** Node.js must be installed (https://nodejs.org/).

```powershell
cd E:\wifirent-bd\frontend\react-app
npm install
npm run dev
```
Frontend starts on `http://localhost:5173` and proxies `/api` requests to `http://localhost:8080/api`.

### Alternative: Test Backend Only (No Frontend Needed)
You can test all backend API endpoints using PowerShell `curl.exe`:

```powershell
# 1. Register a customer
@{"email"="customer@demo.com";"password"="password123";"name"="Demo Customer"} | ConvertTo-Json | Set-Content -Path "login.json"
curl.exe -s -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "@login.json"

# 2. Login
@{"email"="customer@demo.com";"password"="password123"} | ConvertTo-Json | Set-Content -Path "login.json"
$res = curl.exe -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "@login.json"

# 3. Check wallet balance (use the accessToken from login response)
$token = "<access_token_from_response>"
curl.exe -s -X GET http://localhost:8080/api/wallet/balance -H "Authorization: Bearer $token"

# 4. Browse hotspots
curl.exe -s "http://localhost:8080/api/hotspots/nearby?lat=23.8103&lng=90.4125&radiusMeters=5000"

# 5. Get packages for hotspot 1
curl.exe -s "http://localhost:8080/api/hotspots/1/packages"

# 6. Purchase a package
@{"hotspotId"=1;"packageId"=1;"idempotencyKey"="purchase-001"} | ConvertTo-Json | Set-Content -Path "purchase.json"
curl.exe -s -X POST http://localhost:8080/api/wallet/purchase -H "Content-Type: application/json" -H "Authorization: Bearer $token" -d "@purchase.json"
```

---

## Demo Credentials (Auto-Seeded on Backend Startup)

| Role | Email | Password |
|------|-------|----------|
| Customer | `customer@demo.com` | `password123` |
| Host | `host@demo.com` | `password123` |
| Admin | `admin@demo.com` | `password123` |

Every new user receives **5000 DEMO_CREDITS** automatically on registration.

---

## Step-by-Step Teacher Demonstration Guide

### Step 1: Start the Backend
Open PowerShell and run:
```powershell
cd E:\wifirent-bd\backend
.\mvnw spring-boot:run
```
Wait for: `Started WifirentBdApplication in X seconds`

### Step 2: Start the Frontend (if Node.js is installed)
Open a second PowerShell window:
```powershell
cd E:\wifirent-bd\frontend\react-app
npm install
npm run dev
```
Open browser to `http://localhost:5173`

### Step 3: Demonstrate Customer Flow

**3a. Register a customer:**
1. Click "Create account" on the login page
2. Enter: Full Name, Email (`customer@demo.com`), Password (`password123`)
3. Click "Create account"
4. Observe: Auto-redirect to Rent page with 5000 DEMO_CREDITS balance

**3b. Browse hotspots:**
1. View the list of available hotspots (auto-seeded: "Campus Library Wi-Fi")
2. Click on a hotspot to view details and packages

**3c. Purchase a package:**
1. Select a package (e.g., 500 DEMO_CREDITS for 1 hour)
2. Click "Confirm Purchase"
3. Observe: Balance decreases from 5000 to 4500 DEMO_CREDITS
4. Click "Go to Access Page →"

**3d. View session status:**
1. On the Access page, observe the session status (PENDING → ACTIVE if gateway authorized)
2. View time remaining, data remaining, started/expires timestamps
3. Read the connection instructions

### Step 4: Demonstrate Host Flow

**4a. Login as host:**
1. Log out from customer account
2. Login as `host@demo.com` / `password123`
3. Navigate to Host Dashboard

**4b. View earnings:**
1. Observe gross revenue, platform commission, and host earnings in demo credits

**4c. Create a new hotspot:**
1. Click "+ Add Hotspot"
2. Fill in name, description, latitude, longitude, SSID, approximate area
3. Click "Create Hotspot"

**4c. Create a package:**
1. Select a hotspot from the list
2. Click "+ Add Package"
3. Fill in price, package type, duration/data quota
4. Click "Create Package"

**4d. Register a gateway:**
1. Click "+ Register Gateway"
2. Fill in device ID, name, management URL, adapter type (MOCK or OPENWRT)
3. Select a hotspot (optional)
4. Click "Register Gateway"
5. Test connection

### Step 5: Verify Key Behaviors

- **Demo credits labeling:** All balances show "DEMO_CREDITS" with "not withdrawable" notice
- **Insufficient balance:** Attempting to purchase above balance shows error
- **Duplicate purchase:** Same idempotency key rejected with 400 error
- **Role-based access:** Customer cannot access `/api/host/**` endpoints
- **JWT refresh:** Tokens auto-refresh on expiration

---

## Build and Test Results

### Backend
```
Tests run: 73, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
- All 73 JUnit 5 tests pass
- Backend compiles with Java 21
- H2 in-memory database used for demo (no PostgreSQL required)

### Frontend
- **Node.js not installed on this system** - `npm install` and `npm run build` could not be executed
- All TypeScript types match backend DTOs exactly
- All API endpoints match backend controller mappings
- CORS configured for `http://localhost:*` (development)
- Light theme applied

---

## Remaining Blockers

| Blocker | Impact | Resolution |
|---------|--------|------------|
| Node.js not installed | Frontend cannot be compiled or run | Install Node.js from https://nodejs.org/, then `npm install && npm run dev` in `frontend/react-app/` |
| PostgreSQL not installed | Backend uses H2 in-memory (data resets on restart) | Install PostgreSQL, create `wifirent_bd` database, update `.env` with DB credentials |
| No physical OpenWrt router | Real Wi-Fi access control not testable | MockGatewayAdapter simulates gateway; real integration requires OpenWrt router (v22.03+) + openNDS captive portal |
| No real payment provider | Demo credits only by design | Intentionally excluded per requirements; no real money integration |

---

## Security Notes

- JWT access tokens: 15-minute expiration
- JWT refresh tokens: 7-day expiration
- Token revocation via `token_version` column (incremented on logout)
- Passwords hashed with BCrypt
- Role-based authorization: CUSTOMER, HOST, ADMIN
- Gateway credentials never returned to frontend
- CORS configured for development origins only
- SSRF protection on gateway management URLs (no localhost/private IP access)
- No secrets committed to source control (`.env.example` only)
- Gateway credentials stored securely, never returned in API responses

---

## Router Integration Guide

See `ROUTER_INTEGRATION_GUIDE.md` for detailed information about OpenWrt/openNDS integration, supported hardware, and security requirements.
