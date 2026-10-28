import { useState, useEffect } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useNavigate } from 'react-router-dom'
import { HotspotCard, PackageCard } from '@/components/HotspotCard'
import { hotspotApi, walletApi } from '@/api/apiClient'
import { HotspotDto, PackagePlanDto } from '@/types'

export default function RentPage() {
  const { user, isLoading } = useAuth()
  const navigate = useNavigate()
  const [hotspots, setHotspots] = useState<HotspotDto[]>([])
  const [packages, setPackages] = useState<PackagePlanDto[]>([])
  const [selectedHotspot, setSelectedHotspot] = useState<HotspotDto | null>(null)
  const [selectedPackage, setSelectedPackage] = useState<PackagePlanDto | null>(null)
  const [walletBalance, setWalletBalance] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState<{ message: string; orderId: number } | null>(null)

  useEffect(() => {
    if (!user) return
    loadHotspots()
    loadWalletBalance()
  }, [user])

  const loadHotspots = async () => {
    try {
      const res = await hotspotApi.list({ page: 0, size: 20 })
      setHotspots(res.data.content)
    } catch (err) {
      console.error('Failed to load hotspots', err)
    }
  }

  const loadWalletBalance = async () => {
    try {
      const res = await walletApi.getBalance()
      setWalletBalance(res.data.balanceMinorUnits)
    } catch (err) {
      console.error('Failed to load wallet balance', err)
    }
  }

  const handleSelectHotspot = (hotspot: HotspotDto) => {
    setSelectedHotspot(hotspot)
    setSelectedPackage(null)
    setError('')
    setSuccess(null)
    loadPackages(hotspot.id)
  }

  const loadPackages = async (hotspotId: number) => {
    try {
      const res = await hotspotApi.getPackages(hotspotId)
      setPackages(res.data.content || [])
    } catch (err) {
      console.error('Failed to load packages', err)
    }
  }

  const handlePurchase = async () => {
    if (!selectedHotspot || !selectedPackage || !user) return

    setLoading(true)
    setError('')
    setSuccess(null)

    const idempotencyKey = `purchase-${user.id}-${Date.now()}-${Math.random().toString(36).slice(2)}`

    try {
      const res = await walletApi.purchase({
        hotspotId: selectedHotspot.id,
        packageId: selectedPackage.id,
        idempotencyKey,
      })

      const data = res.data
      setSuccess({
        message: `Purchase successful! ${data.customerDebitedMinorUnits} demo credits debited.`,
        orderId: data.orderId,
      })
      setWalletBalance(data.customerRemainingBalanceMinorUnits)
      setSelectedPackage(null)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Purchase failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (isLoading || !user) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mb-4"></div>
        <p className="text-navy-600">Loading...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-navy-900">Rent Wi-Fi</h1>
          <p className="text-navy-600">Discover hotspots and rent internet access</p>
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

      {success && (
        <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg text-sm">
          {success.message}
          <button
            onClick={() => navigate(`/access?order=${success.orderId}`)}
            className="mt-2 text-teal-600 text-sm font-medium underline"
          >
            Go to Access Page →
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {hotspots.map((hotspot) => (
          <HotspotCard
            key={hotspot.id}
            hotspot={hotspot}
            onClick={() => handleSelectHotspot(hotspot)}
          />
        ))}
      </div>

      {selectedHotspot && (
        <div className="bg-white rounded-xl border border-navy-100 p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-xl font-bold text-navy-900">{selectedHotspot.name}</h2>
              <p className="text-navy-600">{selectedHotspot.description || 'Wi-Fi hotspot'}</p>
              <div className="flex items-center gap-2 text-sm text-navy-500 mt-2">
                <span>{selectedHotspot.approximateArea || 'Area not specified'}</span>
                {selectedHotspot.ssid && <span>• SSID: {selectedHotspot.ssid}</span>}
              </div>
            </div>
            <button
              onClick={() => setSelectedHotspot(null)}
              className="text-navy-400 hover:text-teal-600"
            >
              Back
            </button>
          </div>

          <h3 className="text-lg font-semibold text-navy-900 mb-4">Choose a Package</h3>

          {packages.length === 0 ? (
            <div className="text-center py-8 text-navy-500">
              No packages available for this hotspot.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {packages.map((pkg) => (
                <PackageCard
                  key={pkg.id}
                  package={pkg}
                  onSelect={setSelectedPackage}
                  selectedPackageId={selectedPackage?.id ?? null}
                />
              ))}
            </div>
          )}

          {selectedPackage && (
            <div className="mt-6 pt-6 border-t border-navy-100">
              <div className="flex items-center justify-between mb-4">
                <span className="text-sm font-medium text-navy-600">Purchase Confirmation</span>
                <span className="text-lg font-bold text-navy-900">
                  ${(selectedPackage.priceMinorUnits / 100).toFixed(2)} DEMO_CREDITS
                </span>
              </div>
              <p className="text-sm text-navy-600 mb-4">
                You will be charged{' '}
                <span className="font-bold">${(selectedPackage.priceMinorUnits / 100).toFixed(2)} DEMO_CREDITS</span>
                from your demo credits wallet.
              </p>
              <div className="flex items-center gap-3 mb-4">
                <span className={`badge text-sm ${selectedPackage.packageType === 'DATA_BASED' ? 'text-blue-600 bg-blue-50' : selectedPackage.packageType === 'TIME_BASED' ? 'text-indigo-600 bg-indigo-50' : 'text-purple-600 bg-purple-50'}`}>
                  {selectedPackage.packageType.replace('_', ' ')}
                </span>
                {selectedPackage.durationMinutes && (
                  <span className="badge text-navy-600 bg-navy-50">{selectedPackage.durationMinutes} min</span>
                )}
                {selectedPackage.dataQuotaBytes && (
                  <span className="badge text-navy-600 bg-navy-50">
                    {(selectedPackage.dataQuotaBytes / (1024 * 1024 * 1024)).toFixed(2)} GB
                  </span>
                )}
              </div>

              {walletBalance !== null && walletBalance < selectedPackage.priceMinorUnits && (
                <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                  Insufficient demo credits. You have ${(walletBalance / 100).toFixed(2)} DEMO_CREDITS but need ${(selectedPackage.priceMinorUnits / 100).toFixed(2)} DEMO_CREDITS.
                </div>
              )}

              <button
                onClick={handlePurchase}
                disabled={loading || (walletBalance !== null && walletBalance < selectedPackage.priceMinorUnits) || !selectedPackage.active}
                className={`w-full py-3 rounded-lg font-medium transition-colors ${
                  loading
                    ? 'bg-teal-300 text-white cursor-not-allowed'
                    : walletBalance !== null && walletBalance < selectedPackage.priceMinorUnits
                    ? 'bg-teal-200 text-teal-600 cursor-not-allowed'
                    : 'bg-teal-600 text-white hover:bg-teal-700'
                }`}
              >
                {loading ? 'Processing purchase...' : 'Confirm Purchase'}
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
