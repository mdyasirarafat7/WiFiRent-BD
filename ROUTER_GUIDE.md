# WiFiRent BD - Router Configuration and Validation Guide

## Gateway Integration Overview

The backend uses a `GatewayAdapter` interface to separate business logic from vendor-specific router control.

### Available Adapters

1. **MockGatewayAdapter** - Fully functional for local development and automated tests.
   - Simulates gateway registration, heartbeat, session authorization/revocation
   - NO real network control - safe for development

2. **OpenWrtGatewayAdapter** - Interface defined, real implementation PENDING hardware validation.
   - Requires physical OpenWrt router with openNDS captive portal
   - Not implemented - no hardware available for verification

## Supported Gateway Features

| Feature | Mock | OpenWrt (planned) |
|---------|------|-------------------|
| Register gateway | Yes | Pending |
| Heartbeat | Yes | Pending |
| Authorize session | Yes | Pending |
| Revoke session | Yes | Pending |
| Get session status | Yes | Pending |
| Get usage | Yes | Pending |
| Update package limits | Yes | Pending |

## Security Requirements (Production)

1. **Isolate guest Wi-Fi from host LAN** - Use separate VLAN or SSID isolation
2. **Do not expose router admin to public internet** - Only local network access
3. **Authenticate gateway requests** - Use shared secret or API tokens
4. **Store gateway credentials securely** - Encrypt at rest
5. **Minimum necessary permissions** - Gateway user with limited capabilities
6. **Log authorization, revocation, and failures** - Audit trail
7. **Fail safely if authorization fails** - Deny access by default
8. **Handle gateway restarts** - Don't restore expired sessions

## Hardware Validation Checklist

Before deploying with real OpenWrt gateways:

- [ ] Verify OpenWrt version (recommend 22.03 or later)
- [ ] Verify openNDS version and captive portal support
- [ ] Verify management interface (SSH, UCI, or REST API)
- [ ] Test guest Wi-Fi isolation from LAN
- [ ] Test per-session authorization and revocation
- [ ] Test time-based expiry enforcement
- [ ] Test data quota enforcement
- [ ] Test speed limits (if supported by router)
- [ ] Test usage reporting accuracy
- [ ] Test gateway heartbeat and offline detection
- [ ] Test retry-safe commands
- [ ] Test local enforcement when backend unreachable
- [ ] Test gateway restart without restoring expired sessions

## Never Claim

- Real access control has been verified without actual hardware testing
- Mock gateway works in production
- Payments are real when using MockPaymentProvider
