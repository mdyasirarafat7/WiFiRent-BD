export interface User {
  id: number
  name: string
  email: string
  role: 'CUSTOMER' | 'HOST' | 'ADMIN'
  enabled: boolean
  emailVerified: boolean
}

export interface AuthResponse {
  id: number
  name: string
  email: string
  role: string
  accessToken: string
  refreshToken: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  phone?: string
}

export interface HotspotDto {
  id: number
  name: string
  description: string | null
  latitude: number
  longitude: number
  ssid: string | null
  approximateArea: string | null
  available: boolean
  status: string
  hostId: number
  hostBusinessName: string | null
}

export interface PackagePlanDto {
  id: number
  priceMinorUnits: number
  packageType: 'TIME_BASED' | 'DATA_BASED' | 'TIME_AND_DATA'
  durationMinutes: number | null
  dataQuotaBytes: number | null
  validityMinutes: number | null
  speedLimitKbps: number | null
  active: boolean
}

export interface PagedHotspotResponse {
  content: HotspotDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface CreateRentalOrderRequest {
  hotspotId: number
  packageId: number
  idempotencyKey?: string
}

export interface RentalOrderDto {
  id: number
  customerId: number
  hotspotId: number
  packageId: number
  status: string
  totalPriceMinorUnits: number
  currency: string
}

export interface PurchaseWithWalletRequest {
  hotspotId: number
  packageId: number
  idempotencyKey?: string
}

export interface PurchaseWithWalletResponse {
  orderId: number
  customerDebitedMinorUnits: number
  hostCreditedMinorUnits: number
  platformCommissionMinorUnits: number
  customerRemainingBalanceMinorUnits: number
  status: string
  note: string
}

export interface SessionDto {
  id: number
  customerId: number
  hotspotId: number
  packageId: number
  rentalOrderId: number
  status: 'PENDING' | 'ACTIVE' | 'EXPIRED' | 'REVOKED' | 'FAILED'
  startedAt: string | null
  expiresAt: string | null
  dataQuotaBytes: number | null
  consumedBytes: number | null
  gatewaySessionId: string | null
}

export interface ActiveSessionResponse {
  hasActiveSession: boolean
  session: SessionDto | null
}

export interface WalletBalanceDto {
  userId: number
  balanceMinorUnits: number
  currency: string
  note: string
}

export interface WalletTransactionDto {
  id: number
  transactionType: string
  amountMinorUnits: number
  referenceId: string
  referenceType: string
  description: string
  idempotencyKey: string | null
  createdAt: string
}

export interface WalletTransactionListDto {
  content: WalletTransactionDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface WalletTopUpRequest {
  targetUserId: number
  amountMinorUnits: number
  referenceId: string
  idempotencyKey?: string
}

export interface WalletTransferRequest {
  targetUserId: number
  amountMinorUnits: number
  referenceId: string
  idempotencyKey?: string
}

export interface CreateHotspotRequest {
  name: string
  description: string | null
  latitude: number
  longitude: number
  ssid: string | null
  approximateArea: string | null
}

export interface UpdateHotspotRequest {
  name: string | null
  description: string | null
  ssid: string | null
  approximateArea: string | null
}

export interface AvailabilityRequest {
  available: boolean
}

export interface NearbyHotspotRequest {
  lat: number
  lng: number
  radiusMeters: number
}

export interface CreatePackageRequest {
  priceMinorUnits: number
  packageType: string
  durationMinutes: number | null
  dataQuotaBytes: number | null
  validityMinutes: number | null
  speedLimitKbps: number | null
}

export interface UpdatePackageRequest {
  priceMinorUnits: number | null
  packageType: string | null
  durationMinutes: number | null
  dataQuotaBytes: number | null
  validityMinutes: number | null
  speedLimitKbps: number | null
  active: boolean | null
}

export interface HostEarningsDto {
  hostId: number
  grossRevenueMinorUnits: number
  platformCommissionMinorUnits: number
  hostEarningsMinorUnits: number
  currency: string
}

export interface HostLedgerEntryDto {
  id: number
  entryType: string
  amountMinorUnits: number
  currency: string
  referenceId: string | null
  referenceType: string | null
  description: string | null
  createdAt: string
}

export interface ErrorResponse {
  status: number
  code: string
  message: string
  timestamp: string
  fields: Record<string, string> | null
}

export interface GatewayDeviceDto {
  id: number
  deviceId: string
  name: string
  managementUrl: string | null
  adapterType: string
  integrationMode: 'MOCK' | 'REAL'
  online: boolean
  lastHeartbeatAt: string | null
  status: string
  hotspotId: number | null
  hotspotName: string | null
  capabilities: string[]
  firmwareVersion: string | null
}

export interface GatewayHealthDto {
  online: boolean
  lastHeartbeatAt: string | null
}

export interface GatewayRegisterRequest {
  deviceId: string
  name: string
  managementUrl: string | null
  adapterType: string | null
  hotspotId: number | null
}

export interface GatewaySessionAuthorizeRequest {
  sessionId: number
}
