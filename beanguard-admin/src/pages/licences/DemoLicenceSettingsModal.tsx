import { Loader2, X } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'
import ClaimsEditor, { ClaimEntry } from './ClaimsEditor'

interface Parameter {
  name: string
  value: string | null
  createdAt: string | null
  updatedAt: string | null
}

interface DemoLicenceSettingsModalProps {
  onClose: () => void
}

const EXPIRATION_PARAM = 'LICENCE_DEMO_EXPIRATION_DAYS'
const CLAIMS_PARAM = 'LICENCE_DEMO_CLAIMS'
const PARAM_NAMES = [EXPIRATION_PARAM, CLAIMS_PARAM] as const

const parseClaims = (raw: string | null | undefined): ClaimEntry[] => {
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw) as Record<string, string>
    return Object.entries(parsed).map(([key, value]) => ({ key, value }))
  } catch {
    return []
  }
}

const DemoLicenceSettingsModal: React.FC<DemoLicenceSettingsModalProps> = ({
  onClose,
}) => {
  const { t } = useTranslation('licences')
  const [params, setParams] = useState<Record<string, Parameter>>({})
  const [expirationDays, setExpirationDays] = useState('')
  const [claims, setClaims] = useState<ClaimEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchParams = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result: Parameter[] = await apiFetch('/api/parameters')
      const byName: Record<string, Parameter> = Object.fromEntries(
        result
          .filter((p) => (PARAM_NAMES as readonly string[]).includes(p.name))
          .map((p): [string, Parameter] => [p.name, p]),
      )
      setParams(byName)
      setExpirationDays(byName[EXPIRATION_PARAM]?.value ?? '')
      setClaims(parseClaims(byName[CLAIMS_PARAM]?.value))
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('demoModal.genericError'))
      }
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    // Fetch-on-mount: idiomatic effect, not a derived-state anti-pattern.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchParams()
  }, [fetchParams])

  const buildClaimsValue = () => {
    const entries = claims.filter((c) => c.key.trim())
    return JSON.stringify(
      Object.fromEntries(entries.map((c) => [c.key.trim(), c.value])),
    )
  }

  const claimsValue = buildClaimsValue()
  const isDirty =
    expirationDays !== (params[EXPIRATION_PARAM]?.value ?? '') ||
    claimsValue !== (params[CLAIMS_PARAM]?.value ?? '{}')

  const handleSave = async () => {
    setSaving(true)
    setError(null)
    try {
      const updates: Array<[string, string]> = []
      if (expirationDays !== (params[EXPIRATION_PARAM]?.value ?? '')) {
        updates.push([EXPIRATION_PARAM, expirationDays])
      }
      if (claimsValue !== (params[CLAIMS_PARAM]?.value ?? '{}')) {
        updates.push([CLAIMS_PARAM, claimsValue])
      }
      await Promise.all(
        updates.map(([name, value]) =>
          apiFetch(`/api/parameters/${name}`, {
            method: 'PUT',
            body: JSON.stringify({ value }),
          }),
        ),
      )
      onClose()
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('demoModal.genericError'))
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-panel">
        <div className="modal-header">
          <h3 className="modal-title">{t('demoModal.title')}</h3>
          <button
            onClick={onClose}
            aria-label={t('demoModal.close')}
            className="text-zinc-400 transition-colors hover:text-zinc-600"
          >
            <X size={18} />
          </button>
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
          {loading ? (
            <div className="py-8 text-center">
              <Loader2
                className="inline-block animate-spin text-amber-500"
                size={24}
              />
            </div>
          ) : (
            <div className="space-y-4">
              <div>
                <label className="label-text" htmlFor="demo-expiration-days">
                  {t('demoModal.expirationLabel')}
                </label>
                <input
                  id="demo-expiration-days"
                  type="number"
                  min="0"
                  value={expirationDays}
                  onChange={(e) => setExpirationDays(e.target.value)}
                  className="input-field"
                />
              </div>
              <ClaimsEditor value={claims} onChange={setClaims} />
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button
            type="button"
            onClick={onClose}
            className="btn-secondary"
            disabled={saving}
          >
            {t('demoModal.cancel')}
          </button>
          <button
            type="button"
            onClick={handleSave}
            className="btn-primary"
            disabled={saving || loading || !isDirty}
          >
            {saving ? t('demoModal.saving') : t('demoModal.save')}
          </button>
        </div>
      </div>
    </div>
  )
}

export default DemoLicenceSettingsModal
