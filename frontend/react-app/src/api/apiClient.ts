import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { AuthResponse, LoginRequest, RegisterRequest } from '@/types'

const API_BASE_URL = import.meta.env.VITE_API_URL || ''

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Token storage keys
const ACCESS_TOKEN_KEY = 'wifirent_access_token'
const REFRESH_TOKEN_KEY = 'wifirent_refresh_token'

export const getAccessToken = (): string | null => {
  if (typeof window === 'undefined') return null
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export const setAccessToken = (token: string): void => {
  if (typeof window === 'undefined') return
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export const removeAccessToken = (): void => {
  if (typeof window === 'undefined') return
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export const getRefreshToken = (): string | null => {
  if (typeof window === 'undefined') return null
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export const setRefreshToken = (token: string): void => {
  if (typeof window === 'undefined') return
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

export const removeRefreshToken = (): void => {
  if (typeof window === 'undefined') return
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

let isRefreshing = false
let refreshPromise: Promise<string | null> | null = null

const requestTokenRefresh = async (): Promise<string | null> => {
  if (!isRefreshing) {
    isRefreshing = true
    refreshPromise = (async () => {
      try {
        const refreshToken = getRefreshToken()
        if (!refreshToken) {
          return null
        }
        const response = await apiClient.post<AuthResponse>('/api/auth/refresh', {
          refreshToken,
        })
        const { accessToken, refreshToken: newRefreshToken } = response.data
        setAccessToken(accessToken)
        if (newRefreshToken) {
          setRefreshToken(newRefreshToken)
        }
        return accessToken
      } catch (error) {
        removeAccessToken()
        removeRefreshToken()
        return null
      } finally {
        isRefreshing = false
        refreshPromise = null
      }
    })()
  }
  return refreshPromise
}

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const newAccessToken = await requestTokenRefresh()
      if (newAccessToken) {
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return apiClient(originalRequest)
      }

      // Refresh failed - clear tokens
      removeAccessToken()
      removeRefreshToken()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export const authApi = {
  register: (data: RegisterRequest) => apiClient.post<AuthResponse>('/api/auth/register', data),
  login: (data: LoginRequest) => apiClient.post<AuthResponse>('/api/auth/login', data),
  refresh: (refreshToken: string) =>
    apiClient.post<AuthResponse>('/api/auth/refresh', { refreshToken }),
  logout: () => apiClient.post('/api/auth/logout'),
  me: () => apiClient.get<{ id: number; name: string; email: string; role: string; enabled: boolean; emailVerified: boolean }>('/api/auth/me'),
}

export const hotspotApi = {
  list: (params?: { page?: number; size?: number; sort?: string }) =>
    apiClient.get<PagedHotspotResponse>('/api/hotspots', { params }),
  get: (id: number) => apiClient.get<HotspotDto>(`/api/hotspots/${id}`),
  nearby: (lat: number, lng: number, radiusMeters: number) =>
    apiClient.get<PagedHotspotResponse>('/api/hotspots/nearby', { params: { lat, lng, radiusMeters } }),
  getPackages: (id: number) => apiClient.get<any>(`/api/hotspots/${id}/packages`),
}

export const walletApi = {
  getBalance: () => apiClient.get<WalletBalanceDto>('/api/wallet/balance'),
  getTransactions: (params?: { page?: number; size?: number }) =>
    apiClient.get<WalletTransactionListDto>('/api/wallet/transactions', { params }),
  purchase: (data: PurchaseWithWalletRequest) =>
    apiClient.post<PurchaseWithWalletResponse>('/api/wallet/purchase', data),
  transfer: (data: WalletTransferRequest) =>
    apiClient.post<WalletBalanceDto>('/api/wallet/transfer', data),
}

export const rentalApi = {
  create: (data: CreateRentalOrderRequest) =>
    apiClient.post<RentalOrderDto>('/api/rentals', data),
  list: (params?: { page?: number; size?: number }) =>
    apiClient.get<any>('/api/rentals', { params }),
  get: (id: number) => apiClient.get<RentalOrderDto>(`/api/rentals/${id}`),
  cancel: (id: number, reason?: string) =>
    apiClient.post<RentalOrderDto>(`/api/rentals/${id}/cancel`, { reason }),
}

export const sessionApi = {
  getActive: () => apiClient.get<ActiveSessionResponse>('/api/sessions/active'),
  get: (id: number) => apiClient.get<SessionDto>(`/api/sessions/${id}`),
  listHistory: (params?: { page?: number; size?: number }) =>
    apiClient.get<any>('/api/sessions/history', { params }),
}

export const hostApi = {
  listHotspots: (params?: { page?: number; size?: number }) =>
    apiClient.get<PagedHotspotResponse>('/api/host/hotspots', { params }),
  createHotspot: (data: CreateHotspotRequest) => apiClient.post<HotspotDto>('/api/host/hotspots', data),
  updateHotspot: (id: number, data: UpdateHotspotRequest) =>
    apiClient.put<HotspotDto>(`/api/host/hotspots/${id}`, data),
  setAvailability: (id: number, available: boolean) =>
    apiClient.patch<HotspotDto>(`/api/host/hotspots/${id}/availability`, { available }),
  getEarnings: () => apiClient.get<HostEarningsDto>('/api/host/earnings'),
  getLedger: (params?: { page?: number; size?: number }) =>
    apiClient.get<any>('/api/host/ledger', { params }),
  getPayouts: (params?: { page?: number; size?: number }) =>
    apiClient.get<any>('/api/host/payouts', { params }),
}

export const packageApi = {
  create: (hotspotId: number, data: CreatePackageRequest) =>
    apiClient.post<PackagePlanDto>(`/api/host/packages/hotspots/${hotspotId}`, data),
  update: (id: number, data: UpdatePackageRequest) =>
    apiClient.put<PackagePlanDto>(`/api/host/packages/${id}`, data),
  delete: (id: number) => apiClient.delete(`/api/host/packages/${id}`),
}

export const gatewayApi = {
  register: (data: GatewayRegisterRequest) =>
    apiClient.post<GatewaySessionResponse>('/api/gateways/register', data),
  heartbeat: (deviceId: string, status: string) =>
    apiClient.post(`/api/gateways/${deviceId}/heartbeat`, { status }),
  events: (deviceId: string, payload: string) =>
    apiClient.post(`/api/gateways/${deviceId}/events`, payload),
  authorize: (deviceId: string, sessionId: number) =>
    apiClient.post<GatewaySessionResponse>(`/api/gateways/${deviceId}/sessions/${sessionId}/authorize`),
  revoke: (deviceId: string, sessionId: number) =>
    apiClient.post(`/api/gateways/${deviceId}/sessions/${sessionId}/revoke`),
  list: () => apiClient.get<GatewayDeviceDto[]>('/api/gateways'),
  get: (deviceId: string) => apiClient.get<GatewayDeviceDto>(`/api/gateways/${deviceId}`),
  health: (deviceId: string) => apiClient.get<GatewayHealthDto>(`/api/gateways/${deviceId}/health`),
  testConnection: (deviceId: string) =>
    apiClient.post(`/api/gateways/${deviceId}/test-connection`),
  capabilities: () => apiClient.get<string[]>('/api/gateways/capabilities'),
}

export default apiClient
