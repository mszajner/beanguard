'use client'

import { usePathname } from 'next/navigation'

import { translatePath } from '@/components/Navigation'
import { useSiteConfig } from '@/components/SiteConfig'
import { locales, localeCookieName } from '@/lib/i18n'

export function LanguageSwitcher() {
  const pathname = usePathname()
  const { locale } = useSiteConfig()

  return (
    <div className="flex items-center gap-1 rounded-full p-0.5 ring-1 ring-zinc-900/10 dark:ring-white/10">
      {locales.map((code) => (
        <a
          key={code}
          href={translatePath(pathname, code)}
          onClick={() => {
            document.cookie = `${localeCookieName}=${code}; path=/; max-age=31536000; samesite=lax`
          }}
          className={`rounded-full px-2 py-1 text-xs font-semibold uppercase transition-colors ${
            locale === code
              ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-900'
              : 'text-zinc-500 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-white'
          }`}
        >
          {code}
        </a>
      ))}
    </div>
  )
}
