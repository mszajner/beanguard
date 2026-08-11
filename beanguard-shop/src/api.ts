declare global {
  interface Window {
    _env_?: { serverUrl?: string }
  }
}

const SERVER_URL = window._env_?.serverUrl ?? 'http://localhost:8000'

export class HttpError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message)
  }
}

export async function shopFetch(
  path: string,
  options: RequestInit = {},
): Promise<unknown> {
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')

  const response = await fetch(`${SERVER_URL}${path}`, { ...options, headers })

  if (!response.ok) {
    const text = await response.text().catch(() => response.statusText)
    throw new HttpError(response.status, text || `HTTP ${response.status}`)
  }

  const text = await response.text()
  if (!text) return {}
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}
