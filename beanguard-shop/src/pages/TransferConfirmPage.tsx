import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router-dom'
import { shopFetch } from '../api'

export default function TransferConfirmPage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [state, setState] = useState<'loading' | 'success' | 'error'>(() =>
    token ? 'loading' : 'error',
  )

  useEffect(() => {
    if (!token) return
    shopFetch(`/api/open/licences/transfer/${token}/confirm`, {
      method: 'POST',
    })
      .then(() => setState('success'))
      .catch(() => setState('error'))
  }, [token])

  if (state === 'loading') {
    return (
      <div className="flex flex-1 items-center justify-center bg-gray-50">
        <p className="text-lg text-gray-600">
          {t('transferConfirmPage.loading')}
        </p>
      </div>
    )
  }

  if (state === 'success') {
    return (
      <div className="flex flex-1 items-center justify-center bg-gray-50">
        <div className="max-w-md rounded-2xl bg-white p-10 text-center shadow">
          <h1 className="mb-3 text-2xl font-bold text-green-700">
            {t('transferConfirmPage.successHeading')}
          </h1>
          <p className="text-gray-600">
            {t('transferConfirmPage.successBody')}
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 items-center justify-center bg-gray-50">
      <div className="max-w-md rounded-2xl bg-white p-10 text-center shadow">
        <h1 className="mb-3 text-2xl font-bold text-red-700">
          {t('transferConfirmPage.errorHeading')}
        </h1>
        <p className="text-gray-600">{t('transferConfirmPage.errorBody')}</p>
      </div>
    </div>
  )
}
