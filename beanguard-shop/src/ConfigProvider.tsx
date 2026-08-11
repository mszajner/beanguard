import React, { useEffect, useState } from 'react'
import { shopFetch } from './api'
import { AppConfig, ConfigContext, defaultConfig } from './configContext'

export const ConfigProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [config, setConfig] = useState<AppConfig>(defaultConfig)

  useEffect(() => {
    shopFetch('/api/open/config')
      .then((data) => {
        const cfg = data as AppConfig
        setConfig(cfg)
        document.title = `${cfg.title} — Sklep`
        document.documentElement.style.setProperty(
          '--color-primary',
          cfg.primaryColor,
        )
        if (cfg.faviconUrl) {
          const link =
            document.querySelector<HTMLLinkElement>('link[rel="icon"]') ??
            document.head.appendChild(
              Object.assign(document.createElement('link'), { rel: 'icon' }),
            )
          link.href = cfg.faviconUrl
        }
      })
      .catch(() => {})
  }, [])

  return (
    <ConfigContext.Provider value={config}>{children}</ConfigContext.Provider>
  )
}
