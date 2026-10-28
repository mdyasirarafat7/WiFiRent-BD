import { useState, useEffect } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useNavigate, useParams } from 'react-router-dom'
import { gatewayApi } from '@/api/apiClient'
import { GatewayDeviceDto, GatewayHealthDto } from '@/types'
import { Link } from 'react-router-dom'

export default function GatewayManagementPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { deviceId } = useParams()
  const [gateway, setGateway] = useState<GatewayDeviceDto | null>(null)
  const [gateways, setGateways] = useState<GatewayDeviceDto[]>([])
  const [health, setHealth] = useState<GatewayHealthDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!user) return
    if (deviceId) {
      loadGateway(deviceId)
    } else {
      loadGateways()
    }
  }, [user, deviceId])

  const loadGateways = async () => {
    try {
      const res = await gatewayApi.list()
      setGateways(res.data)
    } catch (err) {
      setError('Failed to load gateways')
    } finally {
      setLoading(false)
    }
  }

  const loadGateway = async (id: string) => {
    try {
      const [gRes, hRes] = await Promise.all([
        gatewayApi.get(id),
        gatewayApi.health(id),
      ])
      setGateway(gRes.data)
      setHealth(hRes.data)
    } catch (err: any) {
      setError('Failed to load gateway details')
    } finally {
      setLoading(false)
    }
  }

  const handleTestConnection = async () => {
    if (!gateway?.deviceId) return
    try {
      await gatewayApi.testConnection(gateway.deviceId)
      setSuccess('Gateway connection test successful!')
      await loadGateway(gateway.deviceId)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Gateway connection test failed')
    }
  }

  const handleAuthorize = (sessionId: number) => {
    if (!gateway?.deviceId) return
    navigate(`/rent/${sessionId}/authorize`)
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
        <Link to="/host" className="text-teal-600 hover:underline">
          ← Back to Host Dashboard
        </Link>
        <h1 className="text-2xl font-bold text-navy-900">
          {deviceId ? 'Gateway Management' : 'Gateway Management'}
        </h1>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">{error}</div>
      )}
      {success && (
        <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg text-sm">{success}</div>
      )}

      {/* Gateway List */}
      <div className="card p-6">
        <h3 className="text-lg font-bold text-navy-900 mb-4">Registered Gateways</h3>
        {gateways.length === 0 ? (
          <div className="text-center py-8 text-navy-500">
            No gateways registered. Register your first gateway below.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {gateways.map((g) => (
              <div key={g.deviceId} className="card p-4">
                <div className="flex items-start justify-between">
                  <div>
                    <h4 className="font-bold text-navy-900">{g.name}</h4>
                    <p className="text-sm text-navy-600">{g.deviceId}</p>
                    <div className="flex items-center gap-1 text-xs text-navy-500 mt-1">
                      <span className={`badge ${g.adapterType === 'OPENWRT' ? 'text-blue-600 bg-blue-50' : 'text-teal-600 bg-teal-50'}`}>
                        {g.adapterType}
                      </span>
                      <span className={`badge ${g.integrationMode === 'REAL' ? 'text-green-600 bg-green-50' : 'text-gray-600 bg-gray-50'}`}>
                        {g.integrationMode}
                      </span>
                    </div>
                  </div>
                  <span className={`badge ${g.online ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'}`}>
                    {g.online ? 'Online' : 'Offline'}
                  </span>
                </div>
                <div className="mt-4 flex gap-2">
                  <Link
                    to={`/gateways/${g.deviceId}`}
                    className="px-3 py-1.5 text-sm bg-navy-50 text-navy-600 rounded hover:bg-navy-100"
                  >
                    Manage
                  </Link>
                  {g.online && (
                    <button
                      onClick={() => {
                        navigate(`/gateways/${g.deviceId}`)
                      }}
                      className="px-3 py-1.5 text-sm bg-teal-50 text-teal-600 rounded hover:bg-teal-100"
                    >
                      Test
                    </button>
                  )}
                </div>
                {g.capabilities && g.capabilities.length > 0 && (
                  <div className="mt-2">
                    <span className="text-xs font-medium text-navy-700">Capabilities:</span>
                    <div className="flex flex-wrap gap-1 mt-1">
                      {g.capabilities.map((cap) => (
                        <span key={cap} className="text-xs text-navy-500 bg-navy-100 px-2 py-0.5 rounded">
                          {cap}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Gateway Detail */}
      {deviceId && gateway && (
        <div className="card p-6 mt-8">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-navy-900">Gateway Details: {gateway.name}</h3>
            <Link to="/gateways" className="text-sm text-navy-600 hover:underline">
              ← Back to Gateways
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Device ID</span>
                <span className="text-sm font-mono text-navy-900">{gateway.deviceId}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Name</span>
                <span className="text-sm text-navy-900">{gateway.name}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Adapter Type</span>
                <span className={`badge ${gateway.adapterType === 'OPENWRT' ? 'text-blue-600 bg-blue-50' : 'text-teal-600 bg-teal-50'}`}>
                  {gateway.adapterType}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Integration Mode</span>
                <span className={`badge ${gateway.integrationMode === 'REAL' ? 'text-green-600 bg-green-50' : 'text-gray-600 bg-gray-50'}`}>
                  {gateway.integrationMode}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Status</span>
                <span className={`badge ${gateway.online ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'}`}>
                  {gateway.status}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Management URL</span>
                <span className="text-sm font-mono text-navy-500 truncate max-w-[200px]">
                  {gateway.managementUrl || 'Not configured'}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Firmware Version</span>
                <span className="text-sm text-navy-500">{gateway.firmwareVersion || 'Not reported'}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Last Heartbeat</span>
                <span className="text-sm text-navy-500">{gateway.lastHeartbeatAt || 'Never'}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-navy-600">Hotspot</span>
                <span className="text-sm text-navy-500">{gateway.hotspotName || 'Not assigned'}</span>
              </div>
            </div>

            <div className="space-y-4">
              {gateway.capabilities && gateway.capabilities.length > 0 && (
                <div>
                  <h4 className="text-sm font-semibold text-navy-700 mb-2">Capabilities</h4>
                  <div className="flex flex-wrap gap-2">
                    {gateway.capabilities.map((cap) => (
                      <span key={cap} className="px-2 py-1 text-xs bg-blue-50 text-blue-700 rounded border border-blue-100">
                        {cap.replace(/_/g, ' ')}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              <div className="pt-4 border-t border-navy-100">
                <h4 className="text-sm font-semibold text-navy-700 mb-3">Health Check</h4>
                <div className="flex items-center gap-3">
                  <span className={`badge ${health?.online ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'}`}>
                    {health?.online ? 'Online' : 'Offline'}
                  </span>
                  <span className="text-sm text-navy-500">
                    Last heartbeat: {health?.lastHeartbeatAt || 'Never'}
                  </span>
                </div>
                <button
                  onClick={handleTestConnection}
                  className="w-full py-2.5 rounded-lg border border-navy-200 text-navy-600 font-medium hover:bg-navy-50"
                >
                  Test Connection
                </button>
              </div>

              {gateway.online && gateway.deviceId && (
                <div className="pt-4 border-t border-navy-100">
                  <h4 className="text-sm font-semibold text-navy-700 mb-3">Session Management</h4>
                  <p className="text-sm text-navy-600 mb-3">
                    Authorize or revoke rental sessions on this gateway.
                  </p>
                  <button
                    onClick={() => navigate('/rent')}
                    className="w-full py-2.5 rounded-lg bg-teal-600 text-white font-medium hover:bg-teal-700"
                  >
                    Create New Rental Session
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
