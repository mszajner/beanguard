import { Loader2 } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import ConfirmActionModal from '../../components/ui/ConfirmActionModal'
import { apiFetch } from '../../utils/api'
import RegenerateKeysModal from './RegenerateKeysModal'

interface Parameter {
  name: string
  value: string | null
  createdAt: string | null
  updatedAt: string | null
}

interface KeyPairSectionProps {
  namespace: string
  issuerParam: string
  publicKeyParam: string
  privateKeyParam: string
  secretKeyParam: string
  expirationParam?: string
  regenerateEndpoint: string
}

const KeyPairSection: React.FC<KeyPairSectionProps> = ({
  namespace,
  issuerParam,
  publicKeyParam,
  privateKeyParam,
  secretKeyParam,
  expirationParam,
  regenerateEndpoint,
}) => {
  const { t } = useTranslation(namespace)
  const [params, setParams] = useState<Record<string, Parameter>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [issuerValue, setIssuerValue] = useState('')
  const [showIssuerConfirm, setShowIssuerConfirm] = useState(false)
  const [issuerSaving, setIssuerSaving] = useState(false)
  const [issuerError, setIssuerError] = useState<string | null>(null)
  const [expirationValue, setExpirationValue] = useState('')
  const [expirationSaving, setExpirationSaving] = useState(false)
  const [expirationError, setExpirationError] = useState<string | null>(null)
  const [showRegenerateModal, setShowRegenerateModal] = useState(false)
  const [copiedPublicKey, setCopiedPublicKey] = useState(false)
  const [copiedSecretKey, setCopiedSecretKey] = useState(false)

  const keyParamNames = [
    issuerParam,
    publicKeyParam,
    privateKeyParam,
    secretKeyParam,
    ...(expirationParam ? [expirationParam] : []),
  ]

  const fetchParams = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result: Parameter[] = await apiFetch('/api/parameters')
      const byName: Record<string, Parameter> = Object.fromEntries(
        result
          .filter((p) => keyParamNames.includes(p.name))
          .map((p): [string, Parameter] => [p.name, p]),
      )
      setParams(byName)
      setIssuerValue(byName[issuerParam]?.value ?? '')
      if (expirationParam) {
        setExpirationValue(byName[expirationParam]?.value ?? '')
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('keys.genericError'))
      }
    } finally {
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [t])

  useEffect(() => {
    // Fetch-on-mount: idiomatic effect, not a derived-state anti-pattern.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchParams()
  }, [fetchParams])

  const handleSaveIssuer = async () => {
    setIssuerSaving(true)
    setIssuerError(null)
    try {
      await apiFetch(`/api/parameters/${issuerParam}`, {
        method: 'PUT',
        body: JSON.stringify({ value: issuerValue }),
      })
      setShowIssuerConfirm(false)
      await fetchParams()
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setIssuerError(message || t('issuer.genericError'))
      }
    } finally {
      setIssuerSaving(false)
    }
  }

  const handleSaveExpiration = async () => {
    if (!expirationParam) return
    setExpirationSaving(true)
    setExpirationError(null)
    try {
      await apiFetch(`/api/parameters/${expirationParam}`, {
        method: 'PUT',
        body: JSON.stringify({ value: expirationValue }),
      })
      await fetchParams()
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setExpirationError(message || t('issuer.expirationGenericError'))
      }
    } finally {
      setExpirationSaving(false)
    }
  }

  const handleCopyPublicKey = async () => {
    const value = params[publicKeyParam]?.value
    if (!value) return
    try {
      await navigator.clipboard.writeText(value)
      setCopiedPublicKey(true)
      setTimeout(() => setCopiedPublicKey(false), 1500)
    } catch {
      // ignore copy failure
    }
  }

  const handleCopySecretKey = async () => {
    const value = params[secretKeyParam]?.value
    if (!value) return
    try {
      await navigator.clipboard.writeText(value)
      setCopiedSecretKey(true)
      setTimeout(() => setCopiedSecretKey(false), 1500)
    } catch {
      // ignore copy failure
    }
  }

  const formatUpdatedAt = (iso: string | null | undefined) => {
    if (!iso) return null
    return t('keys.updatedAt', { date: iso.slice(0, 16).replace('T', ' ') })
  }

  const renderStatusBadge = (param: Parameter | undefined) => {
    const configured = !!param?.createdAt
    return (
      <span
        className={
          configured
            ? 'inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-600/20'
            : 'inline-flex items-center rounded-full bg-zinc-100 px-2.5 py-0.5 text-xs font-medium text-zinc-600 ring-1 ring-inset ring-zinc-400/20'
        }
      >
        {configured ? t('keys.configured') : t('keys.notConfigured')}
      </span>
    )
  }

  if (loading) {
    return (
      <div className="py-12 text-center">
        <Loader2
          className="inline-block animate-spin text-amber-500"
          size={24}
        />
        <p className="mt-2 text-sm text-zinc-400">{t('keys.loading')}</p>
      </div>
    )
  }

  return (
    <div>
      {error && (
        <div className="mb-6 rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
          {error}
        </div>
      )}

      <div className="mb-8 rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
        <h2 className="text-base font-semibold text-zinc-900">
          {t('issuer.heading')}
        </h2>
        <div className="mt-3 flex items-end gap-3">
          <div className="flex-1">
            <label className="label-text" htmlFor="key-pair-issuer">
              {t('issuer.label')}
            </label>
            <input
              id="key-pair-issuer"
              type="text"
              value={issuerValue}
              onChange={(e) => setIssuerValue(e.target.value)}
              className="input-field"
            />
          </div>
          <button
            type="button"
            onClick={() => setShowIssuerConfirm(true)}
            className="btn-primary"
            disabled={issuerValue === (params[issuerParam]?.value ?? '')}
          >
            {t('issuer.save')}
          </button>
        </div>

        {expirationParam && (
          <>
            {expirationError && (
              <div className="mt-3 rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
                {expirationError}
              </div>
            )}
            <div className="mt-4 flex items-end gap-3">
              <div className="flex-1">
                <label className="label-text" htmlFor="key-pair-expiration">
                  {t('issuer.expirationLabel')}
                </label>
                <input
                  id="key-pair-expiration"
                  type="number"
                  min="0"
                  value={expirationValue}
                  onChange={(e) => setExpirationValue(e.target.value)}
                  className="input-field"
                />
              </div>
              <button
                type="button"
                onClick={handleSaveExpiration}
                className="btn-primary"
                disabled={
                  expirationSaving ||
                  expirationValue === (params[expirationParam]?.value ?? '')
                }
              >
                {expirationSaving ? t('issuer.saving') : t('issuer.save')}
              </button>
            </div>
          </>
        )}
      </div>

      <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-zinc-900">
            {t('keys.heading')}
          </h2>
          <button
            type="button"
            onClick={() => setShowRegenerateModal(true)}
            className="btn-primary"
          >
            {t('keys.regenerate')}
          </button>
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between rounded-xl border border-zinc-200 p-3">
            <div>
              <p className="text-sm font-medium text-zinc-700">
                {t('keys.publicKey')}
              </p>
              <p className="mt-0.5 max-w-md text-xs text-zinc-500">
                {t('keys.publicKeyDescription')}
              </p>
              {formatUpdatedAt(params[publicKeyParam]?.updatedAt) && (
                <p className="mt-1 text-xs text-zinc-400">
                  {formatUpdatedAt(params[publicKeyParam]?.updatedAt)}
                </p>
              )}
            </div>
            <div className="flex items-center gap-2">
              {renderStatusBadge(params[publicKeyParam])}
              {params[publicKeyParam]?.value && (
                <button
                  onClick={handleCopyPublicKey}
                  className="rounded-lg border border-zinc-200 px-2.5 py-1 text-xs text-zinc-600 transition-colors hover:bg-zinc-50"
                >
                  {copiedPublicKey ? t('keys.copied') : t('keys.copy')}
                </button>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between rounded-xl border border-zinc-200 p-3">
            <div>
              <p className="text-sm font-medium text-zinc-700">
                {t('keys.privateKey')}
              </p>
              <p className="mt-0.5 max-w-md text-xs text-zinc-500">
                {t('keys.privateKeyDescription')}
              </p>
              {formatUpdatedAt(params[privateKeyParam]?.updatedAt) && (
                <p className="mt-1 text-xs text-zinc-400">
                  {formatUpdatedAt(params[privateKeyParam]?.updatedAt)}
                </p>
              )}
            </div>
            {renderStatusBadge(params[privateKeyParam])}
          </div>

          <div className="flex items-center justify-between rounded-xl border border-zinc-200 p-3">
            <div>
              <p className="text-sm font-medium text-zinc-700">
                {t('keys.secretKey')}
              </p>
              <p className="mt-0.5 max-w-md text-xs text-zinc-500">
                {t('keys.secretKeyDescription')}
              </p>
              {formatUpdatedAt(params[secretKeyParam]?.updatedAt) && (
                <p className="mt-1 text-xs text-zinc-400">
                  {formatUpdatedAt(params[secretKeyParam]?.updatedAt)}
                </p>
              )}
            </div>
            <div className="flex items-center gap-2">
              {renderStatusBadge(params[secretKeyParam])}
              {params[secretKeyParam]?.value && (
                <button
                  onClick={handleCopySecretKey}
                  className="rounded-lg border border-zinc-200 px-2.5 py-1 text-xs text-zinc-600 transition-colors hover:bg-zinc-50"
                >
                  {copiedSecretKey ? t('keys.copied') : t('keys.copy')}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {showIssuerConfirm && (
        <ConfirmActionModal
          title={t('issuer.confirmTitle')}
          message={t('issuer.confirmMessage')}
          confirmLabel={t('issuer.confirmLabel')}
          confirmingLabel={t('issuer.saving')}
          loading={issuerSaving}
          error={issuerError}
          onConfirm={handleSaveIssuer}
          onCancel={() => {
            setShowIssuerConfirm(false)
            setIssuerError(null)
          }}
        />
      )}

      {showRegenerateModal && (
        <RegenerateKeysModal
          namespace={namespace}
          endpoint={regenerateEndpoint}
          onClose={() => {
            setShowRegenerateModal(false)
            fetchParams()
          }}
        />
      )}
    </div>
  )
}

export default KeyPairSection
