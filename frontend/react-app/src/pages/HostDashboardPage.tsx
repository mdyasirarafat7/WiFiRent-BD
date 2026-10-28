import { useState, useEffect } from 'react'
import { useAuth } from '@/context/AuthContext'
import { hotspotApi, packageApi, walletApi, gatewayApi } from '@/api/apiClient'
import { HotspotDto, PackagePlanDto, HostEarningsDto, GatewayDeviceDto } from '@/types'
import { Link, useNavigate } from 'react-router-dom'
import { formatCurrency, formatBytes, getStatusColor } from '@/lib/utils'

export default function HostDashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [hotspots, setHotspots] = useState<HotspotDto[]>([])
  const [packages, setPackages] = useState<PackagePlanDto[]>([])
  const [earnings, setEarnings] = useState<HostEarningsDto | null>(null)
  const [gateways, setGateways] = useState<GatewayDeviceDto[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedHotspotId, setSelectedHotspotId] = useState<number | null>(null)
  const [newHotspot, setNewHotspot] = useState({
    name: '',
    description: '',
    latitude: '',
    longitude: '',
    ssid: '',
    approximateArea: '',
  })
  const [newPackage, setNewPackage] = useState({
    priceMinorUnits: '',
    packageType: 'TIME_BASED' as const,
    durationMinutes: '',
    dataQuotaBytes: '',
  })
  const [newGateway, setNewGateway] = useState({
    deviceId: '',
    name: '',
    managementUrl: '',
    adapterType: 'MOCK' as const,
    hotspotId: '',
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!user) return
    loadData()
  }, [user])

  const loadData = async () => {
    try {
      const [hRes, eRes, gRes] = await Promise.all([
        hotspotApi.listHotspots({ page: 0, size: 20 }),
        walletApi.getEarnings(),
        gatewayApi.list(),
      ])
      setHotspots(hRes.data.content)
      setEarnings(eRes.data)
      setGateways(gRes.data)
    } catch (err) {
      setError('Failed to load dashboard data')
    } finally {
      setLoading(false)
    }
  }

  const loadPackagesForHotspot = async (id: number) => {
    try {
      const res = await hotspotApi.getPackages(id)
      setPackages(res.data.content || [])
      setSelectedHotspotId(id)
    } catch (err) {
      setError('Failed to load packages')
    }
  }

  const handleCreateHotspot = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await hotspotApi.createHotspot({
        name: newHotspot.name,
        description: newHotspot.description || null,
        latitude: parseFloat(newHotspot.latitude),
        longitude: parseFloat(newHotspot.longitude),
        ssid: newHotspot.ssid || null,
        approximateArea: newHotspot.approximateArea || null,
      })
      setSuccess('Hotspot created successfully!')
      setNewHotspot({
        name: '',
        description: '',
        latitude: '',
        longitude: '',
        ssid: '',
        approximateArea: '',
      })
      loadData()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create hotspot')
    }
  }

  const handleCreatePackage = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedHotspotId) {
      setError('Please select a hotspot first')
      return
    }
    try {
      await packageApi.create(selectedHotspotId, {
        priceMinorUnits: parseInt(newPackage.priceMinorUnits),
        packageType: newPackage.packageType,
        durationMinutes: newPackage.durationMinutes ? parseInt(newPackage.durationMinutes) : null,
        dataQuotaBytes: newPackage.dataQuotaBytes ? parseInt(newPackage.dataQuotaBytes) : null,
      })
      setSuccess('Package created successfully!')
      setNewPackage({
        priceMinorUnits: '',
        packageType: 'TIME_BASED',
        durationMinutes: '',
        dataQuotaBytes: '',
      })
      loadPackagesForHotspot(selectedHotspotId)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create package')
    }
  }

  const handleToggleAvailability = async (id: number, current: boolean) => {
    try {
      await hotspotApi.setAvailability(id, !current)
      loadData()
    } catch (err) {
      setError('Failed to update availability')
    }
  }

  const handleRegisterGateway = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await gatewayApi.register({
        deviceId: newGateway.deviceId,
        name: newGateway.name,
        managementUrl: newGateway.managementUrl || null,
        adapterType: newGateway.adapterType,
        hotspotId: newGateway.hotspotId ? parseInt(newGateway.hotspotId) : null,
      })
      setSuccess('Gateway registered successfully!')
      setNewGateway({
        deviceId: '',
        name: '',
        managementUrl: '',
        adapterType: 'MOCK',
        hotspotId: '',
      })
      loadData()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to register gateway')
    }
  }

  const handleTestConnection = async (deviceId: string) => {
    try {
      await gatewayApi.testConnection(deviceId)
      setSuccess('Gateway connection successful!')
      loadData()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Gateway connection failed')
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-navy-900">Host Dashboard</h1>
          <p className="text-navy-600">Manage your hotspots, packages, and gateways</p>
        </div>
        <Link
          to="/profile"
          className="text-teal-600 text-sm font-medium hover:underline"
        >
          View Profile
        </Link>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">{error}</div>
      )}
      {success && (
        <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg text-sm">{success}</div>
      )}

      {/* Earnings Summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="card p-5">
          <p className="text-sm text-navy-500">Gross Revenue</p>
          <p className="text-lg font-bold text-navy-900">
            {formatCurrency(earnings?.grossRevenueMinorUnits || 0)}
          </p>
        </div>
        <div className="card p-5">
          <p className="text-sm text-navy-500">Platform Commission</p>
          <p className="text-lg font-bold text-navy-900">
            {formatCurrency(earnings?.platformCommissionMinorUnits || 0)}
          </p>
        </div>
        <div className="card p-5">
          <p className="text-sm text-navy-500">Your Earnings</p>
          <p className="text-lg font-bold text-teal-600">
            {formatCurrency(earnings?.hostEarningsMinorUnits || 0)}
          </p>
        </div>
      </div>

      {/* Gateways */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold text-navy-900">Gateways</h3>
        <button
          onClick={() => (document.getElementById('create-gateway')?.scrollIntoView())}
          className="btn-outline text-sm"
        >
          + Register Gateway
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {gateways.map((g) => (
          <div key={g.deviceId} className="card p-4">
            <div className="flex items-start justify-between">
              <div>
                <h4 className="font-bold text-navy-900">{g.name}</h4>
                <p className="text-sm text-navy-600">{g.deviceId}</p>
                <div className="flex items-center gap-1 text-xs text-navy-500 mt-1">
                  <span>{g.adapterType}</span>
                  <span>{g.integrationMode}</span>
                </div>
              </div>
              <span className={`badge ${g.online ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'}`}>
                {g.online ? 'Online' : 'Offline'}
              </span>
            </div>
            <div className="mt-4 flex gap-2">
              <button
                onClick={() => handleTestConnection(g.deviceId)}
                className="px-3 py-1.5 text-sm bg-navy-50 text-navy-600 rounded hover:bg-navy-100"
              >
                Test Connection
              </button>
              <button
                onClick={() => navigate(`/gateways/${g.deviceId}`)}
                className="px-3 py-1.5 text-sm bg-blue-50 text-blue-600 rounded hover:bg-blue-100"
              >
                Manage
              </button>
            </div>
            <div className="mt-2 text-xs text-navy-500">
              {g.capabilities && g.capabilities.length > 0 ? (
                <div>
                  <span className="text-xs font-medium text-navy-600">Capabilities:</span>{' '}
                  {g.capabilities.join(', ')}
                </div>
              ) : null}
            </div>
          </div>
        ))}
      </div>

      {/* Hotspots */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold text-navy-900">Your Hotspots</h3>
        <button
          onClick={() => (document.getElementById('create-hotspot')?.scrollIntoView())}
          className="btn-outline text-sm"
        >
          + Add Hotspot
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {hotspots.map((h) => (
          <div key={h.id} className="card p-4">
            <div className="flex items-start justify-between">
              <div>
                <h4 className="font-bold text-navy-900">{h.name}</h4>
                <p className="text-sm text-navy-600">{h.description || ''}</p>
                <div className="flex items-center gap-1 text-xs text-navy-500 mt-1">
                  <span>{h.ssid || 'No SSID'}</span>
                  <span>{h.approximateArea || 'Area not set'}</span>
                </div>
              </div>
              <span className={`badge ${getStatusColor(h.status)}`}>{h.status}</span>
            </div>
            <div className="mt-4 flex gap-2">
              <button
                onClick={() => loadPackagesForHotspot(h.id)}
                className="px-3 py-1.5 text-sm bg-navy-50 text-navy-600 rounded hover:bg-navy-100"
              >
                Manage Packages
              </button>
              <button
                onClick={() => handleToggleAvailability(h.id, h.available)}
                className={`px-3 py-1.5 text-sm rounded ${h.available ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-green-50 text-green-600 hover:bg-green-100'}`}
              >
                {h.available ? 'Pause' : 'Activate'}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Create Hotspot Form */}
      <div id="create-hotspot" className="card p-6 mt-8">
        <h3 className="text-lg font-bold text-navy-900 mb-4">Create New Hotspot</h3>
        <form onSubmit={handleCreateHotspot} className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Hotspot Name</label>
            <input
              className="input-field"
              value={newHotspot.name}
              onChange={(e) => setNewHotspot({ ...newHotspot, name: e.target.value })}
              placeholder="e.g. Home Wi-Fi"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Description</label>
            <input
              className="input-field"
              value={newHotspot.description}
              onChange={(e) => setNewHotspot({ ...newHotspot, description: e.target.value })}
              placeholder="Optional description"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Latitude</label>
            <input
              className="input-field"
              type="number"
              step="any"
              value={newHotspot.latitude}
              onChange={(e) => setNewHotspot({ ...newHotspot, latitude: e.target.value })}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Longitude</label>
            <input
              className="input-field"
              type="number"
              step="any"
              value={newHotspot.longitude}
              onChange={(e) => setNewHotspot({ ...newHotspot, longitude: e.target.value })}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">SSID</label>
            <input
              className="input-field"
              value={newHotspot.ssid}
              onChange={(e) => setNewHotspot({ ...newHotspot, ssid: e.target.value })}
              placeholder="WiFi network name"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Approximate Area</label>
            <input
              className="input-field"
              value={newHotspot.approximateArea}
              onChange={(e) => setNewHotspot({ ...newHotspot, approximateArea: e.target.value })}
              placeholder="e.g. Gulshan, Dhaka"
            />
          </div>
          <div className="md:col-span-2">
            <button type="submit" className="btn-primary">
              Create Hotspot
            </button>
          </div>
        </form>
      </div>

      {/* Create Gateway Form */}
      <div id="create-gateway" className="card p-6 mt-8">
        <h3 className="text-lg font-bold text-navy-900 mb-4">Register Gateway</h3>
        <p className="text-sm text-navy-600 mb-4">
          Register a Wi-Fi gateway (OpenWrt router or mock) to manage customer sessions.
          Gateways in MOCK mode simulate authorization without real hardware.
        </p>
        <form onSubmit={handleRegisterGateway} className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Device ID</label>
            <input
              className="input-field"
              value={newGateway.deviceId}
              onChange={(e) => setNewGateway({ ...newGateway, deviceId: e.target.value })}
              placeholder="e.g. gw-001"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Name</label>
            <input
              className="input-field"
              value={newGateway.name}
              onChange={(e) => setNewGateway({ ...newGateway, name: e.target.value })}
              placeholder="e.g. Home Gateway"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Management URL</label>
            <input
              className="input-field"
              value={newGateway.managementUrl}
              onChange={(e) => setNewGateway({ ...newGateway, managementUrl: e.target.value })}
              placeholder="https://gateway.example.com:8443"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Adapter Type</label>
            <select
              className="input-field"
              value={newGateway.adapterType}
              onChange={(e) => setNewGateway({ ...newGateway, adapterType: e.target.value as any })}
            >
              <option value="MOCK">MOCK (Simulated)</option>
              <option value="OPENWRT">OpenWrt/openNDS</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 mb-1">Hotspot (optional)</label>
            <select
              className="input-field"
              value={newGateway.hotspotId}
              onChange={(e) => setNewGateway({ ...newGateway, hotspotId: e.target.value })}
            >
              <option value="">No hotspot (standalone)</option>
              {hotspots.map((h) => (
                <option key={h.id} value={h.id}>{h.name}</option>
              ))}
            </select>
          </div>
          <div className="md:col-span-2">
            <button type="submit" className="btn-primary">
              Register Gateway
            </button>
          </div>
        </form>
      </div>

      {/* Packages Section */}
      {selectedHotspotId && (
        <div className="card p-6 mt-8">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-navy-900">Manage Packages</h3>
            <button
              onClick={() => (document.getElementById('create-package')?.scrollIntoView())}
              className="btn-outline text-sm"
            >
              + Add Package
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {packages.map((p) => (
              <div key={p.id} className="card p-4">
                <h4 className="font-bold text-navy-900">{formatCurrency(p.priceMinorUnits)}</h4>
                <p className="text-sm text-navy-600">{p.packageType.replace('_', ' ')}</p>
                {p.durationMinutes && <p className="text-sm text-navy-500">{p.durationMinutes} minutes</p>}
                {p.dataQuotaBytes && <p className="text-sm text-navy-500">{formatBytes(p.dataQuotaBytes)}</p>}
                <span className={`badge ${getStatusColor(p.active ? 'ACTIVE' : 'INACTIVE')}`}>
                  {p.active ? 'Active' : 'Inactive'}
                </span>
              </div>
            ))}
          </div>

          <div id="create-package" className="mt-6 pt-6 border-t border-navy-100">
            <h4 className="text-md font-bold text-navy-900 mb-3">Create New Package</h4>
            <form onSubmit={handleCreatePackage} className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-navy-700 mb-1">Price (demo credits)</label>
                <input
                  className="input-field"
                  type="number"
                  value={newPackage.priceMinorUnits}
                  onChange={(e) => setNewPackage({ ...newPackage, priceMinorUnits: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-navy-700 mb-1">Package Type</label>
                <select
                  className="input-field"
                  value={newPackage.packageType}
                  onChange={(e) =>
                    setNewPackage({ ...newPackage, packageType: e.target.value as any })}
                >
                  <option value="TIME_BASED">Time Based</option>
                  <option value="DATA_BASED">Data Based</option>
                  <option value="TIME_AND_DATA">Time + Data</option>
                </select>
              </div>
              {newPackage.packageType === 'TIME_BASED' || newPackage.packageType === 'TIME_AND_DATA' ? (
                <div>
                  <label className="block text-sm font-medium text-navy-700 mb-1">Duration (minutes)</label>
                  <input
                    className="input-field"
                    type="number"
                    value={newPackage.durationMinutes}
                    onChange={(e) => setNewPackage({ ...newPackage, durationMinutes: e.target.value })}
                  />
                </div>
              ) : null}
              {newPackage.packageType === 'DATA_BASED' || newPackage.packageType === 'TIME_AND_DATA' ? (
                <div>
                  <label className="block text-sm font-medium text-navy-700 mb-1">Data Quota (GB)</label>
                  <input
                    className="input-field"
                    type="number"
                    step="0.01"
                    value={newPackage.dataQuotaBytes}
                    onChange={(e) => setNewPackage({ ...newPackage, dataQuotaBytes: e.target.value })}
                  />
                </div>
              ) : null}
              <div className="md:col-span-2">
                <button type="submit" className="btn-primary">
                  Create Package
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
