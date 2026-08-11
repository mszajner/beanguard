import { X } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'

export interface Parameter {
  name: string
  value: string | null
  createdAt: string | null
  updatedAt: string | null
}

interface EditParameterModalProps {
  parameter: Parameter | null
  onClose: () => void
  onSaved: (updated: Parameter) => void
}

const EditParameterModal: React.FC<EditParameterModalProps> = ({
  parameter,
  onClose,
  onSaved,
}) => {
  const [value, setValue] = useState(() => parameter?.value ?? '')
  const { t } = useTranslation('parameters')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!parameter) return null

  const isWriteOnly = parameter.value === null
  const description = t(`descriptions.${parameter.name}`, {
    defaultValue: '',
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const updated: Parameter = await apiFetch(
        `/api/parameters/${parameter.name}`,
        {
          method: 'PUT',
          body: JSON.stringify({ value }),
        },
      )
      onSaved(updated)
      onClose()
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('editModal.genericError'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-panel">
        <div className="modal-header">
          <h3 className="modal-title">
            {isWriteOnly
              ? t('editModal.titleOverwrite')
              : t('editModal.titleEdit')}
          </h3>
          <button
            onClick={onClose}
            aria-label={t('editModal.close')}
            className="text-zinc-400 transition-colors hover:text-zinc-600"
          >
            <X size={18} />
          </button>
        </div>

        <div className="mx-6 mt-4 rounded-xl bg-zinc-50 p-3 ring-1 ring-inset ring-zinc-200">
          <p className="mb-1 text-xs font-medium uppercase tracking-wide text-zinc-500">
            {t('editModal.nameLabel')}
          </p>
          <p className="font-mono text-sm text-zinc-700">{parameter.name}</p>
          {description && (
            <p className="mt-2 text-xs text-zinc-500">{description}</p>
          )}
        </div>

        {error && (
          <div
            role="alert"
            className="mx-6 mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200"
          >
            {error}
          </div>
        )}

        <div className="modal-body">
          <form id="edit-parameter-form" onSubmit={handleSubmit}>
            <label className="label-text" htmlFor="parameter-value">
              {t('editModal.valueLabel')}
            </label>
            <textarea
              id="parameter-value"
              rows={6}
              autoFocus
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder={
                isWriteOnly ? t('editModal.valuePlaceholder') : undefined
              }
              className="input-field resize-none font-mono text-sm"
            />
          </form>
        </div>

        <div className="modal-footer">
          <button
            type="button"
            onClick={onClose}
            className="btn-secondary"
            disabled={loading}
          >
            {t('editModal.cancel')}
          </button>
          <button
            type="submit"
            form="edit-parameter-form"
            className="btn-primary"
            disabled={loading}
          >
            {loading ? t('editModal.saving') : t('editModal.save')}
          </button>
        </div>
      </div>
    </div>
  )
}

export default EditParameterModal
