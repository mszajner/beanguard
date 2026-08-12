'use client'

import { Button } from '@/components/Button'
import { LanguageSwitcher } from '@/components/LanguageSwitcher'
import { Logo } from '@/components/Logo'
import { useSiteConfig } from '@/components/SiteConfig'
import { ThemeToggle } from '@/components/ThemeToggle'
import { ui } from '@/lib/i18n'

const navByLocale = {
  en: [
    { href: '#how', label: 'How it works' },
    { href: '#security', label: 'Security' },
    { href: '#code', label: 'Client library' },
    { href: '#license', label: 'License' },
  ],
  pl: [
    { href: '#how', label: 'Jak to działa' },
    { href: '#security', label: 'Bezpieczeństwo' },
    { href: '#code', label: 'Biblioteka klienta' },
    { href: '#license', label: 'Licencja' },
  ],
} as const

const quickStartHref = {
  en: '/en/quick-start',
  pl: '/pl/szybki-start',
} as const

export function MarketingHeader() {
  const { locale } = useSiteConfig()

  return (
    <div className="sticky top-0 z-50 border-b border-zinc-900/7.5 bg-white/85 backdrop-blur-sm dark:border-white/7.5 dark:bg-zinc-900/85">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-6 px-6 py-4">
        <a
          href={`/${locale}`}
          aria-label={ui[locale].homeAriaLabel}
          className="flex items-center gap-2.5"
        >
          <Logo className="h-9" />
        </a>

        <nav className="hidden items-center gap-7 text-sm text-zinc-600 md:flex dark:text-zinc-400">
          {navByLocale[locale].map((item) => (
            <a
              key={item.href}
              href={item.href}
              className="transition hover:text-zinc-900 dark:hover:text-white"
            >
              {item.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          <LanguageSwitcher />
          <ThemeToggle />
          <Button
            href={quickStartHref[locale]}
            className="hidden sm:inline-flex"
          >
            {locale === 'pl' ? 'Zaczynajmy' : 'Get started'}
          </Button>
        </div>
      </div>
    </div>
  )
}
