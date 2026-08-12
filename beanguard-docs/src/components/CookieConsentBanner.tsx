'use client'

import { useEffect } from 'react'
import * as CookieConsent from 'vanilla-cookieconsent'
import 'vanilla-cookieconsent/dist/cookieconsent.css'

import { updateConsent } from '@/lib/gtag'
import { type Locale } from '@/lib/i18n'

const CAT_ANALYTICS = 'analytics'

const translations = {
  en: {
    consentModal: {
      title: 'We use cookies',
      description:
        'This site uses essential cookies to make it work, and optional analytics cookies to understand how visitors use the documentation. Analytics cookies are only set if you agree.',
      acceptAllBtn: 'Accept all',
      acceptNecessaryBtn: 'Reject non-essential',
      showPreferencesBtn: 'Manage preferences',
    },
    preferencesModal: {
      title: 'Cookie preferences',
      acceptAllBtn: 'Accept all',
      acceptNecessaryBtn: 'Reject non-essential',
      savePreferencesBtn: 'Save preferences',
      closeIconLabel: 'Close',
      sections: [
        {
          title: 'Strictly necessary',
          description:
            'Required for the site to function properly. Always active.',
          linkedCategory: 'necessary',
        },
        {
          title: 'Analytics',
          description:
            'Helps us understand how visitors use the documentation (Google Analytics). Only set with your consent.',
          linkedCategory: CAT_ANALYTICS,
        },
      ],
    },
  },
  pl: {
    consentModal: {
      title: 'Używamy plików cookie',
      description:
        'Ta strona używa niezbędnych plików cookie do działania oraz opcjonalnych plików cookie analitycznych, żeby zrozumieć, jak odwiedzający korzystają z dokumentacji. Cookie analityczne są ustawiane tylko za Twoją zgodą.',
      acceptAllBtn: 'Akceptuj wszystkie',
      acceptNecessaryBtn: 'Odrzuć opcjonalne',
      showPreferencesBtn: 'Zarządzaj preferencjami',
    },
    preferencesModal: {
      title: 'Preferencje cookie',
      acceptAllBtn: 'Akceptuj wszystkie',
      acceptNecessaryBtn: 'Odrzuć opcjonalne',
      savePreferencesBtn: 'Zapisz preferencje',
      closeIconLabel: 'Zamknij',
      sections: [
        {
          title: 'Niezbędne',
          description:
            'Wymagane do prawidłowego działania strony. Zawsze aktywne.',
          linkedCategory: 'necessary',
        },
        {
          title: 'Analityka',
          description:
            'Pomaga nam zrozumieć, jak odwiedzający korzystają z dokumentacji (Google Analytics). Ustawiane tylko za Twoją zgodą.',
          linkedCategory: CAT_ANALYTICS,
        },
      ],
    },
  },
}

function syncConsent() {
  updateConsent(CookieConsent.acceptedCategory(CAT_ANALYTICS))
}

export function CookieConsentBanner({ locale }: { locale: Locale }) {
  useEffect(() => {
    CookieConsent.run({
      guiOptions: {
        consentModal: {
          layout: 'box',
          position: 'bottom right',
        },
        preferencesModal: {
          layout: 'box',
        },
      },
      categories: {
        necessary: {
          enabled: true,
          readOnly: true,
        },
        [CAT_ANALYTICS]: {
          autoClear: {
            cookies: [{ name: /^_ga/ }],
          },
        },
      },
      language: {
        default: locale,
        translations,
      },
      onFirstConsent: syncConsent,
      onConsent: syncConsent,
      onChange: syncConsent,
    })
  }, [locale])

  return null
}
