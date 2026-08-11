'use client'

import { motion } from 'framer-motion'
import { usePathname } from 'next/navigation'

import { Footer } from '@/components/Footer'
import { Header } from '@/components/Header'
import { Logo } from '@/components/Logo'
import { Navigation } from '@/components/Navigation'
import { SectionProvider, type Section } from '@/components/SectionProvider'
import { SiteConfigProvider, useSiteConfig } from '@/components/SiteConfig'
import { type Locale, ui } from '@/lib/i18n'

function SidebarLogo() {
  const { siteUrl, locale } = useSiteConfig()
  return (
    <div className="hidden lg:flex">
      <a href={siteUrl} aria-label={ui[locale].homeAriaLabel}>
        <Logo className="h-7" />
      </a>
    </div>
  )
}

export function Layout({
  children,
  locale,
  allSections,
  siteUrl,
}: {
  children: React.ReactNode
  locale: Locale
  allSections: Record<string, Array<Section>>
  siteUrl: string
}) {
  const pathname = usePathname()

  return (
    <SiteConfigProvider siteUrl={siteUrl} locale={locale}>
      <SectionProvider sections={allSections[pathname] ?? []}>
        <div className="h-full lg:ml-72 xl:ml-80">
          <motion.header
            layoutScroll
            className="contents lg:pointer-events-none lg:fixed lg:inset-0 lg:z-40 lg:flex"
          >
            <div className="contents lg:pointer-events-auto lg:block lg:w-72 lg:overflow-y-auto lg:border-r lg:border-zinc-900/10 lg:px-6 lg:pt-4 lg:pb-8 xl:w-80 lg:dark:border-white/10">
              <SidebarLogo />
              <Header />
              <Navigation className="hidden lg:mt-8 lg:block" />
            </div>
          </motion.header>
          <div className="relative flex h-full flex-col px-4 pt-14 sm:px-6 lg:px-8">
            <main className="flex-auto">{children}</main>
            <Footer />
          </div>
        </div>
      </SectionProvider>
    </SiteConfigProvider>
  )
}
