declare global {
  interface Window {
    gtag: (...args: unknown[]) => void
    dataLayer: unknown[]
  }
}

function call(...args: unknown[]) {
  if (typeof window !== 'undefined' && typeof window.gtag === 'function') {
    window.gtag(...args)
  }
}

export function trackPageView(gaId: string, path: string) {
  call('config', gaId, { page_path: path })
}

export function trackEvent(name: string, params?: Record<string, string>) {
  call('event', name, params)
}
