import { Loader2, Settings } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch } from '../../utils/api'
import EditParameterModal, { Parameter } from './EditParameterModal'

const ParametersPage: React.FC = () => {
  const [params, setParams] = useState<Parameter[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [paramToEdit, setParamToEdit] = useState<Parameter | null>(null)
  const [copiedName, setCopiedName] = useState<string | null>(null)
  const { t } = useTranslation('parameters')

  const fetchParams = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result: Parameter[] = await apiFetch('/api/parameters')
      setParams(
        result.filter(
          (p) =>
            !/_(?:SUBJECT|BODY|TEMPLATE)/.test(p.name) &&
            !/^LICENCE_(PUBLIC_KEY|PRIVATE_KEY|SECRET_KEY|ISSUER|DEMO_EXPIRATION_DAYS|DEMO_CLAIMS)$/.test(
              p.name,
            ) &&
            !/^BRANDING_(TITLE|LOGO_URL|WEBSITE_URL|FAVICON_URL|SUPPORT_EMAIL|PRIMARY_COLOR)$/.test(
              p.name,
            ) &&
            !/^LEGAL_(CURRENCY|TERMS_URL|PRIVACY_URL|VAT_RATE|COMPANY_NAME|COMPANY_NIP|COMPANY_KRS|COMPANY_ADDRESS|COMPANY_PHONE|PRO_FORMA_PAYMENT_DAYS|BANK_ACCOUNT|CERTIFICATE_ACTIVATION_TEXT)$/.test(
              p.name,
            ) &&
            !/^TOKEN_(ISSUER|PUBLIC_KEY|PRIVATE_KEY|SECRET_KEY|EXPIRATION)$/.test(
              p.name,
            ),
        ),
      )
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('listPage.genericError'))
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

  const handleCopy = async (param: Parameter) => {
    if (!param.value) return
    try {
      if (navigator.clipboard) {
        await navigator.clipboard.writeText(param.value)
      } else {
        const el = document.createElement('textarea')
        el.value = param.value
        document.body.appendChild(el)
        el.select()
        document.execCommand('copy')
        document.body.removeChild(el)
      }
      setCopiedName(param.name)
      setTimeout(() => setCopiedName(null), 1500)
    } catch {
      // ignore copy failure
    }
  }

  const handleSaved = (updated: Parameter) => {
    setParams((prev) =>
      prev.map((p) => (p.name === updated.name ? updated : p)),
    )
    setParamToEdit(null)
  }

  const formatDate = (iso: string | null) => {
    if (!iso) return t('listPage.noUpdateDate')
    return iso.slice(0, 16).replace('T', ' ')
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <Settings size={24} style={{ color: 'var(--color-primary)' }} />
            {t('listPage.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            {t('listPage.description')}
          </p>
        </div>
        <HelpLink path="/panel-admina/parametry" />
      </div>

      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>{t('listPage.nameColumn')}</th>
              <th>{t('listPage.valueColumn')}</th>
              <th>{t('listPage.updatedColumn')}</th>
              <th className="text-right">{t('listPage.actionsColumn')}</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} className="py-12 text-center">
                  <Loader2
                    className="inline-block animate-spin text-amber-500"
                    size={24}
                  />
                  <p className="mt-2 text-sm text-zinc-400">
                    {t('listPage.loading')}
                  </p>
                </td>
              </tr>
            ) : error ? (
              <tr>
                <td colSpan={4} className="py-10 text-center">
                  <p className="mb-3 text-sm text-red-500">{error}</p>
                  <button
                    onClick={fetchParams}
                    className="btn-secondary text-sm"
                  >
                    {t('listPage.retry')}
                  </button>
                </td>
              </tr>
            ) : (
              params.map((param) => {
                const isWriteOnly = param.value === null
                const description = t(`descriptions.${param.name}`, {
                  defaultValue: '',
                })
                return (
                  <tr key={param.name}>
                    <td>
                      <span className="font-mono text-sm text-zinc-700">
                        {param.name}
                      </span>
                      {description && (
                        <p className="mt-0.5 max-w-sm text-xs text-zinc-500">
                          {description}
                        </p>
                      )}
                    </td>
                    <td>
                      {isWriteOnly ? (
                        <span className="text-sm italic text-zinc-400">
                          {t('listPage.writeOnlyValue')}
                        </span>
                      ) : (
                        <span className="block max-w-xs truncate font-mono text-xs text-zinc-600">
                          {param.value}
                        </span>
                      )}
                    </td>
                    <td className="text-sm text-zinc-500">
                      {formatDate(param.updatedAt)}
                    </td>
                    <td className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        {!isWriteOnly && (
                          <button
                            onClick={() => handleCopy(param)}
                            className="rounded-lg border border-zinc-200 px-2.5 py-1 text-xs text-zinc-600 transition-colors hover:bg-zinc-50"
                          >
                            {copiedName === param.name
                              ? t('listPage.copied')
                              : t('listPage.copy')}
                          </button>
                        )}
                        <button
                          onClick={() => setParamToEdit(param)}
                          className="rounded-lg border border-zinc-200 px-2.5 py-1 text-xs text-zinc-600 transition-colors hover:bg-zinc-50"
                        >
                          {isWriteOnly
                            ? t('listPage.overwrite')
                            : t('listPage.edit')}
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      <EditParameterModal
        key={paramToEdit?.name ?? 'none'}
        parameter={paramToEdit}
        onClose={() => setParamToEdit(null)}
        onSaved={handleSaved}
      />
    </div>
  )
}

export default ParametersPage
