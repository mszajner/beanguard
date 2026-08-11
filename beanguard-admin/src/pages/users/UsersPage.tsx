import { Loader2, Plus, Users } from 'lucide-react'
import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import ConfirmDeleteModal from '../../components/ui/ConfirmDeleteModal'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch } from '../../utils/api'
import AddUserModal from './AddUserModal'
import EditUserModal from './EditUserModal'

interface User {
  id: string
  firstName: string
  lastName: string
  email: string
}

const UsersPage: React.FC = () => {
  const [data, setData] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [userToEdit, setUserToEdit] = useState<User | null>(null)
  const [userToDelete, setUserToDelete] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const { t } = useTranslation('users')

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true)
        setError(null)
        const result = await apiFetch('/api/users')
        setData(Array.isArray(result) ? result : [])
      } catch (err) {
        setError(
          err instanceof Error ? err.message : t('listPage.genericError'),
        )
      } finally {
        setLoading(false)
      }
    }
    fetchUsers()
  }, [t])

  const handleDelete = async (id: string) => {
    setDeleteLoading(true)
    setDeleteError(null)
    try {
      await apiFetch(`/api/users/${id}`, { method: 'DELETE' })
      setData(data.filter((item) => item.id !== id))
      setUserToDelete(null)
    } catch (err) {
      setDeleteError(
        err instanceof Error ? err.message : t('listPage.deleteError'),
      )
    } finally {
      setDeleteLoading(false)
    }
  }

  const handleAdd = async (values: {
    firstName: string
    lastName: string
    email: string
    password: string
  }) => {
    const newUser: User = await apiFetch('/api/users', {
      method: 'POST',
      body: JSON.stringify({
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        password: values.password,
        roles: ['ADMIN'],
      }),
    })
    setData([...data, newUser])
    setIsModalVisible(false)
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <Users size={24} style={{ color: 'var(--color-primary)' }} />
            {t('listPage.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            {t('listPage.description')}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <HelpLink path="/panel-admina/uzytkownicy" />
          <button
            onClick={() => setIsModalVisible(true)}
            className="btn-primary flex items-center gap-2"
          >
            <Plus size={16} /> {t('listPage.addButton')}
          </button>
        </div>
      </div>

      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>{t('listPage.userColumn')}</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td className="py-12 text-center">
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
                <td className="py-10 text-center text-sm text-red-500">
                  {error}
                </td>
              </tr>
            ) : data.length === 0 ? (
              <tr>
                <td className="py-10 text-center text-sm text-zinc-400">
                  {t('listPage.noData')}
                </td>
              </tr>
            ) : (
              data.map((record) => (
                <tr key={record.id}>
                  <td
                    onClick={() => setUserToEdit(record)}
                    className="cursor-pointer hover:bg-zinc-50"
                  >
                    <div className="font-medium text-zinc-900">
                      {record.firstName} {record.lastName}
                    </div>
                    <div className="mt-0.5 text-xs text-zinc-400">
                      {record.email}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {userToDelete && (
        <ConfirmDeleteModal
          title={t('listPage.deleteModalTitle')}
          message={t('listPage.deleteModalMessage')}
          loading={deleteLoading}
          error={deleteError}
          onConfirm={() => handleDelete(userToDelete)}
          onCancel={() => {
            setUserToDelete(null)
            setDeleteError(null)
          }}
        />
      )}

      <AddUserModal
        visible={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        onAdd={handleAdd}
      />

      {userToEdit && (
        <EditUserModal
          user={userToEdit}
          onClose={() => setUserToEdit(null)}
          onSaved={(updated) => {
            setData(
              data.map((u) => (u.id === updated.id ? { ...u, ...updated } : u)),
            )
            setUserToEdit(null)
          }}
          onDelete={() => {
            setUserToDelete(userToEdit.id)
            setUserToEdit(null)
          }}
          isLastAdmin={data.length <= 1}
        />
      )}
    </div>
  )
}

export default UsersPage
