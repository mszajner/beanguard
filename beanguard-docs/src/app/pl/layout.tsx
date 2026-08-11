import glob from 'fast-glob'
import { type Metadata } from 'next'

import { Layout } from '@/components/Layout'
import { type Section } from '@/components/SectionProvider'

export const metadata: Metadata = {
  title: {
    template: '%s — BeanGuard',
    default: 'BeanGuard — Dokumentacja',
  },
}

export default async function LocaleLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const siteUrl = process.env.SITE_URL ?? 'https://beanguard.dev'

  const pages = await glob('**/*.mdx', { cwd: 'src/app/pl' })
  const allSectionsEntries = (await Promise.all(
    pages.map(async (filename) => [
      '/pl/' + filename.replace(/(^|\/)page\.mdx$/, ''),
      (await import(`./${filename}`)).sections,
    ]),
  )) as Array<[string, Array<Section>]>
  const allSections = Object.fromEntries(
    allSectionsEntries.map(([url, sections]) => [
      url.endsWith('/') ? url.slice(0, -1) : url,
      sections,
    ]),
  )

  return (
    <Layout locale="pl" allSections={allSections} siteUrl={siteUrl}>
      {children}
    </Layout>
  )
}
