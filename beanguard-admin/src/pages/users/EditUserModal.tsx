import { Trash2, X } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'

interface User {
  id: string
  firstName: string
  lastName: string
  email: string
}

interface EditUserModalProps {
  user: User
  onClose: () => void
  onSaved: (updatedUser: User) => void
  onDelete: () => void
  isLastAdmin: boolean
}

const EditUserModal: React.FC<EditUserModalProps> = ({
  user,
  onClose,
  onSaved,
  onDelete,
  isLastAdmin,
}) => {
  const [formData, setFormData] = useState({
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    newPassword: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { t } = useTranslation('users')

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const updated: User = await apiFetch(`/api/users/${user.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          password: formData.newPassword,
          roles: ['ADMIN'],
        }),
      })
      onSaved(updated)
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : t('editModal.genericError'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-panel">
        <div className="modal-header">
          <h3 className="modal-title">{t('editModal.title')}</h3>
          <button
            onClick={onClose}
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
            id="edit-user-form"
            onSubmit={handleSubmit}
            className="space-y-4"
          >
            <div className="grid grid-cols-2 gap-4">
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
              <label className="label-text" htmlFor="newPassword">
                {t('editModal.newPasswordLabel')}
              </label>
              <input
                type="password"
                name="newPassword"
                id="newPassword"
                value={formData.newPassword}
                onChange={handleChange}
                className="input-field"
                placeholder={t('editModal.newPasswordPlaceholder')}
                autoComplete="new-password"
              />
            </div>
          </form>
        </div>

        <div className="modal-footer justify-between">
          <button
            type="button"
            onClick={onDelete}
            disabled={loading || isLastAdmin}
            title={isLastAdmin ? t('listPage.cannotDeleteLastAdmin') : undefined}
            className="btn-danger flex items-center gap-1.5"
          >
            <Trash2 size={15} /> {t('editModal.delete')}
          </button>
          <div className="flex gap-3">
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
              form="edit-user-form"
              className="btn-primary"
              disabled={loading}
            >
              {loading ? t('editModal.saving') : t('editModal.save')}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default EditUserModal
