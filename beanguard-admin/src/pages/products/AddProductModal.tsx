import { X } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'

interface AddProductModalProps {
  visible: boolean
  onCancel: () => void
  onAdd: () => void
}

const EMPTY_FORM = {
  name: '',
  description: '',
  certificateName: '',
  type: 'LIMIT',
  claim: '',
  priceOneMonth: '',
  priceOneYear: '',
  enabled: true,
  required: false,
}

const AddProductModal: React.FC<AddProductModalProps> = ({
  visible,
  onCancel,
  onAdd,
}) => {
  const [formData, setFormData] = useState(EMPTY_FORM)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { t } = useTranslation('products')

  if (!visible) return null

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value, type } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]:
        type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await apiFetch('/api/products', {
        method: 'POST',
        body: JSON.stringify({
          name: formData.name,
          description: formData.description || null,
          certificateName: formData.certificateName || null,
          type: formData.type,
          claim: formData.claim,
          priceOneMonth: parseFloat(formData.priceOneMonth),
          priceOneYear: parseFloat(formData.priceOneYear),
          enabled: formData.enabled,
          required: formData.required,
        }),
      })
      setFormData(EMPTY_FORM)
      onAdd()
    } catch (err) {
      setError(err instanceof Error ? err.message : t('addModal.genericError'))
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = () => {
    setFormData(EMPTY_FORM)
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
            className="text-zinc-400 transition-colors hover:text-zinc-600"
          >
            <X size={18} />
          </button>
        </div>
        {error && (
          <div className="mx-6 mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
            {error}
          </div>
        )}
        <div className="modal-body">
          <form
            id="add-product-form"
            onSubmit={handleSubmit}
            className="space-y-4"
          >
            <div>
              <label className="mb-1 block text-sm font-medium text-zinc-700">
                {t('form.nameLabel')}
              </label>
              <input
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
                className="input-field"
                placeholder={t('form.namePlaceholder')}
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-zinc-700">
                {t('form.descriptionLabel')}
              </label>
              <input
                name="description"
                value={formData.description}
                onChange={handleChange}
                className="input-field"
                placeholder={t('form.descriptionPlaceholder')}
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-zinc-700">
                {t('form.certificateNameLabel')}
              </label>
              <input
                name="certificateName"
                value={formData.certificateName}
                onChange={handleChange}
                className="input-field"
                placeholder={t('form.certificateNamePlaceholder')}
              />
            </div>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
              <div>
                <label className="mb-1 block text-sm font-medium text-zinc-700">
                  {t('form.typeLabel')}
                </label>
                <select
                  name="type"
                  value={formData.type}
                  onChange={handleChange}
                  className="input-field"
                >
                  <option value="LIMIT">LIMIT</option>
                  <option value="FEATURE">FEATURE</option>
                </select>
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-zinc-700">
                  {t('form.claimLabel')}
                </label>
                <input
                  name="claim"
                  value={formData.claim}
                  onChange={handleChange}
                  required
                  className="input-field"
                  placeholder={t('form.claimPlaceholder')}
                />
              </div>
            </div>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
              <div>
                <label className="mb-1 block text-sm font-medium text-zinc-700">
                  {t('form.priceMonthlyLabel')}
                </label>
                <input
                  name="priceOneMonth"
                  value={formData.priceOneMonth}
                  onChange={handleChange}
                  required
                  type="number"
                  min="0.01"
                  step="0.01"
                  className="input-field"
                  placeholder={t('form.pricePlaceholder')}
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-zinc-700">
                  {t('form.priceYearlyLabel')}
                </label>
                <input
                  name="priceOneYear"
                  value={formData.priceOneYear}
                  onChange={handleChange}
                  required
                  type="number"
                  min="0.01"
                  step="0.01"
                  className="input-field"
                  placeholder={t('form.pricePlaceholder')}
                />
              </div>
            </div>
            <div className="space-y-2.5">
              <label className="flex cursor-pointer items-center gap-2 text-sm font-medium text-zinc-700">
                <input
                  type="checkbox"
                  name="enabled"
                  checked={formData.enabled}
                  onChange={handleChange}
                  className="rounded border-zinc-300"
                />
                {t('form.enabledLabel')}
              </label>
              <label className="flex cursor-pointer items-center gap-2 text-sm font-medium text-zinc-700">
                <input
                  type="checkbox"
                  name="required"
                  checked={formData.required}
                  onChange={handleChange}
                  className="rounded border-zinc-300"
                />
                {t('form.requiredLabel')}
              </label>
            </div>
          </form>
        </div>
        <div className="modal-footer">
          <button
            type="button"
            onClick={handleCancel}
            className="btn-secondary"
          >
            {t('addModal.cancel')}
          </button>
          <button
            type="submit"
            form="add-product-form"
            disabled={loading}
            className="btn-primary"
          >
            {loading ? t('addModal.saving') : t('addModal.add')}
          </button>
        </div>
      </div>
    </div>
  )
}

export default AddProductModal
