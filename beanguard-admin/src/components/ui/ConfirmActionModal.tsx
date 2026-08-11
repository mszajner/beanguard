import { AlertTriangle } from 'lucide-react'
import React from 'react'
import { useTranslation } from 'react-i18next'

interface ConfirmActionModalProps {
  title: string
  message: string
  confirmLabel: string
  confirmingLabel: string
  loading: boolean
  error: string | null
  onConfirm: () => void
  onCancel: () => void
}

const ConfirmActionModal: React.FC<ConfirmActionModalProps> = ({
  title,
  message,
  confirmLabel,
  confirmingLabel,
  loading,
  error,
  onConfirm,
  onCancel,
}) => {
  const { t } = useTranslation()

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onCancel} />
      <div className="modal-panel max-w-md">
        <div className="px-6 pb-5 pt-6">
          <div className="flex items-start gap-4">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-50 ring-1 ring-amber-100">
              <AlertTriangle className="h-5 w-5 text-amber-600" />
            </div>
            <div className="min-w-0">
              <h3 className="text-base font-semibold text-zinc-900">{title}</h3>
              <p className="mt-1 text-sm text-zinc-500">{message}</p>
              {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button
            type="button"
            onClick={onCancel}
            className="btn-secondary"
            disabled={loading}
          >
            {t('confirmActionModal.cancel')}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="btn-primary"
            disabled={loading}
          >
            {loading ? confirmingLabel : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}

export default ConfirmActionModal
