'use client'

import { trackPageView } from '@/lib/gtag'
import { usePathname } from 'next/navigation'
import { useEffect } from 'react'

export function Analytics({ gaId }: { gaId: string }) {
  const pathname = usePathname()

  useEffect(() => {
    if (gaId) trackPageView(gaId, pathname)
  }, [gaId, pathname])

  return null
}
