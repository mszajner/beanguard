import { NextRequest, NextResponse } from 'next/server'
import {
  defaultLocale,
  isLocale,
  localeCookieName,
  locales,
} from '@/lib/i18n'

function detectLocale(request: NextRequest): string {
  const cookieLocale = request.cookies.get(localeCookieName)?.value
  if (cookieLocale && isLocale(cookieLocale)) {
    return cookieLocale
  }

  const acceptLanguage = request.headers.get('accept-language')
  if (acceptLanguage) {
    // Accept-Language is a comma-separated, already-quality-sorted list,
    // e.g. "pl-PL,pl;q=0.9,en-US;q=0.8" - take the first matching primary tag.
    for (const part of acceptLanguage.split(',')) {
      const tag = part.split(';')[0].trim().split('-')[0].toLowerCase()
      if (isLocale(tag)) {
        return tag
      }
    }
  }

  return defaultLocale
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  const pathnameLocale = locales.find(
    (locale) => pathname === `/${locale}` || pathname.startsWith(`/${locale}/`),
  )
  if (pathnameLocale) {
    const requestHeaders = new Headers(request.headers)
    requestHeaders.set('x-locale', pathnameLocale)
    return NextResponse.next({ request: { headers: requestHeaders } })
  }

  const locale = detectLocale(request)
  const url = request.nextUrl.clone()
  url.pathname = `/${locale}${pathname === '/' ? '' : pathname}`
  return NextResponse.redirect(url)
}

export const config = {
  matcher: [
    // Skip Next internals, static assets, and API/search machinery.
    '/((?!_next|api|.*\\..*).*)',
  ],
}
