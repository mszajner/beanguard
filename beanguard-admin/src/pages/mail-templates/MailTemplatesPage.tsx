import { Loader2, Mail, RotateCcw, X } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch } from '../../utils/api'
import { Parameter } from '../parameters/EditParameterModal'

interface MailTemplate {
  labelKey: string
  subjectKey: string
  bodyKey: string
}

const TEMPLATES: MailTemplate[] = [
  {
    labelKey: 'templates.demoWelcome',
    subjectKey: 'MAIL_DEMO_WELCOME_SUBJECT',
    bodyKey: 'MAIL_DEMO_WELCOME_BODY',
  },
  {
    labelKey: 'templates.expiryWarning',
    subjectKey: 'MAIL_EXPIRY_WARNING_SUBJECT',
    bodyKey: 'MAIL_EXPIRY_WARNING_BODY',
  },
  {
    labelKey: 'templates.expiryWarningDemo',
    subjectKey: 'MAIL_EXPIRY_WARNING_SUBJECT_DEMO',
    bodyKey: 'MAIL_EXPIRY_WARNING_BODY_DEMO',
  },
  {
    labelKey: 'templates.transferConfirm',
    subjectKey: 'MAIL_TRANSFER_CONFIRM_SUBJECT',
    bodyKey: 'MAIL_TRANSFER_CONFIRM_BODY',
  },
  {
    labelKey: 'templates.licenceCreated',
    subjectKey: 'MAIL_LICENCE_CREATED_SUBJECT',
    bodyKey: 'MAIL_LICENCE_CREATED_BODY',
  },
  {
    labelKey: 'templates.orderConfirm',
    subjectKey: 'MAIL_ORDER_CONFIRM_SUBJECT',
    bodyKey: 'MAIL_ORDER_CONFIRM_BODY',
  },
  {
    labelKey: 'templates.orderCancel',
    subjectKey: 'MAIL_ORDER_CANCEL_SUBJECT',
    bodyKey: 'MAIL_ORDER_CANCEL_BODY',
  },
]

interface EditModalProps {
  template: MailTemplate | null
  params: Record<string, Parameter>
  onClose: () => void
  onSaved: (updates: Parameter[]) => void
}

const EditModal: React.FC<EditModalProps> = ({
  template,
  params,
  onClose,
  onSaved,
}) => {
  const [subject, setSubject] = useState(() =>
    template ? (params[template.subjectKey]?.value ?? '') : '',
  )
  const [body, setBody] = useState(() =>
    template ? (params[template.bodyKey]?.value ?? '') : '',
  )
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { t } = useTranslation('mail-templates')

  if (!template) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const [updatedSubject, updatedBody] = await Promise.all([
        apiFetch(`/api/parameters/${template.subjectKey}`, {
          method: 'PUT',
          body: JSON.stringify({ value: subject }),
        }),
        apiFetch(`/api/parameters/${template.bodyKey}`, {
          method: 'PUT',
          body: JSON.stringify({ value: body }),
        }),
      ])
      onSaved([updatedSubject, updatedBody])
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
      <div className="modal-panel" style={{ maxWidth: '680px' }}>
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

        <div className="modal-body space-y-4">
          <form id="mail-template-form" onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="label-text" htmlFor="mail-subject">
                {t('editModal.subjectLabel')}
              </label>
              <input
                id="mail-subject"
                type="text"
                autoFocus
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                className="input-field"
              />
            </div>
            <div>
              <label className="label-text" htmlFor="mail-body">
                {t('editModal.bodyLabel')}
              </label>
              <textarea
                id="mail-body"
                rows={14}
                value={body}
                onChange={(e) => setBody(e.target.value)}
                className="input-field resize-y font-mono text-xs"
              />
            </div>
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
            form="mail-template-form"
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

const ALL_TEMPLATE_KEYS = TEMPLATES.flatMap((t) => [t.subjectKey, t.bodyKey])

const MailTemplatesPage: React.FC = () => {
  const [params, setParams] = useState<Record<string, Parameter>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editing, setEditing] = useState<MailTemplate | null>(null)
  const [resetting, setResetting] = useState(false)
  const { t } = useTranslation('mail-templates')

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

  const handleSaved = (updates: Parameter[]) => {
    setParams((prev) => {
      const next = { ...prev }
      updates.forEach((p) => {
        next[p.name] = p
      })
      return next
    })
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
            <Mail size={24} style={{ color: 'var(--color-primary)' }} />
            {t('listPage.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            {t('listPage.description')}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <HelpLink path="/panel-admina/szablony-maili" />
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
                <th>{t('listPage.subjectColumn')}</th>
              </tr>
            </thead>
            <tbody>
              {TEMPLATES.map((tpl) => (
                <tr key={tpl.subjectKey}>
                  <td>
                    <button
                      onClick={() => setEditing(tpl)}
                      className="text-left font-medium text-amber-700 underline underline-offset-2 hover:text-amber-900"
                    >
                      {t(tpl.labelKey)}
                    </button>
                  </td>
                  <td className="max-w-xs truncate text-sm text-zinc-500">
                    {params[tpl.subjectKey]?.value ?? (
                      <span className="italic text-zinc-400">
                        {t('listPage.noValue')}
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <EditModal
        key={editing?.subjectKey ?? 'none'}
        template={editing}
        params={params}
        onClose={() => setEditing(null)}
        onSaved={handleSaved}
      />
    </div>
  )
}

export default MailTemplatesPage
