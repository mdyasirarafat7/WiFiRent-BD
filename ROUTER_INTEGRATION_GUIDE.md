# WiFiRent BD - Router Integration Guide

## Overview

This document describes the router integration architecture for WiFiRent BD. The system supports two gateway modes:

1. **MOCK** - Simulated gateway for development and demonstration (default)
2. **REAL** - Real OpenWrt router with openNDS captive portal (requires physical hardware)

---

## Supported Integration Architecture

### Mock Gateway (Default)

The MockGatewayAdapter simulates all gateway operations without any physical hardware:
- Gateway registration, heartbeat, and health checks
- Session authorization and revocation
- Usage reporting (simulated)
- Session status tracking

This is suitable for demonstrations and development without any physical hardware.

### OpenWrt Gateway (Real Hardware)

The OpenWrtGatewayAdapter provides the interface for real OpenWrt routers running openNDS captive portal. **This integration has NOT been tested against physical hardware.**

#### Supported OpenWrt Versions
- OpenWrt 22.03 (recommended)
- OpenWrt 23.05 (recommended)

#### Required Packages
- `opennds-fast` - Captive portal with FAS (Forwarding Authentication Service)
- `luci-app-opennds` - Web interface for openNDS

#### Network Topology

```
[Internet] <--> [OpenWrt Router] <--> [Guest Wi-Fi SSID]
                                       |
                              [WiFiRent BD Backend] (via HTTPS)
```

The gateway (OpenWrt router) connects to the backend via HTTPS to:
- Send heartbeats (report online status)
- Receive session authorization commands
- Report session status and usage

#### Guest Wi-Fi Isolation

The OpenWrt router MUST be configured with:
- Guest Wi-Fi SSID isolated from the host's private LAN
- Client-to-client isolation enabled on the guest SSID
- Captive portal blocking unauthenticated traffic

---

## Gateway Capabilities

| Capability | Mock | OpenWrt (planned) | Description |
|------------|------|-------------------|-------------|
| TIME_BASED_ACCESS | ✅ | ⚠️ | Authorize access for a duration |
| DATA_BASED_ACCESS | ✅ | ⚠️ | Authorize access for a data quota |
| CAPTIVE_PORTAL | ✅ | ⚠️ | Captive portal authentication |
| SESSION_REVOCATION | ✅ | ⚠️ | Revoke active sessions |
| USAGE_REPORTING | ✅ | ⚠️ | Report consumed bytes/duration |
| SPEED_LIMITING | ✅ | ⚠️ | Enforce speed limits |
| DEVICE_ISOLATION | ✅ | ⚠️ | Isolate guest devices |
| HEARTBEAT_MONITORING | ✅ | ⚠️ | Report gateway online status |

✅ = Implemented and tested with mock
⚠️ = Interface defined, not tested against hardware

---

## Gateway Configuration

### Registering a Gateway

Hosts can register gateways through the backend API or frontend:

```powershell
# Example: Register a MOCK gateway
curl.exe -s -X POST http://localhost:8080/api/gateways/register `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer <access_token>" `
  -d '{"deviceId":"gw-001","name":"Home Gateway","managementUrl":"https://gateway.example.com:8443","adapterType":"MOCK","hotspotId":1}'
```

### Gateway Configuration Fields

| Field | Description | Required |
|-------|-------------|----------|
| deviceId | Unique device identifier | Yes |
| name | Human-readable name | Yes |
| managementUrl | HTTPS URL for gateway management | No (optional for MOCK) |
| adapterType | MOCK or OPENWRT | Yes |
| hotspotId | Associated hotspot ID | No |

### Gateway Security

1. **Never expose router administration to the public internet**
2. **Gateway credentials are stored securely and never returned in API responses**
3. **SSRF Protection**: Management URLs are validated - localhost, private IPs (10.x, 172.16-31.x, 192.168.x), and cloud metadata endpoints (169.254.169.254) are blocked
4. **Only HTTP/HTTPS schemes are allowed** for management URLs
5. **Gateway credentials are never sent to the frontend**

---

## Rental Session Flow

### With Mock Gateway

1. Customer purchases a package using demo credits
2. Backend creates rental order (PAID) and session (PENDING)
3. Backend automatically authorizes session on mock gateway
4. Session becomes ACTIVE
5. Customer connects to Wi-Fi and gets internet access

### With Real OpenWrt Gateway

1. Customer purchases a package using demo credits
2. Backend creates rental order (PAID) and session (PENDING)
3. Backend attempts gateway authorization via openNDS FAS
4. If authorization succeeds → Session ACTIVE, customer gets Wi-Fi access
5. If authorization fails → Customer refunded, session marked FAILED
6. Session expires based on time or data quota
7. Backend revokes session on gateway

---

## Device Identification

The system tracks rental sessions by customer and hotspot. The gateway adapter is responsible for mapping sessions to physical devices (MAC addresses) on the router.

**Limitations:**
- MAC randomization on modern devices may cause identification issues
- Device changes during a session are not supported
- The gateway must support per-device authorization for this to work

---

## Testing Without Hardware

The mock gateway allows full testing of the rental flow:

1. Start the backend: `./mvnw spring-boot:run`
2. Register/login as customer
3. Purchase a package
4. Session is automatically authorized by mock gateway
5. View session status at `/api/sessions/active`

To test gateway management:
1. Register a gateway via `/api/gateways/register`
2. Test connection via `/api/gateways/{deviceId}/test-connection`
3. Check health via `/api/gateways/{deviceId}/health`

---

## Router Models

| Model | OpenWrt Support | openNDS Support | Verified |
|-------|-----------------|-----------------|----------|
| TP-Link Archer C7 (v2, v4, v5) | Yes | Yes (manual install) | ❌ Not tested |
| TP-Link Archer A7 | Yes | Yes (manual install) | ❌ Not tested |
| TP-Link Archer A6/C6 | Partial | Partial | ❌ Not tested |
| TP-Link Archer AX series | Mostly No | No | ❌ Not tested |
| TP-Link TL-WR841N | Yes | Yes (manual install) | ❌ Not tested |
| TP-Link Deco mesh | No | No | ❌ Not tested |
| GL.iNet GL-MT300N | Yes | Yes | ❌ Not tested |
| GL.iNet GL-MT1300 | Yes | Yes | ❌ Not tested |
| All other models | UNKNOWN | UNKNOWN | ❌ Not tested |

**No router models have been verified against physical hardware.** All gateway integration is currently simulated via MockGatewayAdapter.

---

## Running with Mock Gateway (Default)

No additional configuration needed. The backend starts with MockGatewayAdapter by default:

```powershell
cd E:\wifirent-bd\backend
.\mvnw spring-boot:run
```

All gateway operations are simulated:
- Registration: Always succeeds
- Heartbeat: Always marks online
- Authorization: Always succeeds
- Session status: Always returns active
- Usage: Returns zero values

---

## Running with Real OpenWrt Gateway

1. Flash OpenWrt 22.03 or 23.05 on compatible router
2. Install openNDS: `opkg update && opkg install opennds-fast luci-app-opennds`
3. Configure openNDS with FAS pointing to backend API
4. Configure guest Wi-Fi with isolation
5. Register gateway in backend with adapterType=OPENWRT
6. Test connection via `/api/gateways/{deviceId}/test-connection`

---

## Security Considerations

1. **Router administration must NOT be exposed to the public internet**
2. **All gateway communication must use HTTPS**
3. **Gateway shared secrets must be encrypted at rest**
4. **SSRF protection prevents access to localhost/private IPs/metadata endpoints**
5. **Gateway credentials are never returned in API responses**
6. **Only hotspot owners or admins can manage gateways**

---

## Current Limitations

1. **No physical router testing** - All gateway integration is simulated
2. **No real OpenWrt/openNDS testing** - Interface defined but not verified
3. **No device identification** - MAC address association not implemented
4. **No automatic session expiry enforcement on gateway** - Backend tracks expiry, gateway enforcement not implemented
5. **No captive portal integration** - openNDS FAS integration not implemented

## Hardware Needed for Testing

- OpenWrt-compatible router (Archer C7 v2/v4/v5, GL.iNet GL-MT300N, or similar)
- OpenWrt 22.03 or 23.05 firmware
- openNDS package installed
- Network access to router management interface from backend server
