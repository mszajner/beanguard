import { CheckCircle } from 'lucide-react'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'

const ConfirmationPage: React.FC = () => {
  const { t } = useTranslation()

  return (
    <div className="flex flex-1 items-center justify-center p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-10 text-center shadow-sm ring-1 ring-zinc-900/5">
        <CheckCircle className="mx-auto mb-4 text-emerald-500" size={48} />
        <h1 className="mb-2 text-xl font-bold text-zinc-900">
          {t('confirmationPage.heading')}
        </h1>
        <p className="mb-6 text-sm text-zinc-500">
          {t('confirmationPage.body')}
        </p>
        <Link to="/" className="btn-primary">
          {t('confirmationPage.backToShop')}
        </Link>
      </div>
    </div>
  )
}

export default ConfirmationPage
