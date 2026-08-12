import { IBM_Plex_Mono, IBM_Plex_Sans } from 'next/font/google'

// Scoped to the marketing homepage only — the rest of the docs site keeps
// the default system sans stack, so these are exported for local use, not
// wired into the root layout.
export const plexSans = IBM_Plex_Sans({
  subsets: ['latin', 'latin-ext'],
  weight: ['400', '500', '600', '700'],
  variable: '--font-plex-sans',
  display: 'swap',
})

export const plexMono = IBM_Plex_Mono({
  subsets: ['latin', 'latin-ext'],
  weight: ['400', '500', '600'],
  variable: '--font-plex-mono',
  display: 'swap',
})
