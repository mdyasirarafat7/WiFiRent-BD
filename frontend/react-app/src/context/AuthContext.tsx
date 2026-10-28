import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react'
import { authApi, getAccessToken, setAccessToken, getRefreshToken, setRefreshToken, removeAccessToken, removeRefreshToken } from '@/api/apiClient'
import { User, AuthResponse } from '@/types'

interface AuthContextType {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refresh: () => Promise<void>
  fetchMe: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const fetchMe = useCallback(async () => {
    try {
      const response = await authApi.me()
      setUser(response.data as User)
    } catch {
      setUser(null)
      removeAccessToken()
      removeRefreshToken()
    } finally {
      setIsLoading(false)
    }
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login({ email, password })
    const data: AuthResponse = response.data
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    setUser({
      id: data.id,
      name: data.name,
      email: data.email,
      role: data.role as 'CUSTOMER' | 'HOST' | 'ADMIN',
      enabled: true,
      emailVerified: false,
    })
  }, [])

  const register = useCallback(async (name: string, email: string, password: string) => {
    const response = await authApi.register({ name, email, password })
    const data: AuthResponse = response.data
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    setUser({
      id: data.id,
      name: data.name,
      email: data.email,
      role: data.role as 'CUSTOMER' | 'HOST' | 'ADMIN',
      enabled: true,
      emailVerified: false,
    })
  }, [])

  const refresh = useCallback(async () => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) throw new Error('No refresh token')
    const response = await authApi.refresh(refreshToken)
    const data: AuthResponse = response.data
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    if (user) {
      setUser({ ...user, accessToken: data.accessToken })
    }
  }, [user])

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } catch {
      // Ignore logout errors - always clear local state
    }
    removeAccessToken()
    removeRefreshToken()
    setUser(null)
    setIsLoading(false)
  }, [])

  useEffect(() => {
    const token = getAccessToken()
    if (token) {
      fetchMe()
    } else {
      setIsLoading(false)
    }
  }, [fetchMe])

  const value = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    register,
    logout,
    refresh,
    fetchMe,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
