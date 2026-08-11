import { X } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'

interface AddUserModalProps {
  visible: boolean
  onCancel: () => void
  onAdd: (values: {
    firstName: string
    lastName: string
    email: string
    password: string
  }) => Promise<void>
}

const AddUserModal: React.FC<AddUserModalProps> = ({
  visible,
  onCancel,
  onAdd,
}) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { t } = useTranslation('users')

  if (!visible) return null

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await onAdd(formData)
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : t('addModal.genericError'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onCancel} />
      <div className="modal-panel">
        <div className="modal-header">
          <h3 className="modal-title">{t('addModal.title')}</h3>
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
            id="add-user-form"
            onSubmit={handleSubmit}
            className="space-y-4"
          >
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
              <div>
                <label className="label-text" htmlFor="firstName">
                  {t('form.firstNameLabel')}
                </label>
                <input
                  required
                  type="text"
                  name="firstName"
                  id="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  className="input-field"
                />
              </div>
              <div>
                <label className="label-text" htmlFor="lastName">
                  {t('form.lastNameLabel')}
                </label>
                <input
                  required
                  type="text"
                  name="lastName"
                  id="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  className="input-field"
                />
              </div>
            </div>

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
              <label className="label-text" htmlFor="password">
                {t('addModal.passwordLabel')}
              </label>
              <input
                required
                minLength={6}
                type="password"
                name="password"
                id="password"
                value={formData.password}
                onChange={handleChange}
                className="input-field"
              />
            </div>
          </form>
        </div>

        <div className="modal-footer">
          <button
            type="button"
            onClick={onCancel}
            className="btn-secondary"
            disabled={loading}
          >
            {t('addModal.cancel')}
          </button>
          <button
            type="submit"
            form="add-user-form"
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

export default AddUserModal
