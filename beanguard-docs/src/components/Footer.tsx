'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

import { Button } from '@/components/Button'
import { getNavigation } from '@/components/Navigation'
import { useSiteConfig } from '@/components/SiteConfig'
import { ui } from '@/lib/i18n'

function PageLink({
  label,
  page,
  previous = false,
}: {
  label: string
  page: { href: string; title: string }
  previous?: boolean
}) {
  return (
    <>
      <Button
        href={page.href}
        aria-label={`${label}: ${page.title}`}
        variant="secondary"
        arrow={previous ? 'left' : 'right'}
      >
        {label}
      </Button>
      <Link
        href={page.href}
        tabIndex={-1}
        aria-hidden="true"
        className="text-base font-semibold text-zinc-900 transition hover:text-zinc-600 dark:text-white dark:hover:text-zinc-300"
      >
        {page.title}
      </Link>
    </>
  )
}

function PageNavigation() {
  const pathname = usePathname()
  const { locale } = useSiteConfig()
  const allPages = getNavigation(locale).flatMap((group) => group.links)
  const currentPageIndex = allPages.findIndex((page) => page.href === pathname)

  if (currentPageIndex === -1) {
    return null
  }

  const previousPage = allPages[currentPageIndex - 1]
  const nextPage = allPages[currentPageIndex + 1]

  if (!previousPage && !nextPage) {
    return null
  }

  return (
    <div className="flex">
      {previousPage && (
        <div className="flex flex-col items-start gap-3">
          <PageLink label={ui[locale].previous} page={previousPage} previous />
        </div>
      )}
      {nextPage && (
        <div className="ml-auto flex flex-col items-end gap-3">
          <PageLink label={ui[locale].next} page={nextPage} />
        </div>
      )}
    </div>
  )
}

function SmallPrint() {
  const { locale } = useSiteConfig()
  const rightsReserved =
    locale === 'pl' ? 'Wszelkie prawa zastrzeżone.' : 'All rights reserved.'

  return (
    <div className="flex flex-col items-center justify-between gap-5 border-t border-zinc-900/5 pt-8 sm:flex-row dark:border-white/5">
      <p className="text-xs text-zinc-600 dark:text-zinc-400">
        &copy; {new Date().getFullYear()} REXOFT Mirosław Szajner.{' '}
        {rightsReserved}
      </p>
    </div>
  )
}

export function Footer() {
  return (
    <footer className="mx-auto w-full max-w-2xl space-y-10 pb-16 lg:max-w-5xl">
      <PageNavigation />
      <SmallPrint />
    </footer>
  )
}
