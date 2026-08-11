import { createContext } from 'react'

export interface AppConfig {
  title: string
  logoUrl: string | null
  faviconUrl: string | null
  supportEmail: string | null
  primaryColor: string
  currency: string
  termsUrl: string | null
  privacyUrl: string | null
  vatRate: number | null
  companyName: string | null
  companyNip: string | null
  companyKrs: string | null
  companyAddress: string | null
  companyPhone: string | null
}

export const defaultConfig: AppConfig = {
  title: 'BeanGuard',
  logoUrl: null,
  faviconUrl: null,
  supportEmail: null,
  primaryColor: '#f59e0b',
  currency: 'PLN',
  termsUrl: null,
  privacyUrl: null,
  vatRate: 23,
  companyName: null,
  companyNip: null,
  companyKrs: null,
  companyAddress: null,
  companyPhone: null,
}

export const ConfigContext = createContext<AppConfig>(defaultConfig)
