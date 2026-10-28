import { HotspotDto, PackagePlanDto } from '@/types'
import { formatCurrency, getStatusColor } from '@/lib/utils'
import { WalletMinus } from 'lucide-react'

export const HotspotCard = ({ hotspot, onClick }: { hotspot: HotspotDto; onClick: () => void }) => {
  return (
    <div
      className={`card cursor-pointer p-5 ${onClick ? 'hover:border-teal-500/50' : ''}`}
      onClick={onClick}
    >
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-2">
          <svg className="w-5 h-5 text-teal-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
          <h3 className="font-semibold text-navy-900">{hotspot.name}</h3>
        </div>
        <span className={`badge ${getStatusColor(hotspot.status)}`}>{hotspot.status}</span>
      </div>
      <p className="text-navy-600 text-sm mb-4">{hotspot.description || 'No description available'}</p>
      <div className="flex items-center justify-between text-xs text-navy-500 mb-4">
        <span>
          <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          {hotspot.approximateArea || 'Location not specified'}
        </span>
        <span>
          {hotspot.ssid ? `SSID: ${hotspot.ssid}` : 'SSID not set'}
        </span>
      </div>
      <div className="flex items-center justify-between pt-4 border-t border-navy-50">
        <span className="text-sm font-medium text-navy-700">
          {hotspot.available ? 'Available' : 'Unavailable'}
        </span>
        <span className={`text-xs font-bold ${getStatusColor(hotspot.available ? 'AVAILABLE' : 'UNAVAILABLE')}`}>
          {hotspot.available ? 'Available' : 'Unavailable'}
        </span>
      </div>
    </div>
  )
}

export const PackageCard = ({ 
  pkg: packageItem, 
  onSelect,
  selectedPackageId,
}: { 
  pkg: PackagePlanDto; 
  onSelect: (pkg: PackagePlanDto) => void;
  selectedPackageId: number | null;
}) => {
  const isSelected = selectedPackageId === packageItem.id
  
  return (
    <div
      className={`card p-4 rounded-xl border-2 cursor-pointer transition-all ${
        isSelected
          ? 'border-teal-500 bg-teal-50 ring-1 ring-teal-500'
          : 'border-navy-100 hover:border-teal-300'
      }`}
      onClick={() => onSelect(packageItem)}
    >
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-2">
          <span className={`badge ${getStatusColor(packageItem.packageType)}`}>
            {packageItem.packageType.replace('_', ' ')}
          </span>
          {packageItem.active ? (
            <span className="badge text-green-600 bg-green-50">Active</span>
          ) : (
            <span className="badge text-gray-600 bg-gray-50">Inactive</span>
          )}
        </div>
        <span className="text-xl font-bold text-navy-900">{formatCurrency(packageItem.priceMinorUnits)}</span>
      </div>
      
      <div className="space-y-2 text-sm text-navy-600 mb-4">
        {packageItem.durationMinutes && (
          <div className="flex items-center gap-2">
            <svg className="w-4 h-4 text-navy-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{packageItem.durationMinutes} minutes</span>
          </div>
        )}
        {packageItem.dataQuotaBytes && (
          <div className="flex items-center gap-2">
            <svg className="w-4 h-4 text-navy-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            <span>{(packageItem.dataQuotaBytes / (1024 * 1024 * 1024)).toFixed(2)} GB</span>
          </div>
        )}
        {packageItem.speedLimitKbps && (
          <div className="flex items-center gap-2">
            <svg className="w-4 h-4 text-navy-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V4M4 7c0 2.21-3.582 4-8 4S0 6.21 0 4s3.582-4 8-4m8 9v2M4 7v2m8-4v2m8-4v2m-8 6v-2m-8 4v2m8 0v2M12 7v2" />
            </svg>
            <span>{(packageItem.speedLimitKbps / 1000).toFixed(1)} Mbps</span>
          </div>
        )}
      </div>
 
      <button
        onClick={() => onSelect(packageItem)}
        className={`w-full py-2 rounded-lg font-medium transition-colors ${
          isSelected
            ? 'bg-teal-600 text-white'
            : 'bg-navy-50 text-navy-600 hover:bg-teal-50 hover:text-teal-600'
        }`}
        disabled={!packageItem.active}
      >
        {isSelected ? 'Selected' : 'Select Package'}
      </button>
    </div>
  )
}

export const WalletBalance = ({ className }: { className?: string }) => {
  // This is a display-only component; the actual balance is fetched in pages that need it
  return (
    <div className={`flex items-center gap-2 px-3 py-2 rounded-lg bg-white border border-navy-200 ${className}`}>
      <WalletMinus size={18} className="text-teal-600" />
      <span className="text-sm font-medium text-navy-900">Demo Credits</span>
      <span className="text-sm font-bold text-teal-600" id="wallet-balance-display">--</span>
    </div>
  )
}
