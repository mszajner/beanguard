import glob from 'fast-glob'
import { type Metadata } from 'next'

import { Layout } from '@/components/Layout'
import { type Section } from '@/components/SectionProvider'

export const metadata: Metadata = {
  title: {
    template: '%s — BeanGuard',
    default: 'BeanGuard — Documentation',
  },
}

export default async function DocsLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const siteUrl = process.env.SITE_URL ?? 'https://beanguard.dev'

  const pages = await glob('**/*.mdx', { cwd: 'src/app/en/(docs)' })
  const allSectionsEntries = (await Promise.all(
    pages.map(async (filename) => [
      '/en/' + filename.replace(/(^|\/)page\.mdx$/, ''),
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
    <Layout locale="en" allSections={allSections} siteUrl={siteUrl}>
      {children}
    </Layout>
  )
}
