import React, { useEffect, useState } from 'react'
import { SERVER_URL } from '../utils/api'
import { AppConfig, ConfigContext, defaultConfig } from './configContext'

export const ConfigProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [config, setConfig] = useState<AppConfig>(defaultConfig)

  useEffect(() => {
    fetch(`${SERVER_URL}/api/open/config`)
      .then((r) => r.json())
      .then((data: AppConfig) => {
        setConfig(data)
        document.title = data.title
        document.documentElement.style.setProperty(
          '--color-primary',
          data.primaryColor,
        )
        if (data.faviconUrl) {
          const link =
            document.querySelector<HTMLLinkElement>('link[rel="icon"]') ??
            document.head.appendChild(
              Object.assign(document.createElement('link'), { rel: 'icon' }),
            )
          link.href = data.faviconUrl
        }
      })
      .catch(() => {})
  }, [])

  return (
    <ConfigContext.Provider value={config}>{children}</ConfigContext.Provider>
  )
}
