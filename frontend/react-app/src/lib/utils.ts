import { navigate } from 'react-router-dom'

export const navigateTo = (path: string) => {
  navigate(path)
}

export const formatCurrency = (minorUnits: number, currency: string = 'DEMO_CREDITS'): string => {
  const major = (minorUnits / 100).toFixed(2)
  return `${major} ${currency}`
}

export const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const dm = 2
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i]
}

export const formatDate = (isoString: string | null | undefined): string => {
  if (!isoString) return 'N/A'
  try {
    return new Date(isoString).toLocaleString()
  } catch {
    return isoString
  }
}

export const getStatusColor = (status: string): string => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE': return 'text-green-600 bg-green-50'
    case 'PENDING': return 'text-yellow-600 bg-yellow-50'
    case 'EXPIRED': return 'text-gray-600 bg-gray-50'
    case 'REVOKED': return 'text-red-600 bg-red-50'
    case 'FAILED': return 'text-red-600 bg-red-50'
    case 'PAID': return 'text-green-600 bg-green-50'
    case 'AVAILABLE': return 'text-green-600 bg-green-50'
    default: return 'text-gray-600 bg-gray-50'
  }
}
