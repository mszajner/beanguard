export const locales = ['en', 'pl'] as const
export type Locale = (typeof locales)[number]
export const defaultLocale: Locale = 'en'
export const localeCookieName = 'NEXT_LOCALE'

export function isLocale(value: string): value is Locale {
  return (locales as readonly string[]).includes(value)
}

/** First path segment, e.g. '/pl/licencje' -> 'pl'. Returns null if the path has no locale prefix. */
export function localeFromPathname(pathname: string): Locale | null {
  const segment = pathname.split('/')[1]
  return segment && isLocale(segment) ? segment : null
}

export const ui = {
  en: {
    homeAriaLabel: 'BeanGuard home',
    searchPlaceholder: 'Search...',
    searchAriaLabel: 'Search...',
    noResultsFor: (query: string) => `Nothing found for '${query}'. Please try again.`,
    feedbackQuestion: 'Was this page helpful?',
    feedbackYes: 'Yes',
    feedbackNo: 'No',
    feedbackThanks: 'Thanks for the feedback!',
    previous: 'Previous',
    next: 'Next',
    notFoundTitle: 'Page not found',
    notFoundBody: "We couldn't find the page you're looking for.",
    notFoundButton: 'Back to the docs',
    languageName: 'English',
  },
  pl: {
    homeAriaLabel: 'Strona główna BeanGuard',
    searchPlaceholder: 'Szukaj...',
    searchAriaLabel: 'Szukaj...',
    noResultsFor: (query: string) => `Brak wyników dla '${query}'. Spróbuj ponownie.`,
    feedbackQuestion: 'Czy ta strona była pomocna?',
    feedbackYes: 'Tak',
    feedbackNo: 'Nie',
    feedbackThanks: 'Dziękujemy za opinię!',
    previous: 'Poprzednia',
    next: 'Następna',
    notFoundTitle: 'Nie znaleziono strony',
    notFoundBody: 'Nie udało się znaleźć strony, której szukasz.',
    notFoundButton: 'Wróć do dokumentacji',
    languageName: 'Polski',
  },
} as const
