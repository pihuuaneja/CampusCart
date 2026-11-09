import axios from 'axios'

const TOKEN_KEY = 'campuscart.token'

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api' })

api.interceptors.request.use((config) => {
  const token = tokenStore.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

/** Turns any axios error into a readable sentence. */
export function errorMessage(err) {
  if (err.response?.data?.message) return err.response.data.message
  if (err.response?.status === 401) return 'Please log in to continue.'
  if (err.response?.status === 413) return 'That photo is too large. The limit is 5 MB.'
  if (err.code === 'ERR_NETWORK') return 'Cannot reach the server. Check that the backend is running.'
  return 'Something went wrong. Please try again.'
}

/** Uploaded photos are served by the backend at /uploads/... */
export const assetUrl = (url) =>
  !url ? null : /^https?:/.test(url) ? url : (import.meta.env.VITE_ASSET_URL || '') + url

export const formatPrice = (value) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value)

export const PERIOD_LABEL = { DAY: 'day', WEEK: 'week', MONTH: 'month' }

export const priceLabel = (l) =>
  l.type === 'RENT' ? `${formatPrice(l.price)} / ${PERIOD_LABEL[l.rentPeriod] || 'period'}` : formatPrice(l.price)

export const timeAgo = (iso) => {
  const s = Math.max(1, Math.floor((Date.now() - new Date(iso).getTime()) / 1000))
  if (s < 60) return 'just now'
  if (s < 3600) return `${Math.floor(s / 60)} min ago`
  if (s < 86400) return `${Math.floor(s / 3600)} h ago`
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })
}

export default api
