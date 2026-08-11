import { X } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'
import ClaimsEditor, { ClaimEntry } from './ClaimsEditor'

interface AddLicenceModalProps {
  visible: boolean
  onCancel: () => void
  onAdd: () => void
}

const EMPTY_FORM = {
  email: '',
  vatId: '',
  expiration: '',
  companyName: '',
  phoneNumber: '',
  street: '',
  postCode: '',
  city: '',
  type: 'STANDARD',
}

const AddLicenceModal: React.FC<AddLicenceModalProps> = ({
  visible,
  onCancel,
  onAdd,
}) => {
  const { t } = useTranslation('licences')
  const [formData, setFormData] = useState(EMPTY_FORM)
  const [claims, setClaims] = useState<ClaimEntry[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!visible) return null

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
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
    setLoading(true)
    setError(null)
    try {
      await apiFetch('/api/licences', {
        method: 'POST',
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
          type: formData.type,
        }),
      })
      setFormData(EMPTY_FORM)
      setClaims([])
      onAdd()
    } catch (err) {
      setError(err instanceof Error ? err.message : t('addModal.genericError'))
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = () => {
    setFormData(EMPTY_FORM)
    setClaims([])
    setError(null)
    onCancel()
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={handleCancel} />
      <div className="modal-panel">
        <div className="modal-header">
          <h3 className="modal-title">{t('addModal.title')}</h3>
          <button
            onClick={handleCancel}
            aria-label={t('addModal.close')}
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
          <form
            id="add-licence-form"
            onSubmit={handleSubmit}
            className="space-y-4"
          >
            <div>
              <label className="label-text" htmlFor="add-email">
                {t('form.emailLabel')}
              </label>
              <input
                required
                type="email"
                name="email"
                id="add-email"
                value={formData.email}
                onChange={handleChange}
                className="input-field"
              />
            </div>
            <div>
              <label className="label-text" htmlFor="add-vatId">
                {t('form.vatIdLabel')}
              </label>
              <input
                required
                type="text"
                name="vatId"
                id="add-vatId"
                value={formData.vatId}
                onChange={handleChange}
                className="input-field"
              />
            </div>
            <div>
              <label className="label-text" htmlFor="add-type">
                {t('addModal.typeLabel')}
              </label>
              <select
                name="type"
                id="add-type"
                value={formData.type}
                onChange={handleChange}
                className="input-field"
              >
                <option value="STANDARD">STANDARD</option>
                <option value="DEMO">DEMO</option>
              </select>
            </div>
            <div>
              <label className="label-text" htmlFor="add-expiration">
                {t('form.expirationLabel')}
              </label>
              <input
                type="date"
                name="expiration"
                id="add-expiration"
                value={formData.expiration}
                onChange={handleChange}
                className="input-field"
              />
            </div>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
              <div>
                <label className="label-text" htmlFor="add-companyName">
                  {t('form.companyNameLabel')}
                </label>
                <input
                  type="text"
                  name="companyName"
                  id="add-companyName"
                  value={formData.companyName}
                  onChange={handleChange}
                  className="input-field"
                />
              </div>
              <div>
                <label className="label-text" htmlFor="add-phoneNumber">
                  {t('form.phoneLabel')}
                </label>
                <input
                  type="text"
                  name="phoneNumber"
                  id="add-phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  className="input-field"
                />
              </div>
            </div>
            <div>
              <label className="label-text" htmlFor="add-street">
                {t('form.streetLabel')}
              </label>
              <input
                type="text"
                name="street"
                id="add-street"
                value={formData.street}
                onChange={handleChange}
                className="input-field"
              />
            </div>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
              <div>
                <label className="label-text" htmlFor="add-postCode">
                  {t('form.postCodeLabel')}
                </label>
                <input
                  type="text"
                  name="postCode"
                  id="add-postCode"
                  value={formData.postCode}
                  onChange={handleChange}
                  className="input-field"
                />
              </div>
              <div>
                <label className="label-text" htmlFor="add-city">
                  {t('form.cityLabel')}
                </label>
                <input
                  type="text"
                  name="city"
                  id="add-city"
                  value={formData.city}
                  onChange={handleChange}
                  className="input-field"
                />
              </div>
            </div>
            <ClaimsEditor value={claims} onChange={setClaims} />
          </form>
        </div>

        <div className="modal-footer">
          <button
            type="button"
            onClick={handleCancel}
            className="btn-secondary"
            disabled={loading}
          >
            {t('addModal.cancel')}
          </button>
          <button
            type="submit"
            form="add-licence-form"
            className="btn-primary"
            disabled={loading}
          >
            {loading ? t('addModal.adding') : t('addModal.add')}
          </button>
        </div>
      </div>
    </div>
  )
}

export default AddLicenceModal
