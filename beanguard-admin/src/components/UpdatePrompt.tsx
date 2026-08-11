import { AlertTriangle } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useRegisterSW } from 'virtual:pwa-register/react'

export default function UpdatePrompt() {
  const { t } = useTranslation()
  const {
    needRefresh: [needRefresh],
    updateServiceWorker,
  } = useRegisterSW({
    onRegisterError: (error) =>
      console.error('Service worker registration failed', error),
  })

  if (!needRefresh) {
    return null
  }

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed inset-x-0 bottom-4 z-50 mx-auto flex w-[calc(100%-2rem)] max-w-sm items-center gap-3 rounded-2xl bg-amber-50 px-4 py-3 shadow-xl ring-1 ring-inset ring-amber-200"
    >
      <AlertTriangle className="shrink-0 text-amber-600" size={20} />
      <span className="flex-1 text-sm font-medium text-amber-900">
        {t('updatePrompt.message')}
      </span>
      <button
        type="button"
        onClick={() => updateServiceWorker(true)}
        className="shrink-0 rounded-full bg-amber-500 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-amber-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-600 focus-visible:ring-offset-2"
      >
        {t('updatePrompt.refreshButton')}
      </button>
    </div>
  )
}
