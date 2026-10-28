import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { LogOut, User, Shield, Wifi, WifiOff, Home, Settings, LayoutDashboard, Wallet, History, Building2, WalletMinus } from 'lucide-react'

export const Navbar = () => {
  const { user, isAuthenticated, isLoading, logout } = useAuth()
  const navigate = useNavigate()

  if (isLoading) {
    return <nav className="bg-white border-b border-navy-200"><div className="container mx-auto px-4 py-3"><div className="flex items-center justify-between"><span className="text-teal-600 font-bold text-lg">Loading...</span></div></div></nav>
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <nav className="bg-white border-b border-navy-200 sticky top-0 z-50">
      <div className="container mx-auto px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-teal-600 flex items-center justify-center text-white font-bold">
            <Wifi size={20} />
          </div>
          <Link to="/" className="text-teal-600 font-bold text-lg tracking-tight">
            WiFiRent BD
          </Link>
        </div>

        <div className="hidden md:flex items-center gap-6">
          <Link to="/rent" className="text-navy-600 font-medium hover:text-teal-600 transition-colors">
            Rent Wi-Fi
          </Link>
          <Link to="/access" className="text-navy-600 font-medium hover:text-teal-600 transition-colors">
            Access
          </Link>
          {user?.role === 'HOST' || user?.role === 'ADMIN' ? (
            <>
              <Link to="/host" className="text-navy-600 font-medium hover:text-teal-600 transition-colors">
                Host Dashboard
              </Link>
            </>
          ) : null}
          <Link to="/profile" className="text-navy-600 font-medium hover:text-teal-600 transition-colors">
            Profile
          </Link>
        </div>

        <div className="flex items-center gap-3">
          {isAuthenticated && user ? (
            <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-navy-50 text-navy-900 text-sm font-medium">
              <User size={16} className="mr-1" />
              {user.name}
            </div>
          ) : null}
          {isAuthenticated ? (
            <button onClick={handleLogout} className="px-4 py-2 text-navy-600 hover:bg-navy-100 rounded-lg font-medium transition-colors">
              Logout
            </button>
          ) : (
            <Link to="/login" className="btn-primary">Login</Link>
          )}
        </div>
      </div>
    </nav>
  )
}

export const Layout = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAuth()

  return (
    <div className="min-h-screen bg-navy-50">
      <Navbar />
      <main className="container mx-auto px-4 py-8 max-w-7xl">{children}</main>
    </div>
  )
}

export const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mb-4"></div>
        <p className="text-navy-600">Loading...</p>
      </div>
    )
  }

  if (!isAuthenticated) {
    return null
  }

  return <>{children}</>
}

export const RoleRoute = ({ children, roles }: { children: React.ReactNode; roles: string[] }) => {
  const { user } = useAuth()

  if (!user || !roles.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
