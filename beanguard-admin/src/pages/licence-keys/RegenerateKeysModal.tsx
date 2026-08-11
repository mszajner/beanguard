import { X } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'

interface RegenerateResult {
  publicKey: string
  secretKey: string
  rotatedAt: string
}

interface RegenerateKeysModalProps {
  namespace: string
  endpoint: string
  onClose: () => void
}

const RegenerateKeysModal: React.FC<RegenerateKeysModalProps> = ({
  namespace,
  endpoint,
  onClose,
}) => {
  const { t } = useTranslation(namespace)
  const [step, setStep] = useState<'confirm' | 'reveal'>('confirm')
  const [confirmText, setConfirmText] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<RegenerateResult | null>(null)
  const [copiedField, setCopiedField] = useState<'public' | 'secret' | null>(
    null,
  )

  const confirmPhrase = t('regenerateModal.confirmPhrase')

  const handleRegenerate = async () => {
    setLoading(true)
    setError(null)
    try {
      const data: RegenerateResult = await apiFetch(endpoint, {
        method: 'POST',
      })
      setResult(data)
      setStep('reveal')
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('regenerateModal.genericError'))
      }
    } finally {
      setLoading(false)
    }
  }

  const handleCopy = async (field: 'public' | 'secret', value: string) => {
    try {
      await navigator.clipboard.writeText(value)
      setCopiedField(field)
      setTimeout(() => setCopiedField(null), 1500)
    } catch {
      // ignore copy failure
    }
  }

  if (step === 'reveal' && result) {
    return (
      <div className="modal-overlay">
        <div className="modal-backdrop" onClick={onClose} />
        <div className="modal-panel max-w-lg">
          <div className="modal-header">
            <h3 className="modal-title">{t('regenerateModal.revealTitle')}</h3>
            <button
              onClick={onClose}
              className="text-zinc-400 transition-colors hover:text-zinc-600"
            >
              <X size={18} />
            </button>
          </div>
          <div className="modal-body space-y-4">
            <div className="rounded-xl bg-amber-50 p-3 text-sm text-amber-800 ring-1 ring-inset ring-amber-200">
              {t('regenerateModal.revealWarning')}
            </div>
            <div>
              <p className="label-text">
                {t('regenerateModal.publicKeyLabel')}
              </p>
              <div className="flex items-start gap-2">
                <code className="block max-h-24 flex-1 overflow-y-auto break-all rounded-lg bg-zinc-50 p-2 font-mono text-xs text-zinc-700 ring-1 ring-inset ring-zinc-200">
                  {result.publicKey}
                </code>
                <button
                  onClick={() => handleCopy('public', result.publicKey)}
                  className="shrink-0 rounded-lg border border-zinc-200 px-2.5 py-1 text-xs text-zinc-600 transition-colors hover:bg-zinc-50"
                >
                  {copiedField === 'public'
                    ? t('regenerateModal.copied')
                    : t('regenerateModal.copy')}
                </button>
              </div>
            </div>
            <div>
              <p className="label-text">
                {t('regenerateModal.secretKeyLabel')}
              </p>
              <div className="flex items-start gap-2">
                <code className="block max-h-24 flex-1 overflow-y-auto break-all rounded-lg bg-zinc-50 p-2 font-mono text-xs text-zinc-700 ring-1 ring-inset ring-zinc-200">
                  {result.secretKey}
                </code>
                <button
                  onClick={() => handleCopy('secret', result.secretKey)}
                  className="shrink-0 rounded-lg border border-zinc-200 px-2.5 py-1 text-xs text-zinc-600 transition-colors hover:bg-zinc-50"
                >
                  {copiedField === 'secret'
                    ? t('regenerateModal.copied')
                    : t('regenerateModal.copy')}
                </button>
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn-primary">
              {t('regenerateModal.close')}
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-panel max-w-md">
        <div className="modal-header">
          <h3 className="modal-title">{t('regenerateModal.confirmTitle')}</h3>
          <button
            onClick={onClose}
            className="text-zinc-400 transition-colors hover:text-zinc-600"
            disabled={loading}
          >
            <X size={18} />
          </button>
        </div>
        <div className="modal-body space-y-4">
          <div className="rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
            {t('regenerateModal.confirmWarning')}
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <div>
            <label className="label-text" htmlFor="regenerate-confirm-input">
              {t('regenerateModal.confirmInputLabel')}
            </label>
            <input
              id="regenerate-confirm-input"
              type="text"
              autoFocus
              value={confirmText}
              onChange={(e) => setConfirmText(e.target.value)}
              className="input-field font-mono text-sm"
            />
          </div>
        </div>
        <div className="modal-footer">
          <button
            type="button"
            onClick={onClose}
            className="btn-secondary"
            disabled={loading}
          >
            {t('regenerateModal.cancel')}
          </button>
          <button
            type="button"
            onClick={handleRegenerate}
            className="btn-danger"
            disabled={loading || confirmText !== confirmPhrase}
          >
            {loading ? t('regenerateModal.generating') : t('regenerateModal.confirm')}
          </button>
        </div>
      </div>
    </div>
  )
}

export default RegenerateKeysModal
