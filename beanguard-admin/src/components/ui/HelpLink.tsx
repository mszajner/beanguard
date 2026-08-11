import { HelpCircle } from 'lucide-react'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { DOCS_URL } from '../../utils/api'

// beanguard-docs uses English URL slugs under /en/... while /pl/... keeps
// the original Polish ones (see beanguard-docs/src/components/Navigation.tsx) —
// `path` below is always the Polish slug, translated to English when needed.
const EN_DOCS_PATHS: Record<string, string> = {
  '/panel-admina/branding': '/admin-panel/branding',
  '/panel-admina/klucze-kryptograficzne': '/admin-panel/cryptographic-keys',
  '/panel-admina/licencje': '/admin-panel/licences',
  '/panel-admina/parametry': '/admin-panel/parameters',
  '/panel-admina/produkty': '/admin-panel/products',
  '/panel-admina/szablony-maili': '/admin-panel/mail-templates',
  '/panel-admina/szablony-pdf': '/admin-panel/pdf-templates',
  '/panel-admina/ustawienia': '/admin-panel/settings',
  '/panel-admina/uzytkownicy': '/admin-panel/users',
  '/panel-admina/zamowienia': '/admin-panel/orders',
}

interface HelpLinkProps {
  path: string
}

const HelpLink: React.FC<HelpLinkProps> = ({ path }) => {
  const { t, i18n } = useTranslation()
  const locale = i18n.resolvedLanguage === 'en' ? 'en' : 'pl'
  const localizedPath = locale === 'en' ? (EN_DOCS_PATHS[path] ?? path) : path

  return (
    <a
      href={`${DOCS_URL}/${locale}${localizedPath}`}
      target="_blank"
      rel="noopener noreferrer"
      className="btn-secondary flex shrink-0 items-center gap-2"
    >
      <HelpCircle size={16} /> {t('pageHelp.label')}
    </a>
  )
}

export default HelpLink
