import { Trash2, X } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'

export interface Product {
  id: string
  enabled: boolean
  type: 'LIMIT' | 'FEATURE'
  claim: string
  priceOneMonth: number
  priceOneYear: number
  name: string
  description: string | null
  certificateName: string | null
  sortOrder: number
  required: boolean
}

interface EditProductModalProps {
  product: Product | null
  onCancel: () => void
  onSave: () => void
  onDelete: () => void
}

const EditProductModal: React.FC<EditProductModalProps> = ({
  product,
  onCancel,
  onSave,
  onDelete,
}) => {
  const [formData, setFormData] = useState(() => ({
    name: product?.name ?? '',
    description: product?.description || '',
    certificateName: product?.certificateName || '',
    type: product?.type ?? 'LIMIT',
    claim: product?.claim ?? '',
    priceOneMonth: product ? String(product.priceOneMonth) : '',
    priceOneYear: product ? String(product.priceOneYear) : '',
    enabled: product?.enabled ?? true,
    required: product?.required ?? false,
  }))
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { t } = useTranslation('products')

  if (!product) return null

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
      await apiFetch(`/api/products/${product.id}`, {
        method: 'PUT',
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
      onSave()
    } catch (err) {
      setError(err instanceof Error ? err.message : t('editModal.genericError'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onCancel} />
      <div className="modal-panel">
        <div className="modal-header">
          <h3 className="modal-title">{t('editModal.title')}</h3>
          <button
            onClick={onCancel}
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
            id="edit-product-form"
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
        <div className="modal-footer justify-between">
          <button
            type="button"
            onClick={onDelete}
            disabled={loading}
            className="btn-danger flex items-center gap-1.5"
          >
            <Trash2 size={15} /> {t('editModal.delete')}
          </button>
          <div className="flex gap-3">
            <button
              type="button"
              onClick={onCancel}
              className="btn-secondary"
            >
              {t('editModal.cancel')}
            </button>
            <button
              type="submit"
              form="edit-product-form"
              disabled={loading}
              className="btn-primary"
            >
              {loading ? t('editModal.saving') : t('editModal.save')}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default EditProductModal
