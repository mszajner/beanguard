import React from 'react'
import { useConfig } from '../useConfig'
import LanguageSwitcher from './LanguageSwitcher'

const AppHeader: React.FC = () => {
  const { title, logoUrl } = useConfig()

  return (
    <header className="border-b border-zinc-900/5 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-4 sm:px-6">
        <img
          src={logoUrl ?? '/logo-full.png'}
          alt={title}
          className="h-7 rounded-lg object-contain"
        />
        <LanguageSwitcher />
      </div>
    </header>
  )
}

export default AppHeader
