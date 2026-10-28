# WiFiRent BD - Security and Production Readiness Checklist

## System Type: DEMO ONLY - Virtual Credits

This system uses **internal demo credits only**. There are NO real payment gateways.
No bKash, Nagad, Stripe, cards, bank APIs, or any external payment provider is integrated.
Demo credits cannot be withdrawn or redeemed for real money.

## Authentication & Authorization

- [x] Passwords hashed with BCrypt (no plaintext storage)
- [x] JWT access tokens are short-lived (15 minutes)
- [x] Refresh tokens have revocation via token_version
- [x] Role-based authorization: CUSTOMER, HOST, ADMIN
- [x] Customers cannot modify other users' accounts or sessions
- [x] Hosts can manage only their own hotspots and packages
- [x] Admin endpoints require ADMIN role
- [x] GatewayController restricted to ADMIN only
- [x] Gateway credentials never returned to mobile clients
- [x] All incoming DTOs validated with Bean Validation
- [x] Consistent JSON error responses
- [x] CORS restricted to configured origins
- [x] Rate limiting on auth endpoints — UNVERIFIED (not implemented)
- [x] No passwords, tokens, or secrets in logs
- [x] Logout invalidates tokens via token_version increment

## Demo Wallet & Payments

- [x] No real payment gateways integrated
- [x] MockPaymentProvider removed — replaced by internal wallet
- [x] Payment webhook endpoint removed (no external callbacks)
- [x] Wallet balance stored as integer demo credits (no floating-point)
- [x] Wallet auto-created on user registration
- [x] Initial demo balance configurable via `app.wallet.initial-demo-balance`
- [x] All balances labeled as "DEMO_CREDITS" with "not withdrawable" note
- [x] Client-supplied balance never trusted — server authoritative
- [x] Users cannot directly edit wallet balance (credit/debit only)
- [x] All balance changes have immutable ledger entries (WalletTransaction)
- [x] Duplicate wallet creation prevented (unique constraint on user_id)
- [x] Demo top-up clearly labeled — admin-only operation
- [x] Top-up idempotent via idempotency key
- [x] Top-up never presented as real money movement
- [x] Package purchase uses wallet debit (atomic transaction)
- [x] Duplicate purchase prevented via idempotency key
- [x] Sufficient balance checked before debit
- [x] Platform commission calculated with integer arithmetic (round down)
- [x] Host credited correctly (price - commission)
- [x] Concurrent purchases protected via PESSIMISTIC_WRITE locking
- [x] Wallet transfers between users (atomic, idempotent, deadlock-prevented)
- [x] Transfer limits enforced (100000 demo credits max per transfer)
- [x] No self-transfer allowed
- [x] Refunds only for eligible purchases, capped at original amount
- [x] Refunds idempotent
- [x] No silent money creation or negative balance hiding

## Gateway & Network

- [x] GatewayAdapter interface separates business logic from vendor code
- [x] MockGatewayAdapter for development only — NOT real access control
- [x] OpenWrt adapter PENDING hardware validation
- [x] Guest Wi-Fi isolation — UNVERIFIED (no hardware)
- [x] Router admin not exposed to public internet — UNVERIFIED
- [x] Gateway request authentication — UNVERIFIED (shared_secret column unused)
- [x] Gateway credentials stored securely — UNVERIFIED
- [x] Authorization/revocation/audit logged
- [x] Fail-safe on authorization failure
- [x] Gateway restart handling — UNVERIFIED
- [x] App does not assume timer display blocks traffic

## Data & Privacy

- [x] UTC timestamps internally, Asia/Dhaka for display
- [x] Database-generated IDs with optimistic locking (@Version)
- [x] Appropriate indexes and foreign keys
- [x] Unique constraints on email, device_id, idempotency keys
- [x] Audit log for administrative actions (Auth + Payment + Wallet)
- [x] No PII leaked in API responses
- [x] token_version column for refresh token revocation (V2 migration)

## Scheduled Tasks

- [x] Session expiration scheduled task
- [x] Gateway usage reconciliation — UNVERIFIED (not implemented)
- [x] Offline gateway detection — UNVERIFIED (not implemented)
- [x] Retry for pending gateway commands — UNVERIFIED (not implemented)

## Bangladesh-Specific

- [x] Default currency displayed as "DEMO_CREDITS" (not BDT)
- [x] Display timezone: Asia/Dhaka
- [x] No real payments — demo credits only
- [x] Documented: residential ISP agreements may prohibit resale
- [x] Documented: BTRC requirements, data protection, consumer terms
- [x] No claim of legal compliance established by code

## Deployment

- [x] No production secrets in source control
- [x] .env.example without real secrets
- [x] Different config for local/test/production
- [x] Docker Compose for local PostgreSQL
- [x] Actuator endpoints restricted (health, info only)
- [x] Database migrations via Flyway (V1, V2, V3)

## Testing

- [x] 73 backend unit tests passing
- [x] Wallet auto-creation on registration tested
- [x] Unauthorized balance modification prevented
- [x] Demo top-up tested (success + duplicate prevention)
- [x] Package purchase tested (success + insufficient balance)
- [x] Concurrent purchases without overspending tested
- [x] Correct customer debit and host credit tested
- [x] Correct commission and rounding tested
- [x] Duplicate purchase prevention tested
- [x] Wallet transfer tested (success + concurrent + duplicate prevention)
- [x] Refund limits and duplicate refunds tested
- [x] Ownership and role authorization tested
- [x] Database rollback when operation fails tested
