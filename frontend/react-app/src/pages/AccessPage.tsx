import { useState, useEffect } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Navigate } from 'react-router-dom'
import { sessionApi, walletApi } from '@/api/apiClient'
import { SessionDto } from '@/types'

export default function AccessPage() {
  const { user, isLoading } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [session, setSession] = useState<any>(null)
  const [walletBalance, setWalletBalance] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user) return
    loadSession()
    loadWalletBalance()
  }, [user])

  const loadSession = async () => {
    try {
      const res = await sessionApi.getActive()
      const data = res.data
      if (data.hasActiveSession && data.session) {
        setSession(data.session)
      }
    } catch (err: any) {
      setError('Failed to load session status')
    }
  }

  const loadWalletBalance = async () => {
    try {
      const res = await walletApi.getBalance()
      setWalletBalance(res.data.balanceMinorUnits)
    } catch (err) {
      console.error('Failed to load balance', err)
    }
  }

  const formatBytes = (bytes: number | null): string => {
    if (!bytes) return 'Unlimited'
    const gb = bytes / (1024 * 1024 * 1024)
    return gb.toFixed(2) + ' GB'
  }

  const formatTimeRemaining = (expiresAt: string | null): string => {
    if (!expiresAt) return 'Unlimited'
    const now = new Date()
    const expiry = new Date(expiresAt)
    const diffMs = expiry.getTime() - now.getTime()
    if (diffMs <= 0) return 'Expired'
    const diffMins = Math.floor(diffMs / 60000)
    const hours = Math.floor(diffMins / 60)
    const mins = diffMins % 60
    return hours > 0 ? `${hours}h ${mins}m remaining` : `${mins}m remaining`
  }

  if (isLoading || !user) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mb-4"></div>
        <p className="text-navy-600">Loading...</p>
      </div>
    )
  }

  const orderId = searchParams.get('order')

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-navy-900">Connection Access</h1>
          <p className="text-navy-600">Manage your Wi-Fi rental sessions</p>
        </div>
        <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-white border border-navy-200">
          <span className="text-sm font-medium text-navy-700">Balance:</span>
          <span className="text-sm font-bold text-teal-600">
            {walletBalance !== null ? (walletBalance / 100).toFixed(2) : '--'} DEMO_CREDITS
          </span>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      {orderId && (
        <div className="bg-blue-50 border border-blue-200 text-blue-600 px-4 py-3 rounded-lg text-sm">
          Order #{orderId} - Please refresh to see updated session status.
        </div>
      )}

      {!session || !session.hasActiveSession ? (
        <div className="card p-8 text-center">
          <div className="w-16 h-16 rounded-lg bg-navy-100 flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-navy-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-navy-900 mb-2">No Active Session</h2>
          <p className="text-navy-600 mb-6">
            You don't have an active Wi-Fi session. Purchase a package to get internet access.
          </p>
          <button
            onClick={() => navigate('/rent')}
            className="btn-primary"
          >
            Rent Wi-Fi Now
          </button>
        </div>
      ) : (
        <div className="card p-6">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-xl font-bold text-navy-900">Active Session</h2>
              <p className="text-navy-600">Session #{session.session?.id}</p>
            </div>
            <span className={`badge ${session.session.status === 'ACTIVE' ? 'text-green-600 bg-green-50' : 'text-gray-600 bg-gray-50'}`}>
              {session.session.status}
            </span>
          </div>

          <div className="mt-6 grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-navy-50 p-3 rounded-lg">
              <p className="text-xs text-navy-500 mb-1">Time Remaining</p>
              <p className="text-lg font-bold text-navy-900">{formatTimeRemaining(session.session.expiresAt)}</p>
            </div>
            <div className="bg-navy-50 p-3 rounded-lg">
              <p className="text-xs text-navy-500 mb-1">Data Remaining</p>
              <p className="text-lg font-bold text-navy-900">
                {session.session.dataQuotaBytes !== null && session.session.consumedBytes !== null
                  ? formatBytes(session.session.dataQuotaBytes - session.session.consumedBytes)
                  : 'Unlimited'}
              </p>
            </div>
            <div className="bg-navy-50 p-3 rounded-lg">
              <p className="text-xs text-navy-500 mb-1">Started</p>
              <p className="text-sm font-medium text-navy-900">{session.session.startedAt || 'N/A'}</p>
            </div>
            <div className="bg-navy-50 p-3 rounded-lg">
              <p className="text-xs text-navy-500 mb-1">Expires</p>
              <p className="text-sm font-medium text-navy-900">{session.session.expiresAt || 'N/A'}</p>
            </div>
          </div>

          <div className="mt-6 p-4 bg-navy-50 rounded-lg border border-navy-100">
            <h3 className="text-sm font-semibold text-navy-900 mb-3">Connection Instructions</h3>
            <ol className="text-sm text-navy-600 space-y-2 list-disc pl-5">
              <li>Turn on Wi-Fi on your device</li>
              <li>Connect to the hotspot network (SSID shown at the hotspot location)</li>
              <li>Open your browser - you'll be redirected to the captive portal</li>
              <li>Enter your credentials or access code if prompted</li>
              <li>You're now connected! Your session will expire automatically.</li>
            </ol>
            <p className="mt-3 text-xs text-navy-400">
              <strong>Note:</strong> A successful payment does not guarantee internet access.
              The router/gateway must authorize your session. If you cannot connect, please
              contact the hotspot host.
            </p>
          </div>

          {session.session.status !== 'ACTIVE' && (
            <div className="mt-6 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
              <strong>Session Status:</strong> {session.session.status}. Internet access may not be available yet.
            </div>
          )}
        </div>
      )}
    </div>
  )
}
