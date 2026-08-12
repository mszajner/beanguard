import { type Metadata } from 'next'
import { headers } from 'next/headers'
import Script from 'next/script'

import { Providers } from '@/app/providers'
import { Analytics } from '@/components/Analytics'
import { CookieConsentBanner } from '@/components/CookieConsentBanner'
import { defaultLocale, isLocale } from '@/lib/i18n'

import '@/styles/tailwind.css'

export const dynamic = 'force-dynamic'

export const metadata: Metadata = {
  title: {
    template: '%s — BeanGuard',
    default: 'BeanGuard — Documentation',
  },
}

export default async function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const gaId = process.env.GA_ID ?? ''

  const headersList = await headers()
  const headerLocale = headersList.get('x-locale')
  const lang =
    headerLocale && isLocale(headerLocale) ? headerLocale : defaultLocale

  return (
    <html lang={lang} className="h-full" suppressHydrationWarning>
      <body className="flex min-h-full bg-white antialiased dark:bg-zinc-900">
        {gaId && (
          <>
            <Script
              src={`https://www.googletagmanager.com/gtag/js?id=${gaId}`}
              strategy="afterInteractive"
            />
            <Script id="ga-init" strategy="afterInteractive">{`
              window.dataLayer = window.dataLayer || [];
              function gtag(){dataLayer.push(arguments);}
              window.gtag = gtag;
              gtag('consent', 'default', {
                analytics_storage: 'denied',
                ad_storage: 'denied',
                ad_user_data: 'denied',
                ad_personalization: 'denied',
              });
              gtag('js', new Date());
              gtag('config', '${gaId}', { send_page_view: false });
            `}</Script>
          </>
        )}
        <Analytics gaId={gaId} />
        {gaId && <CookieConsentBanner locale={lang} />}
        <Providers>
          <div className="w-full">{children}</div>
        </Providers>
      </body>
    </html>
  )
}
