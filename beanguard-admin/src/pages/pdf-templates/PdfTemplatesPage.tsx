import { FileText, Loader2, RotateCcw, X } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch } from '../../utils/api'
import { Parameter } from '../parameters/EditParameterModal'

interface PdfTemplate {
  labelKey: string
  key: string
}

const TEMPLATES: PdfTemplate[] = [
  {
    labelKey: 'templates.licenceCertificate',
    key: 'LEGAL_CERTIFICATE_TEMPLATE',
  },
  { labelKey: 'templates.proFormaInvoice', key: 'LEGAL_PRO_FORMA_TEMPLATE' },
]

interface EditModalProps {
  template: PdfTemplate | null
  params: Record<string, Parameter>
  onClose: () => void
  onSaved: (updated: Parameter) => void
}

const EditModal: React.FC<EditModalProps> = ({
  template,
  params,
  onClose,
  onSaved,
}) => {
  const [value, setValue] = useState(() =>
    template ? (params[template.key]?.value ?? '') : '',
  )
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { t } = useTranslation('pdf-templates')

  if (!template) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const updated: Parameter = await apiFetch(
        `/api/parameters/${template.key}`,
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
      <div className="modal-panel" style={{ maxWidth: '780px' }}>
        <div className="modal-header">
          <h3 className="modal-title">{t(template.labelKey)}</h3>
          <button
            onClick={onClose}
            aria-label={t('editModal.close')}
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
          <form id="pdf-template-form" onSubmit={handleSubmit}>
            <label className="label-text" htmlFor="pdf-template-body">
              {t('editModal.bodyLabel')}
            </label>
            <textarea
              id="pdf-template-body"
              rows={20}
              autoFocus
              value={value}
              onChange={(e) => setValue(e.target.value)}
              className="input-field resize-y font-mono text-xs"
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
            form="pdf-template-form"
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

const ALL_TEMPLATE_KEYS = TEMPLATES.map((t) => t.key)

const PdfTemplatesPage: React.FC = () => {
  const [params, setParams] = useState<Record<string, Parameter>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editing, setEditing] = useState<PdfTemplate | null>(null)
  const [resetting, setResetting] = useState(false)
  const { t } = useTranslation('pdf-templates')

  const fetchParams = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const all: Parameter[] = await apiFetch('/api/parameters')
      const map: Record<string, Parameter> = {}
      all.forEach((p) => {
        map[p.name] = p
      })
      setParams(map)
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('listPage.genericLoadError'))
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

  const handleSaved = (updated: Parameter) => {
    setParams((prev) => ({ ...prev, [updated.name]: updated }))
  }

  const handleResetDefaults = async () => {
    if (!window.confirm(t('listPage.resetConfirm'))) return
    setResetting(true)
    try {
      await Promise.all(
        ALL_TEMPLATE_KEYS.map((key) =>
          apiFetch(`/api/parameters/${key}`, { method: 'DELETE' }),
        ),
      )
      await fetchParams()
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('listPage.resetError'))
      }
    } finally {
      setResetting(false)
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <FileText size={24} style={{ color: 'var(--color-primary)' }} />
            {t('listPage.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            {t('listPage.description')}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <HelpLink path="/panel-admina/szablony-pdf" />
          <button
            onClick={handleResetDefaults}
            disabled={resetting || loading}
            className="btn-secondary flex items-center gap-2 text-sm"
          >
            {resetting ? (
              <Loader2 size={14} className="animate-spin" />
            ) : (
              <RotateCcw size={14} />
            )}
            {t('listPage.resetButton')}
          </button>
        </div>
      </div>

      {loading ? (
        <div className="py-12 text-center">
          <Loader2
            className="inline-block animate-spin text-amber-500"
            size={24}
          />
          <p className="mt-2 text-sm text-zinc-400">{t('listPage.loading')}</p>
        </div>
      ) : error ? (
        <div className="py-10 text-center">
          <p className="mb-3 text-sm text-red-500">{error}</p>
          <button onClick={fetchParams} className="btn-secondary text-sm">
            {t('listPage.retry')}
          </button>
        </div>
      ) : (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>{t('listPage.templateColumn')}</th>
              </tr>
            </thead>
            <tbody>
              {TEMPLATES.map((tpl) => (
                <tr key={tpl.key}>
                  <td>
                    <button
                      onClick={() => setEditing(tpl)}
                      className="text-left font-medium text-amber-700 underline underline-offset-2 hover:text-amber-900"
                    >
                      {t(tpl.labelKey)}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <EditModal
        key={editing?.key ?? 'none'}
        template={editing}
        params={params}
        onClose={() => setEditing(null)}
        onSaved={handleSaved}
      />
    </div>
  )
}

export default PdfTemplatesPage
