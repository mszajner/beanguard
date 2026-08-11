import { ArrowLeft, Loader2 } from 'lucide-react'
import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../utils/api'
import ClaimsEditor, { ClaimEntry } from './ClaimsEditor'

export interface Licence {
  key: string
  email: string
  vatId: string
  companyName: string | null
  expiration: string | null
  street: string | null
  postCode: string | null
  city: string | null
  phoneNumber: string | null
  claims: Record<string, string> | null
  netAmount: number | null
  createdAt: string
  updatedAt: string
  type: 'DEMO' | 'STANDARD' | null
}

const EditLicencePage: React.FC = () => {
  const { key } = useParams<{ key: string }>()
  const navigate = useNavigate()
  const { t } = useTranslation('licences')

  const [licence, setLicence] = useState<Licence | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    email: '',
    vatId: '',
    expiration: '',
    companyName: '',
    phoneNumber: '',
    street: '',
    postCode: '',
    city: '',
  })
  const [claims, setClaims] = useState<ClaimEntry[]>([])
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  useEffect(() => {
    apiFetch(`/api/licences/${key}`)
      .then((data: Licence) => {
        setLicence(data)
        setFormData({
          email: data.email,
          vatId: data.vatId,
          expiration: data.expiration ? data.expiration.slice(0, 10) : '',
          companyName: data.companyName ?? '',
          phoneNumber: data.phoneNumber ?? '',
          street: data.street ?? '',
          postCode: data.postCode ?? '',
          city: data.city ?? '',
        })
        setClaims(
          data.claims
            ? Object.entries(data.claims).map(([k, v]) => ({
                key: k,
                value: v,
              }))
            : [],
        )
      })
      .catch((err) => {
        const message = err instanceof Error ? err.message : undefined
        if (message !== 'session_expired') {
          setLoadError(message || t('editPage.genericLoadError'))
        }
      })
  }, [key, t])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const buildClaimsRecord = (): Record<string, string> | null => {
    const entries = claims.filter((c) => c.key.trim())
    if (entries.length === 0) return null
    return Object.fromEntries(entries.map((c) => [c.key.trim(), c.value]))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setSaveError(null)
    try {
      await apiFetch(`/api/licences/${key}`, {
        method: 'PUT',
        body: JSON.stringify({
          email: formData.email,
          vatId: formData.vatId,
          expiration: formData.expiration
            ? `${formData.expiration}T00:00:00Z`
            : null,
          companyName: formData.companyName || null,
          phoneNumber: formData.phoneNumber || null,
          street: formData.street || null,
          postCode: formData.postCode || null,
          city: formData.city || null,
          claims: buildClaimsRecord(),
        }),
      })
      navigate('/licences')
    } catch (err) {
      setSaveError(
        err instanceof Error ? err.message : t('editPage.genericSaveError'),
      )
    } finally {
      setSaving(false)
    }
  }

  if (loadError) {
    return (
      <div className="mx-auto max-w-2xl">
        <div className="rounded-xl bg-red-50 p-6 text-sm text-red-700 ring-1 ring-inset ring-red-200">
          {loadError}
        </div>
      </div>
    )
  }

  if (!licence) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="animate-spin text-amber-500" size={28} />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl">
      <div className="mb-6 flex items-center gap-3">
        <button
          onClick={() => navigate('/licences')}
          className="text-zinc-400 transition-colors hover:text-zinc-600"
          aria-label={t('editPage.back')}
        >
          <ArrowLeft size={20} />
        </button>
        <h1 className="text-2xl font-bold text-zinc-900">
          {t('editPage.heading')}
        </h1>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div className="rounded-xl bg-zinc-50 p-3 ring-1 ring-inset ring-zinc-200 sm:col-span-2">
          <p className="mb-1 text-xs font-medium uppercase tracking-wide text-zinc-500">
            {t('editPage.keyLabel')}
          </p>
          <p className="select-all break-all font-mono text-sm text-zinc-700">
            {licence.key}
          </p>
        </div>
        {licence.netAmount != null && (
          <div className="rounded-xl bg-zinc-50 p-3 ring-1 ring-inset ring-zinc-200">
            <p className="mb-1 text-xs font-medium uppercase tracking-wide text-zinc-500">
              {t('editPage.netAmountLabel')}
            </p>
            <p className="text-sm font-semibold tabular-nums text-zinc-700">
              {Number(licence.netAmount).toFixed(2)} PLN
            </p>
          </div>
        )}
      </div>

      {saveError && (
        <div
          role="alert"
          className="mb-4 rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200"
        >
          {saveError}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="label-text" htmlFor="email">
              {t('form.emailLabel')}
            </label>
            <input
              required
              type="email"
              name="email"
              id="email"
              value={formData.email}
              onChange={handleChange}
              className="input-field"
            />
          </div>
          <div>
            <label className="label-text" htmlFor="vatId">
              {t('form.vatIdLabel')}
            </label>
            <input
              required
              type="text"
              name="vatId"
              id="vatId"
              value={formData.vatId}
              onChange={handleChange}
              className="input-field"
            />
          </div>
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="label-text" htmlFor="companyName">
              {t('form.companyNameLabel')}
            </label>
            <input
              type="text"
              name="companyName"
              id="companyName"
              value={formData.companyName}
              onChange={handleChange}
              className="input-field"
            />
          </div>
          <div>
            <label className="label-text" htmlFor="phoneNumber">
              {t('form.phoneLabel')}
            </label>
            <input
              type="text"
              name="phoneNumber"
              id="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              className="input-field"
            />
          </div>
        </div>
        <div>
          <label className="label-text" htmlFor="street">
            {t('form.streetLabel')}
          </label>
          <input
            type="text"
            name="street"
            id="street"
            value={formData.street}
            onChange={handleChange}
            className="input-field"
          />
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="label-text" htmlFor="postCode">
              {t('form.postCodeLabel')}
            </label>
            <input
              type="text"
              name="postCode"
              id="postCode"
              value={formData.postCode}
              onChange={handleChange}
              className="input-field"
            />
          </div>
          <div>
            <label className="label-text" htmlFor="city">
              {t('form.cityLabel')}
            </label>
            <input
              type="text"
              name="city"
              id="city"
              value={formData.city}
              onChange={handleChange}
              className="input-field"
            />
          </div>
        </div>
        <div>
          <label className="label-text" htmlFor="expiration">
            {t('form.expirationLabel')}
          </label>
          <input
            type="date"
            name="expiration"
            id="expiration"
            value={formData.expiration}
            onChange={handleChange}
            className="input-field sm:max-w-xs"
          />
        </div>
        <ClaimsEditor value={claims} onChange={setClaims} />

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/licences')}
            className="btn-secondary"
            disabled={saving}
          >
            {t('editPage.cancel')}
          </button>
          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? t('editPage.saving') : t('editPage.save')}
          </button>
        </div>
      </form>
    </div>
  )
}

export default EditLicencePage
