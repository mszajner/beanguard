import { type Metadata } from 'next'

export const metadata: Metadata = {
  title: {
    template: '%s — BeanGuard',
    default: 'BeanGuard — Licensing infrastructure for Java & Spring Boot',
  },
}

export default function LocaleLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return children
}
