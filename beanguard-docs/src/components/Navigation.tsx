'use client'

import clsx from 'clsx'
import { AnimatePresence, motion, useIsPresent } from 'framer-motion'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useRef } from 'react'

import { useIsInsideMobileNavigation } from '@/components/MobileNavigation'
import { useSectionStore } from '@/components/SectionProvider'
import { useSiteConfig } from '@/components/SiteConfig'
import { Tag } from '@/components/Tag'
import { trackEvent } from '@/lib/gtag'
import { localeFromPathname, locales, type Locale } from '@/lib/i18n'
import { remToPx } from '@/lib/remToPx'
import { CloseButton } from '@headlessui/react'

interface NavGroup {
  title: string
  links: Array<{
    title: string
    href: string
  }>
}

function useInitialValue<T>(value: T, condition = true) {
  const initialValue = useRef(value).current
  return condition ? initialValue : value
}

function NavLink({
  href,
  children,
  tag,
  active = false,
  isAnchorLink = false,
  onClick,
}: {
  href: string
  children: React.ReactNode
  tag?: string
  active?: boolean
  isAnchorLink?: boolean
  onClick?: () => void
}) {
  return (
    <CloseButton
      as={Link}
      href={href}
      aria-current={active ? 'page' : undefined}
      onClick={onClick}
      className={clsx(
        'flex justify-between gap-2 py-1 pr-3 text-sm transition',
        isAnchorLink ? 'pl-7' : 'pl-4',
        active
          ? 'text-zinc-900 dark:text-white'
          : 'text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-white',
      )}
    >
      <span className="truncate">{children}</span>
      {tag && (
        <Tag variant="small" color="zinc">
          {tag}
        </Tag>
      )}
    </CloseButton>
  )
}

function VisibleSectionHighlight({
  group,
  pathname,
}: {
  group: NavGroup
  pathname: string
}) {
  const [sections, visibleSections] = useInitialValue(
    [
      useSectionStore((s) => s.sections),
      useSectionStore((s) => s.visibleSections),
    ],
    useIsInsideMobileNavigation(),
  )

  const isPresent = useIsPresent()
  const firstVisibleSectionIndex = Math.max(
    0,
    [{ id: '_top' }, ...sections].findIndex(
      (section) => section.id === visibleSections[0],
    ),
  )
  const itemHeight = remToPx(2)
  const height = isPresent
    ? Math.max(1, visibleSections.length) * itemHeight
    : itemHeight
  const top =
    group.links.findIndex((link) => link.href === pathname) * itemHeight +
    firstVisibleSectionIndex * itemHeight

  return (
    <motion.div
      layout
      initial={{ opacity: 0 }}
      animate={{ opacity: 1, transition: { delay: 0.2 } }}
      exit={{ opacity: 0 }}
      className="absolute inset-x-0 top-0 bg-zinc-800/2.5 will-change-transform dark:bg-white/2.5"
      style={{ borderRadius: 8, height, top }}
    />
  )
}

function ActivePageMarker({
  group,
  pathname,
}: {
  group: NavGroup
  pathname: string
}) {
  const itemHeight = remToPx(2)
  const offset = remToPx(0.25)
  const activePageIndex = group.links.findIndex(
    (link) => link.href === pathname,
  )
  const top = offset + activePageIndex * itemHeight

  return (
    <motion.div
      layout
      className="absolute left-2 h-6 w-px bg-sky-500"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1, transition: { delay: 0.2 } }}
      exit={{ opacity: 0 }}
      style={{ top }}
    />
  )
}

function NavigationGroup({
  group,
  className,
}: {
  group: NavGroup
  className?: string
}) {
  // If this is the mobile navigation then we always render the initial
  // state, so that the state does not change during the close animation.
  // The state will still update when we re-open (re-render) the navigation.
  const isInsideMobileNavigation = useIsInsideMobileNavigation()
  const [pathname, sections] = useInitialValue(
    [usePathname(), useSectionStore((s) => s.sections)],
    isInsideMobileNavigation,
  )

  const isActiveGroup =
    group.links.findIndex((link) => link.href === pathname) !== -1

  return (
    <li className={clsx('relative mt-6', className)}>
      <motion.h2
        layout="position"
        className="text-xs font-semibold text-zinc-900 dark:text-white"
      >
        {group.title}
      </motion.h2>
      <div className="relative mt-3 pl-2">
        <AnimatePresence initial={!isInsideMobileNavigation}>
          {isActiveGroup && (
            <VisibleSectionHighlight group={group} pathname={pathname} />
          )}
        </AnimatePresence>
        <motion.div
          layout
          className="absolute inset-y-0 left-2 w-px bg-zinc-900/10 dark:bg-white/5"
        />
        <AnimatePresence initial={false}>
          {isActiveGroup && (
            <ActivePageMarker group={group} pathname={pathname} />
          )}
        </AnimatePresence>
        <ul role="list" className="border-l border-transparent">
          {group.links.map((link) => (
            <motion.li key={link.href} layout="position" className="relative">
              <NavLink
                href={link.href}
                active={link.href === pathname}
                onClick={() =>
                  trackEvent('nav_click', {
                    link_text: link.title,
                    link_url: link.href,
                    section: group.title,
                  })
                }
              >
                {link.title}
              </NavLink>
              <AnimatePresence mode="popLayout" initial={false}>
                {link.href === pathname && sections.length > 0 && (
                  <motion.ul
                    role="list"
                    initial={{ opacity: 0 }}
                    animate={{
                      opacity: 1,
                      transition: { delay: 0.1 },
                    }}
                    exit={{
                      opacity: 0,
                      transition: { duration: 0.15 },
                    }}
                  >
                    {sections.map((section) => (
                      <li key={section.id}>
                        <NavLink
                          href={`${link.href}#${section.id}`}
                          tag={section.tag}
                          isAnchorLink
                        >
                          {section.title}
                        </NavLink>
                      </li>
                    ))}
                  </motion.ul>
                )}
              </AnimatePresence>
            </motion.li>
          ))}
        </ul>
      </div>
    </li>
  )
}

const navigationByLocale: Record<Locale, Array<NavGroup>> = {
  pl: [
    {
      title: 'Pierwsze kroki',
      links: [
        { title: 'Wprowadzenie', href: '/pl' },
        { title: 'Architektura', href: '/pl/architektura' },
        { title: 'Szybki start', href: '/pl/szybki-start' },
        { title: 'Pobierz BeanGuard', href: '/pl/pobierz' },
      ],
    },
    {
      title: 'Serwer licencji',
      links: [
        { title: 'Uruchomienie', href: '/pl/serwer#uruchomienie' },
        { title: 'Zmienne środowiskowe', href: '/pl/serwer#zmienne' },
        { title: 'Parametry konfiguracyjne', href: '/pl/serwer#parametry' },
        { title: 'REST API', href: '/pl/serwer#api' },
      ],
    },
    {
      title: 'Panel admina',
      links: [
        { title: 'Przegląd', href: '/pl/panel-admina' },
        { title: 'Licencje', href: '/pl/panel-admina/licencje' },
        { title: 'Zamówienia', href: '/pl/panel-admina/zamowienia' },
        { title: 'Produkty', href: '/pl/panel-admina/produkty' },
        { title: 'Branding', href: '/pl/panel-admina/branding' },
        { title: 'Ustawienia', href: '/pl/panel-admina/ustawienia' },
        { title: 'Użytkownicy', href: '/pl/panel-admina/uzytkownicy' },
        { title: 'Szablony maili', href: '/pl/panel-admina/szablony-maili' },
        { title: 'Szablony PDF', href: '/pl/panel-admina/szablony-pdf' },
        {
          title: 'Klucze kryptograficzne',
          href: '/pl/panel-admina/klucze-kryptograficzne',
        },
        { title: 'Parametry', href: '/pl/panel-admina/parametry' },
      ],
    },
    {
      title: 'Sklep',
      links: [
        { title: 'Uruchomienie', href: '/pl/sklep#uruchomienie' },
        { title: 'Do czego służy', href: '/pl/sklep#funkcje' },
      ],
    },
    {
      title: 'Integracja klienta',
      links: [
        { title: 'Instalacja i konfiguracja', href: '/pl/klient' },
        { title: 'Adnotacje licencyjne', href: '/pl/klient/adnotacje' },
        {
          title: 'Ochrona kluczy ProGuardem',
          href: '/pl/klient/ochrona-kluczy',
        },
      ],
    },
    {
      title: 'Model licencji',
      links: [{ title: 'Licencje', href: '/pl/licencje' }],
    },
  ],
  en: [
    {
      title: 'Getting started',
      links: [
        { title: 'Introduction', href: '/en' },
        { title: 'Architecture', href: '/en/architecture' },
        { title: 'Quick start', href: '/en/quick-start' },
        { title: 'Download BeanGuard', href: '/en/download' },
      ],
    },
    {
      title: 'Licence server',
      links: [
        { title: 'Getting started', href: '/en/server#uruchomienie' },
        { title: 'Environment variables', href: '/en/server#zmienne' },
        { title: 'Configuration parameters', href: '/en/server#parametry' },
        { title: 'REST API', href: '/en/server#api' },
      ],
    },
    {
      title: 'Admin panel',
      links: [
        { title: 'Overview', href: '/en/admin-panel' },
        { title: 'Licences', href: '/en/admin-panel/licences' },
        { title: 'Orders', href: '/en/admin-panel/orders' },
        { title: 'Products', href: '/en/admin-panel/products' },
        { title: 'Branding', href: '/en/admin-panel/branding' },
        { title: 'Settings', href: '/en/admin-panel/settings' },
        { title: 'Users', href: '/en/admin-panel/users' },
        { title: 'Mail templates', href: '/en/admin-panel/mail-templates' },
        { title: 'PDF templates', href: '/en/admin-panel/pdf-templates' },
        {
          title: 'Cryptographic keys',
          href: '/en/admin-panel/cryptographic-keys',
        },
        { title: 'Parameters', href: '/en/admin-panel/parameters' },
      ],
    },
    {
      title: 'Shop',
      links: [
        { title: 'Getting started', href: '/en/shop#uruchomienie' },
        { title: 'What it does', href: '/en/shop#funkcje' },
      ],
    },
    {
      title: 'Client integration',
      links: [
        { title: 'Installation & configuration', href: '/en/client' },
        { title: 'Licensing annotations', href: '/en/client/annotations' },
        {
          title: 'Protecting keys with ProGuard',
          href: '/en/client/key-protection',
        },
      ],
    },
    {
      title: 'Licence model',
      links: [{ title: 'Licences', href: '/en/licence-model' }],
    },
  ],
}

export function getNavigation(locale: Locale): Array<NavGroup> {
  return navigationByLocale[locale]
}

function stripHash(href: string): string {
  return href.split('#')[0]
}

function buildPathMap(from: Locale, to: Locale): Record<string, string> {
  const map: Record<string, string> = {}
  const fromGroups = navigationByLocale[from]
  const toGroups = navigationByLocale[to]
  fromGroups.forEach((group, groupIndex) => {
    group.links.forEach((link, linkIndex) => {
      map[stripHash(link.href)] = stripHash(
        toGroups[groupIndex].links[linkIndex].href,
      )
    })
  })
  return map
}

const pathMaps: Record<Locale, Record<Locale, Record<string, string>>> =
  Object.fromEntries(
    locales.map((from) => [
      from,
      Object.fromEntries(
        locales.map((to) => [to, from === to ? {} : buildPathMap(from, to)]),
      ),
    ]),
  ) as Record<Locale, Record<Locale, Record<string, string>>>

/** Translates a pathname (optionally with a #hash) to its equivalent page in another locale. */
export function translatePath(pathname: string, targetLocale: Locale): string {
  const [path, hash] = pathname.split('#')
  const currentLocale = localeFromPathname(path)
  if (!currentLocale || currentLocale === targetLocale) {
    return pathname
  }
  const translatedPath = pathMaps[currentLocale][targetLocale][path] ?? `/${targetLocale}`
  return hash ? `${translatedPath}#${hash}` : translatedPath
}

export function Navigation(props: React.ComponentPropsWithoutRef<'nav'>) {
  const { locale } = useSiteConfig()
  const navigation = getNavigation(locale)

  return (
    <nav {...props}>
      <ul role="list">
        {navigation.map((group, groupIndex) => (
          <NavigationGroup
            key={group.title}
            group={group}
            className={groupIndex === 0 ? 'md:mt-0' : ''}
          />
        ))}
      </ul>
    </nav>
  )
}
