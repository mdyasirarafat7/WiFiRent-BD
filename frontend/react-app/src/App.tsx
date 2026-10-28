import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { Layout, ProtectedRoute, RoleRoute } from '@/components/Layout'
import LoginPage from '@/pages/LoginPage'
import RegisterPage from '@/pages/RegisterPage'
import RentPage from '@/pages/RentPage'
import AccessPage from '@/pages/AccessPage'
import HostDashboardPage from '@/pages/HostDashboardPage'
import ProfilePage from '@/pages/ProfilePage'
import GatewayManagementPage from '@/pages/GatewayManagementPage'

function App() {
  const { isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-navy-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600"></div>
      </div>
    )
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/"
          element={
            <Layout>
              <Routes>
                <Route path="rent" element={<RentPage />} />
                <Route path="rent/:hotspotId" element={<RentPage />} />
                <Route path="access" element={<AccessPage />} />
                <Route path="profile" element={<ProfilePage />} />
                <Route path="host" element={<HostDashboardPage />} />
                <Route path="host/hotspots/:hotspotId" element={<HostDashboardPage />} />
                <Route path="gateways" element={<GatewayManagementPage />} />
                <Route path="gateways/:deviceId" element={<GatewayManagementPage />} />
                <Route path="/" element={<Navigate to="/rent" replace />} />
              </Routes>
            </Layout>
          }
        />
        <Route path="*" element={<Navigate to="/rent" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
