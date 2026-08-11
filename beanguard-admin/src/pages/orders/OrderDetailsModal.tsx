import { CheckCircle, X, XCircle } from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { apiFetch } from '../../utils/api'

interface OrderItem {
  id: string
  productId: string
  name: string
  price: number
  quantity: number
}

export interface Order {
  id: string
  status: 'NEW' | 'ACCEPTED' | 'CANCELED'
  licenceId: string | null
  period: 'ONE_YEAR' | 'ONE_MONTH'
  vatId: string
  email: string
  companyName: string | null
  street: string | null
  postCode: string | null
  city: string | null
  phoneNumber: string | null
  items: OrderItem[]
  creditAmount: number | null
  createdAt: string
  number: string | null
  proFormaNumber: string | null
}

interface OrderDetailsModalProps {
  order: Order | null
  onClose: () => void
  onStatusChange: () => void
}

const PERIOD_LABEL_KEYS: Record<string, string> = {
  ONE_MONTH: 'detailsModal.periodOneMonth',
  ONE_YEAR: 'detailsModal.periodOneYear',
}

const OrderDetailsModal: React.FC<OrderDetailsModalProps> = ({
  order,
  onClose,
  onStatusChange,
}) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { t } = useTranslation('orders')

  if (!order) return null

  const itemsTotal = order.items.reduce(
    (sum, item) => sum + Number(item.price) * item.quantity,
    0,
  )
  const credit = Number(order.creditAmount ?? 0)
  const total = Math.max(0, itemsTotal - credit)

  const handleAction = async (action: 'accept' | 'cancel') => {
    setLoading(true)
    setError(null)
    try {
      await apiFetch(`/api/orders/${order.id}/${action}`, { method: 'POST' })
      onStatusChange()
      onClose()
    } catch (err) {
      setError(
        err instanceof Error ? err.message : t('detailsModal.genericError'),
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-backdrop" onClick={onClose} />
      <div className="modal-panel" style={{ maxWidth: '560px' }}>
        <div className="modal-header">
          <h3 className="modal-title">
            {t('detailsModal.orderNumberPrefix')}
            {order.number ?? order.id.substring(0, 8).toUpperCase()}
          </h3>
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

        <div className="modal-body space-y-4">
          <div className="grid grid-cols-1 gap-x-4 gap-y-2 text-sm sm:grid-cols-2">
            <div>
              <span className="text-zinc-500">
                {t('detailsModal.emailLabel')}
              </span>{' '}
              <span className="font-medium">{order.email}</span>
            </div>
            <div>
              <span className="text-zinc-500">
                {t('detailsModal.vatIdLabel')}
              </span>{' '}
              <span className="font-medium">{order.vatId}</span>
            </div>
            {order.companyName && (
              <div>
                <span className="text-zinc-500">
                  {t('detailsModal.companyLabel')}
                </span>{' '}
                <span className="font-medium">{order.companyName}</span>
              </div>
            )}
            {order.phoneNumber && (
              <div>
                <span className="text-zinc-500">
                  {t('detailsModal.phoneLabel')}
                </span>{' '}
                <span className="font-medium">{order.phoneNumber}</span>
              </div>
            )}
            {order.street && (
              <div className="col-span-2">
                <span className="text-zinc-500">
                  {t('detailsModal.addressLabel')}
                </span>{' '}
                <span className="font-medium">
                  {order.street}, {order.postCode} {order.city}
                </span>
              </div>
            )}
            <div>
              <span className="text-zinc-500">
                {t('detailsModal.periodLabel')}
              </span>{' '}
              <span className="font-medium">
                {(PERIOD_LABEL_KEYS[order.period] &&
                  t(PERIOD_LABEL_KEYS[order.period])) ||
                  order.period}
              </span>
            </div>
            {order.licenceId && (
              <div className="col-span-2">
                <span className="text-zinc-500">
                  {t('detailsModal.licenceKeyLabel')}
                </span>{' '}
                <code className="rounded bg-zinc-100 px-1.5 py-0.5 text-xs">
                  {order.licenceId}
                </code>
              </div>
            )}
          </div>

          <div>
            <p className="mb-2 text-sm font-semibold text-zinc-700">
              {t('detailsModal.itemsHeading')}
            </p>
            <div className="overflow-x-auto rounded-xl ring-1 ring-zinc-200">
              <table className="w-full min-w-[400px] text-sm">
                <thead className="bg-zinc-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-medium text-zinc-500">
                      {t('detailsModal.productColumn')}
                    </th>
                    <th className="px-3 py-2 text-right font-medium text-zinc-500">
                      {t('detailsModal.priceColumn')}
                    </th>
                    <th className="px-3 py-2 text-right font-medium text-zinc-500">
                      {t('detailsModal.quantityColumn')}
                    </th>
                    <th className="px-3 py-2 text-right font-medium text-zinc-500">
                      {t('detailsModal.totalColumn')}
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100">
                  {order.items.map((item) => (
                    <tr key={item.id}>
                      <td className="px-3 py-2 text-zinc-800">{item.name}</td>
                      <td className="px-3 py-2 text-right text-zinc-600">
                        {Number(item.price).toFixed(2)}{' '}
                        {t('detailsModal.currency')}
                      </td>
                      <td className="px-3 py-2 text-right text-zinc-600">
                        {item.quantity}
                      </td>
                      <td className="px-3 py-2 text-right font-medium">
                        {(Number(item.price) * item.quantity).toFixed(2)}{' '}
                        {t('detailsModal.currency')}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-zinc-50">
                  {credit > 0 && (
                    <tr>
                      <td
                        colSpan={3}
                        className="px-3 py-2 text-right text-sm text-zinc-500"
                      >
                        {t('detailsModal.creditLabel')}
                      </td>
                      <td className="px-3 py-2 text-right text-sm text-green-600">
                        -{credit.toFixed(2)} {t('detailsModal.currency')}
                      </td>
                    </tr>
                  )}
                  <tr>
                    <td
                      colSpan={3}
                      className="px-3 py-2 text-right font-semibold text-zinc-700"
                    >
                      {t('detailsModal.netTotalLabel')}
                    </td>
                    <td className="px-3 py-2 text-right font-bold text-zinc-900">
                      {total.toFixed(2)} {t('detailsModal.currency')}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button onClick={onClose} className="btn-secondary">
            {t('detailsModal.close')}
          </button>
          {order.status === 'NEW' && (
            <>
              <button
                onClick={() => handleAction('cancel')}
                disabled={loading}
                className="flex items-center gap-1.5 rounded-xl bg-red-50 px-4 py-2 text-sm font-medium text-red-700 ring-1 ring-inset ring-red-200 transition-colors hover:bg-red-100 disabled:opacity-50"
              >
                <XCircle size={15} /> {t('detailsModal.cancel')}
              </button>
              <button
                onClick={() => handleAction('accept')}
                disabled={loading}
                className="btn-primary flex items-center gap-1.5"
              >
                <CheckCircle size={15} />{' '}
                {loading
                  ? t('detailsModal.processing')
                  : t('detailsModal.accept')}
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default OrderDetailsModal
