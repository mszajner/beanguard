'use client'

import { createContext, useContext } from 'react'

import { defaultLocale, type Locale } from '@/lib/i18n'

const SiteConfigContext = createContext({
  siteUrl: 'https://beanguard.dev',
  locale: defaultLocale as Locale,
})

export function SiteConfigProvider({
  siteUrl,
  locale,
  children,
}: {
  siteUrl: string
  locale: Locale
  children: React.ReactNode
}) {
  return (
    <SiteConfigContext.Provider value={{ siteUrl, locale }}>
      {children}
    </SiteConfigContext.Provider>
  )
}

export function useSiteConfig() {
  return useContext(SiteConfigContext)
}
