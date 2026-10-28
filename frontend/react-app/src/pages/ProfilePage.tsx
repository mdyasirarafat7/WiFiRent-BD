import { useEffect, useState } from 'react'
import { useAuth } from '@/context/AuthContext'
import { authApi, walletApi } from '@/api/apiClient'
import { formatCurrency } from '@/lib/utils'

export default function ProfilePage() {
  const { user, logout } = useAuth()
  const [balance, setBalance] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadBalance()
  }, [])

  const loadBalance = async () => {
    try {
      const res = await walletApi.getBalance()
      setBalance(res.data.balanceMinorUnits)
    } catch (err) {
      console.error('Failed to load balance', err)
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = async () => {
    await logout()
  }

  if (loading || !user) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600"></div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-bold text-navy-900">Profile</h1>
        <button
          onClick={handleLogout}
          className="px-4 py-2 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 transition-colors"
        >
          Logout
        </button>
      </div>

      <div className="card p-6">
        <h2 className="text-lg font-bold text-navy-900 mb-4">Account Information</h2>
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-teal-600 flex items-center justify-center text-white font-bold">
              {user.name.charAt(0).toUpperCase()}
            </div>
            <div>
              <p className="font-bold text-navy-900">{user.name}</p>
              <p className="text-sm text-navy-500">{user.email}</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm font-medium text-navy-700">Role:</span>
            <span className={`badge text-sm ${user.role === 'ADMIN' ? 'text-purple-600 bg-purple-50' : user.role === 'HOST' ? 'text-indigo-600 bg-indigo-50' : 'text-teal-600 bg-teal-50'}`}>
              {user.role}
            </span>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm font-medium text-navy-700">Status:</span>
            <span className={`badge ${user.enabled ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'}`}>
              {user.enabled ? 'Active' : 'Disabled'}
            </span>
          </div>
        </div>
      </div>

      <div className="card p-6">
        <h2 className="text-lg font-bold text-navy-900 mb-4">Demo Credits Wallet</h2>
        <div className="flex items-center justify-between mb-4">
          <span className="text-lg font-medium text-navy-700">Current Balance</span>
          <span className="text-2xl font-bold text-teal-600">
            {balance !== null ? (balance / 100).toFixed(2) : '--'} DEMO_CREDITS
          </span>
        </div>
        <div className="bg-navy-50 p-4 rounded-lg">
          <p className="text-sm text-navy-600 mb-2">
            Demo credits are virtual currency used within WiFiRent BD. They have no real monetary value
            and cannot be withdrawn or redeemed for cash.
          </p>
          <p className="text-sm text-navy-600">
            You receive {formatCurrency(5000)} when you register a new account.
          </p>
        </div>
      </div>
    </div>
  )
}
