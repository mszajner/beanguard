'use client'

import { useSiteConfig } from '@/components/SiteConfig'

const linksByLocale = {
  en: [
    { href: '/en/quick-start', label: 'Documentation' },
    { href: 'https://github.com/mszajner/beanguard', label: 'GitHub' },
    { href: 'https://hub.docker.com/u/mszajner', label: 'Docker Hub' },
    {
      href: 'https://central.sonatype.com/artifact/dev.beanguard/beanguard-client',
      label: 'Maven Central',
    },
    { href: '/en/licence-model', label: 'License' },
  ],
  pl: [
    { href: '/pl/szybki-start', label: 'Dokumentacja' },
    { href: 'https://github.com/mszajner/beanguard', label: 'GitHub' },
    { href: 'https://hub.docker.com/u/mszajner', label: 'Docker Hub' },
    {
      href: 'https://central.sonatype.com/artifact/dev.beanguard/beanguard-client',
      label: 'Maven Central',
    },
    { href: '/pl/licencje', label: 'Licencja' },
  ],
} as const

export function MarketingFooter() {
  const { locale } = useSiteConfig()

  return (
    <footer className="border-t border-zinc-900/7.5 dark:border-white/7.5">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-5 px-6 py-10 text-sm sm:flex-row">
        <p className="font-[family-name:var(--font-plex-mono)] text-xs text-zinc-500 dark:text-zinc-500">
          BeanGuard · dev.beanguard
        </p>
        <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-zinc-600 dark:text-zinc-400">
          {linksByLocale[locale].map((link) => (
            <a
              key={link.href}
              href={link.href}
              className="transition hover:text-zinc-900 dark:hover:text-white"
            >
              {link.label}
            </a>
          ))}
        </div>
      </div>
    </footer>
  )
}
