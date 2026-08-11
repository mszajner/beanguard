import React from 'react'
import { useTranslation } from 'react-i18next'

const LANGUAGES: Array<{ code: 'en' | 'pl'; label: string }> = [
  { code: 'en', label: 'EN' },
  { code: 'pl', label: 'PL' },
]

const LanguageSwitcher: React.FC = () => {
  const { i18n } = useTranslation()

  return (
    <div className="flex items-center gap-1 rounded-full p-0.5 ring-1 ring-zinc-900/10">
      {LANGUAGES.map(({ code, label }) => (
        <button
          key={code}
          type="button"
          onClick={() => i18n.changeLanguage(code)}
          className={`rounded-full px-2 py-1 text-xs font-semibold transition-colors ${
            i18n.resolvedLanguage === code
              ? 'bg-zinc-900 text-white'
              : 'text-zinc-500 hover:text-zinc-900'
          }`}
        >
          {label}
        </button>
      ))}
    </div>
  )
}

export default LanguageSwitcher
