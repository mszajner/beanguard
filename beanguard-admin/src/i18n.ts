import i18n from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import { initReactI18next } from 'react-i18next'
import common_en from './locales/en/common.json'
import licences_en from './locales/en/licences.json'
import mailTemplates_en from './locales/en/mail-templates.json'
import orders_en from './locales/en/orders.json'
import parameters_en from './locales/en/parameters.json'
import pdfTemplates_en from './locales/en/pdf-templates.json'
import licenceKeys_en from './locales/en/licence-keys.json'
import branding_en from './locales/en/branding.json'
import legal_en from './locales/en/legal.json'
import token_en from './locales/en/token-keys.json'
import products_en from './locales/en/products.json'
import users_en from './locales/en/users.json'
import common_pl from './locales/pl/common.json'
import licences_pl from './locales/pl/licences.json'
import mailTemplates_pl from './locales/pl/mail-templates.json'
import orders_pl from './locales/pl/orders.json'
import parameters_pl from './locales/pl/parameters.json'
import pdfTemplates_pl from './locales/pl/pdf-templates.json'
import licenceKeys_pl from './locales/pl/licence-keys.json'
import branding_pl from './locales/pl/branding.json'
import legal_pl from './locales/pl/legal.json'
import token_pl from './locales/pl/token-keys.json'
import products_pl from './locales/pl/products.json'
import users_pl from './locales/pl/users.json'

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: {
        common: common_en,
        licences: licences_en,
        orders: orders_en,
        parameters: parameters_en,
        products: products_en,
        users: users_en,
        'mail-templates': mailTemplates_en,
        'pdf-templates': pdfTemplates_en,
        'licence-keys': licenceKeys_en,
        branding: branding_en,
        legal: legal_en,
        'token-keys': token_en,
      },
      pl: {
        common: common_pl,
        licences: licences_pl,
        orders: orders_pl,
        parameters: parameters_pl,
        products: products_pl,
        users: users_pl,
        'mail-templates': mailTemplates_pl,
        'pdf-templates': pdfTemplates_pl,
        'licence-keys': licenceKeys_pl,
        branding: branding_pl,
        legal: legal_pl,
        'token-keys': token_pl,
      },
    },
    fallbackLng: 'en',
    supportedLngs: ['en', 'pl'],
    defaultNS: 'common',
    interpolation: { escapeValue: false },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
      lookupLocalStorage: 'beanguard-admin-lang',
    },
  })
  .then(() => {
    document.documentElement.lang = i18n.resolvedLanguage ?? 'en'
  })

i18n.on('languageChanged', (lng) => {
  document.documentElement.lang = lng
})

export default i18n
