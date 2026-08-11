import { Loader2, Settings2 } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch } from '../../utils/api'

interface Parameter {
  name: string
  value: string | null
  createdAt: string | null
  updatedAt: string | null
}

const LEGAL_PARAM_NAMES = [
  'LEGAL_CURRENCY',
  'LEGAL_TERMS_URL',
  'LEGAL_PRIVACY_URL',
  'LEGAL_VAT_RATE',
  'LEGAL_COMPANY_NAME',
  'LEGAL_COMPANY_NIP',
  'LEGAL_COMPANY_KRS',
  'LEGAL_COMPANY_ADDRESS',
  'LEGAL_COMPANY_PHONE',
  'LEGAL_PRO_FORMA_PAYMENT_DAYS',
  'LEGAL_BANK_ACCOUNT',
  'LEGAL_CERTIFICATE_ACTIVATION_TEXT',
] as const

const LegalSettingsPage: React.FC = () => {
  const { t } = useTranslation('legal')
  const [params, setParams] = useState<Record<string, Parameter>>({})
  const [formValues, setFormValues] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)

  const fetchParams = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result: Parameter[] = await apiFetch('/api/parameters')
      const byName: Record<string, Parameter> = Object.fromEntries(
        result
          .filter((p) =>
            (LEGAL_PARAM_NAMES as readonly string[]).includes(p.name),
          )
          .map((p): [string, Parameter] => [p.name, p]),
      )
      setParams(byName)
      setFormValues(
        Object.fromEntries(
          LEGAL_PARAM_NAMES.map((name): [string, string] => [
            name,
            byName[name]?.value ?? '',
          ]),
        ),
      )
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('genericError'))
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

  const handleChange = (name: string, value: string) => {
    setFormValues((prev) => ({ ...prev, [name]: value }))
  }

  const changedNames = LEGAL_PARAM_NAMES.filter(
    (name) => formValues[name] !== (params[name]?.value ?? ''),
  )
  const isDirty = changedNames.length > 0

  const handleSave = async () => {
    setSaving(true)
    setError(null)
    setSaved(false)
    try {
      await Promise.all(
        changedNames.map((name) =>
          apiFetch(`/api/parameters/${name}`, {
            method: 'PUT',
            body: JSON.stringify({ value: formValues[name] }),
          }),
        ),
      )
      await fetchParams()
      setSaved(true)
      setTimeout(() => setSaved(false), 2000)
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('genericError'))
      }
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="py-12 text-center">
        <Loader2
          className="inline-block animate-spin text-amber-500"
          size={24}
        />
        <p className="mt-2 text-sm text-zinc-400">{t('loading')}</p>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <Settings2 size={24} style={{ color: 'var(--color-primary)' }} />
            {t('page.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">{t('page.description')}</p>
        </div>
        <HelpLink path="/panel-admina/ustawienia" />
      </div>

      {error && (
        <div className="mb-6 rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
          {error}
        </div>
      )}

      {saved && (
        <div className="mb-6 rounded-xl bg-emerald-50 p-3 text-sm text-emerald-700 ring-1 ring-inset ring-emerald-200">
          {t('saved')}
        </div>
      )}

      <div className="space-y-6">
        <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
          <h2 className="mb-4 text-xs font-semibold uppercase tracking-wider text-zinc-400">
            {t('groups.company')}
          </h2>
          <div className="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
            <div>
              <label className="label-text" htmlFor="legal-company-name">
                {t('fields.companyName')}
              </label>
              <input
                id="legal-company-name"
                type="text"
                value={formValues.LEGAL_COMPANY_NAME ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_COMPANY_NAME', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label className="label-text" htmlFor="legal-company-nip">
                {t('fields.companyNip')}
              </label>
              <input
                id="legal-company-nip"
                type="text"
                value={formValues.LEGAL_COMPANY_NIP ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_COMPANY_NIP', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label className="label-text" htmlFor="legal-company-krs">
                {t('fields.companyKrs')}
              </label>
              <input
                id="legal-company-krs"
                type="text"
                value={formValues.LEGAL_COMPANY_KRS ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_COMPANY_KRS', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label className="label-text" htmlFor="legal-company-phone">
                {t('fields.companyPhone')}
              </label>
              <input
                id="legal-company-phone"
                type="text"
                value={formValues.LEGAL_COMPANY_PHONE ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_COMPANY_PHONE', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label className="label-text" htmlFor="legal-company-address">
                {t('fields.companyAddress')}
              </label>
              <input
                id="legal-company-address"
                type="text"
                value={formValues.LEGAL_COMPANY_ADDRESS ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_COMPANY_ADDRESS', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label className="label-text" htmlFor="legal-bank-account">
                {t('fields.bankAccount')}
              </label>
              <input
                id="legal-bank-account"
                type="text"
                value={formValues.LEGAL_BANK_ACCOUNT ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_BANK_ACCOUNT', e.target.value)
                }
                className="input-field"
              />
            </div>
          </div>
        </div>

        <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
          <h2 className="mb-4 text-xs font-semibold uppercase tracking-wider text-zinc-400">
            {t('groups.sales')}
          </h2>
          <div className="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
            <div>
              <label className="label-text" htmlFor="legal-currency">
                {t('fields.currency')}
              </label>
              <input
                id="legal-currency"
                type="text"
                value={formValues.LEGAL_CURRENCY ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_CURRENCY', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label className="label-text" htmlFor="legal-vat-rate">
                {t('fields.vatRate')}
              </label>
              <input
                id="legal-vat-rate"
                type="number"
                value={formValues.LEGAL_VAT_RATE ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_VAT_RATE', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label
                className="label-text"
                htmlFor="legal-pro-forma-payment-days"
              >
                {t('fields.proFormaPaymentDays')}
              </label>
              <input
                id="legal-pro-forma-payment-days"
                type="number"
                value={formValues.LEGAL_PRO_FORMA_PAYMENT_DAYS ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_PRO_FORMA_PAYMENT_DAYS', e.target.value)
                }
                className="input-field"
              />
            </div>
          </div>
        </div>

        <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
          <h2 className="mb-4 text-xs font-semibold uppercase tracking-wider text-zinc-400">
            {t('groups.documents')}
          </h2>
          <div className="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
            <div>
              <label className="label-text" htmlFor="legal-terms-url">
                {t('fields.termsUrl')}
              </label>
              <input
                id="legal-terms-url"
                type="text"
                value={formValues.LEGAL_TERMS_URL ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_TERMS_URL', e.target.value)
                }
                className="input-field"
              />
            </div>

            <div>
              <label className="label-text" htmlFor="legal-privacy-url">
                {t('fields.privacyUrl')}
              </label>
              <input
                id="legal-privacy-url"
                type="text"
                value={formValues.LEGAL_PRIVACY_URL ?? ''}
                onChange={(e) =>
                  handleChange('LEGAL_PRIVACY_URL', e.target.value)
                }
                className="input-field"
              />
            </div>
          </div>
        </div>

        <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
          <h2 className="mb-4 text-xs font-semibold uppercase tracking-wider text-zinc-400">
            {t('groups.certificate')}
          </h2>
          <div>
            <label
              className="label-text"
              htmlFor="legal-certificate-activation-text"
            >
              {t('fields.certificateActivationText')}
            </label>
            <textarea
              id="legal-certificate-activation-text"
              rows={4}
              value={formValues.LEGAL_CERTIFICATE_ACTIVATION_TEXT ?? ''}
              onChange={(e) =>
                handleChange(
                  'LEGAL_CERTIFICATE_ACTIVATION_TEXT',
                  e.target.value,
                )
              }
              className="input-field resize-y text-sm"
            />
          </div>
        </div>

        <div className="flex justify-end">
          <button
            type="button"
            onClick={handleSave}
            className="btn-primary"
            disabled={!isDirty || saving}
          >
            {saving ? t('saving') : t('save')}
          </button>
        </div>
      </div>
    </div>
  )
}

export default LegalSettingsPage
