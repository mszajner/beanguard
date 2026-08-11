declare global {
  interface Window {
    _env_?: { serverUrl?: string; shopUrl?: string; docsUrl?: string }
  }
}

export const SERVER_URL = window._env_?.serverUrl || 'http://localhost:8000'
export const SHOP_URL = window._env_?.shopUrl || 'http://localhost:8001'
export const DOCS_URL = window._env_?.docsUrl || 'http://localhost:8003'

export const apiFetch = async (endpoint: string, options: RequestInit = {}) => {
  const token = localStorage.getItem('token')
  const headers = new Headers(options.headers || {})

  headers.set('Content-Type', 'application/json')
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${SERVER_URL}${endpoint}`, {
    ...options,
    headers,
  })

  if (response.status === 401 || response.status === 403) {
    localStorage.removeItem('token')
    sessionStorage.setItem('sessionExpired', 'true')
    window.location.href = '/#/login'
    throw new Error('session_expired')
  }

  if (response.status === 402) {
    window.location.href = '/#/license'
    throw new Error('licence_required')
  }

  if (!response.ok) {
    const error = await response.text()
    throw new Error(error || response.statusText)
  }

  // Handle empty responses
  const text = await response.text()
  if (!text) return {}
  try {
    return JSON.parse(text)
  } catch {
    return text // Return plain text if it's not JSON
  }
}

export const apiFetchBlob = async (endpoint: string): Promise<Blob> => {
  const token = localStorage.getItem('token')
  const response = await fetch(`${SERVER_URL}${endpoint}`, {
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
      Accept: 'application/pdf',
    },
  })
  if (response.status === 401 || response.status === 403) {
    localStorage.removeItem('token')
    window.location.href = '/#/login'
    throw new Error('session_expired')
  }
  if (!response.ok) throw new Error(response.statusText)
  return response.blob()
}
