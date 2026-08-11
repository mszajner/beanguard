import { Loader2, ShoppingCart } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch, apiFetchBlob } from '../../utils/api'
import OrderDetailsModal, { Order } from './OrderDetailsModal'

interface OrderPage {
  content: Order[]
  totalElements: number
  totalPages: number
  number: number
  first: boolean
  last: boolean
}

type StatusFilter = 'ALL' | 'NEW' | 'ACCEPTED' | 'CANCELED'

const StatusBadge: React.FC<{ status: string }> = ({ status }) => {
  const { t } = useTranslation('orders')
  const styles: Record<string, string> = {
    NEW: 'bg-amber-50 text-amber-700 ring-amber-600/20',
    ACCEPTED: 'bg-amber-50 text-amber-700 ring-amber-600/20',
    CANCELED: 'bg-zinc-100 text-zinc-500 ring-zinc-400/20',
  }
  const labelKeys: Record<string, string> = {
    NEW: 'listPage.statusNew',
    ACCEPTED: 'listPage.statusAccepted',
    CANCELED: 'listPage.statusCanceled',
  }
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${styles[status] || ''}`}
    >
      {(labelKeys[status] && t(labelKeys[status])) || status}
    </span>
  )
}

const OrdersPage: React.FC = () => {
  const [data, setData] = useState<OrderPage | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [inputValue, setInputValue] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)
  const { t } = useTranslation('orders')

  useEffect(() => {
    const timer = setTimeout(() => {
      if (inputValue.length === 0 || inputValue.length >= 2) {
        setSearchQuery(inputValue)
        setPage(0)
      }
    }, 400)
    return () => clearTimeout(timer)
  }, [inputValue])

  const fetchOrders = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: '20',
        sort: 'createdAt,desc',
        rk: '' + refreshKey,
      })
      if (statusFilter !== 'ALL') params.set('status', statusFilter)
      if (searchQuery) params.set('query', searchQuery)
      const result = await apiFetch(`/api/orders?${params}`)
      setData(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : t('listPage.genericError'))
    } finally {
      setLoading(false)
    }
  }, [page, statusFilter, searchQuery, refreshKey, t])

  useEffect(() => {
    // Fetch-on-mount: idiomatic effect, not a derived-state anti-pattern.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchOrders()
  }, [fetchOrders])

  const downloadProForma = async (orderId: string, proFormaNumber: string) => {
    try {
      const blob = await apiFetchBlob(`/api/orders/${orderId}/pro-forma`)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `faktura-proforma-${proFormaNumber.replace(/\//g, '-')}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        alert(
          t('listPage.downloadInvoiceError') +
            (message ?? t('listPage.unknownError')),
        )
      }
    }
  }

  const filters: { labelKey: string; value: StatusFilter }[] = [
    { labelKey: 'listPage.filterAll', value: 'ALL' },
    { labelKey: 'listPage.statusNew', value: 'NEW' },
    { labelKey: 'listPage.statusAccepted', value: 'ACCEPTED' },
    { labelKey: 'listPage.statusCanceled', value: 'CANCELED' },
  ]

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <ShoppingCart size={24} style={{ color: 'var(--color-primary)' }} />
            {t('listPage.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            {t('listPage.description')}
          </p>
        </div>
        <HelpLink path="/panel-admina/zamowienia" />
      </div>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <input
          type="text"
          placeholder={t('listPage.searchPlaceholder')}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          className="w-64 rounded-lg bg-white px-3 py-1.5 text-sm text-zinc-700 ring-1 ring-zinc-200 focus:outline-none focus:ring-2 focus:ring-amber-400"
        />
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value as StatusFilter)
            setPage(0)
          }}
          className="rounded-lg bg-white px-3 py-1.5 text-sm font-medium text-zinc-700 ring-1 ring-zinc-200 focus:outline-none focus:ring-2 focus:ring-amber-400"
        >
          {filters.map((f) => (
            <option key={f.value} value={f.value}>
              {t(f.labelKey)}
            </option>
          ))}
        </select>
      </div>

      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>{t('listPage.numberColumn')}</th>
              <th>{t('listPage.customerColumn')}</th>
              <th>{t('listPage.contactColumn')}</th>
              <th>{t('listPage.statusColumn')}</th>
              <th>{t('listPage.dateColumn')}</th>
              <th>{t('listPage.invoiceColumn')}</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} className="py-12 text-center">
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
                <td colSpan={6} className="py-12 text-center text-red-600">
                  {error}
                </td>
              </tr>
            ) : !data || data.content.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-12 text-center text-zinc-400">
                  {t('listPage.noOrders')}
                </td>
              </tr>
            ) : (
              data.content.map((order) => (
                <tr key={order.id}>
                  <td
                    className="cursor-pointer font-mono text-sm hover:bg-zinc-50"
                    onClick={() => setSelectedOrder(order)}
                  >
                    {order.number ?? order.id.substring(0, 8).toUpperCase()}
                  </td>
                  <td>
                    <span className="font-medium text-zinc-900">
                      {order.companyName}
                    </span>
                    {(order.street || order.postCode || order.city) && (
                      <span className="block text-xs text-zinc-500">
                        {[
                          order.street,
                          [order.postCode, order.city]
                            .filter(Boolean)
                            .join(' '),
                        ]
                          .filter(Boolean)
                          .join(', ')}
                      </span>
                    )}
                    {order.vatId && (
                      <span className="block text-xs text-zinc-500">
                        {t('listPage.vatIdPrefix')}
                        {order.vatId}
                      </span>
                    )}
                  </td>
                  <td>
                    <a
                      href={`mailto:${order.email}`}
                      className="font-medium text-zinc-900 hover:text-amber-700"
                    >
                      {order.email}
                    </a>
                    {order.phoneNumber && (
                      <a
                        href={`tel:${order.phoneNumber}`}
                        className="block text-xs text-zinc-500 hover:text-amber-700"
                      >
                        {order.phoneNumber}
                      </a>
                    )}
                  </td>
                  <td>
                    <StatusBadge status={order.status} />
                  </td>
                  <td className="text-sm text-zinc-500">
                    {new Date(order.createdAt).toLocaleDateString('pl-PL')}
                  </td>
                  <td>
                    {order.proFormaNumber ? (
                      <button
                        onClick={() =>
                          downloadProForma(order.id, order.proFormaNumber!)
                        }
                        className="text-sm font-medium text-amber-700 underline underline-offset-2 hover:text-amber-900"
                      >
                        {order.proFormaNumber}
                      </button>
                    ) : (
                      <span className="text-zinc-400">—</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="mt-4 flex justify-center gap-2">
          <button
            disabled={data.first}
            onClick={() => setPage((p) => p - 1)}
            className="rounded-lg px-3 py-1.5 text-sm font-medium ring-1 ring-zinc-200 transition-colors hover:bg-zinc-50 disabled:opacity-40"
          >
            {t('listPage.previous')}
          </button>
          <span className="px-3 py-1.5 text-sm text-zinc-500">
            {data.number + 1} / {data.totalPages}
          </span>
          <button
            disabled={data.last}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-lg px-3 py-1.5 text-sm font-medium ring-1 ring-zinc-200 transition-colors hover:bg-zinc-50 disabled:opacity-40"
          >
            {t('listPage.next')}
          </button>
        </div>
      )}

      <OrderDetailsModal
        order={selectedOrder}
        onClose={() => setSelectedOrder(null)}
        onStatusChange={() => setRefreshKey((k) => k + 1)}
      />
    </div>
  )
}

export default OrdersPage
