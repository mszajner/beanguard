import {
  ExternalLink,
  FileDown,
  KeyRound,
  Loader2,
  Plus,
  Settings2,
} from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch, apiFetchBlob } from '../../utils/api'
import AddLicenceModal from './AddLicenceModal'
import DemoLicenceSettingsModal from './DemoLicenceSettingsModal'
import { Licence } from './EditLicencePage'

interface LicencePage {
  content: Licence[]
  totalElements: number
  totalPages: number
  number: number
  first: boolean
  last: boolean
}

const StatusBadge: React.FC<{ expiration: string | null }> = ({
  expiration,
}) => {
  const { t } = useTranslation('licences')
  if (!expiration) {
    return (
      <span className="inline-flex items-center rounded-full bg-zinc-100 px-2.5 py-0.5 text-xs font-medium text-zinc-600 ring-1 ring-inset ring-zinc-400/20">
        {t('listPage.statusUnlimited')}
      </span>
    )
  }
  const active = new Date(expiration) > new Date()
  return active ? (
    <span className="inline-flex items-center rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-medium text-amber-700 ring-1 ring-inset ring-amber-600/20">
      {t('listPage.statusActive')}
    </span>
  ) : (
    <span className="inline-flex items-center rounded-full bg-red-50 px-2.5 py-0.5 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-600/20">
      {t('listPage.statusExpired')}
    </span>
  )
}

const DemoBadge: React.FC<{ type: string | null }> = ({ type }) => {
  if (type !== 'DEMO') return null
  return (
    <span className="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-600/20">
      DEMO
    </span>
  )
}

type TypeFilter = 'ALL' | 'DEMO' | 'STANDARD'
type ExpirationFilter = 'ALL' | 'ACTIVE' | 'EXPIRED'

const LicencesPage: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation('licences')
  const [data, setData] = useState<LicencePage | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL')
  const [expirationFilter, setExpirationFilter] =
    useState<ExpirationFilter>('ALL')
  const [refreshKey, setRefreshKey] = useState(0)
  const [addVisible, setAddVisible] = useState(false)
  const [demoSettingsVisible, setDemoSettingsVisible] = useState(false)
  const [openingShop, setOpeningShop] = useState<string | null>(null)

  useEffect(() => {
    const timer = setTimeout(() => {
      if (search.length === 0 || search.length >= 2) {
        setDebouncedSearch(search)
        setPage(0)
      }
    }, 400)
    return () => clearTimeout(timer)
  }, [search])

  const fetchLicences = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: '20',
        sort: 'createdAt,desc',
        rk: '' + refreshKey,
      })
      if (debouncedSearch) params.set('query', debouncedSearch)
      if (typeFilter !== 'ALL') params.set('type', typeFilter)
      if (expirationFilter !== 'ALL')
        params.set('expirationStatus', expirationFilter)
      const result = await apiFetch(`/api/licences?${params}`)
      setData(result)
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('listPage.genericError'))
      }
    } finally {
      setLoading(false)
    }
  }, [page, debouncedSearch, typeFilter, expirationFilter, refreshKey, t])

  useEffect(() => {
    // Fetch-on-mount: idiomatic effect, not a derived-state anti-pattern.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchLicences()
  }, [fetchLicences])

  const handleAdded = () => {
    setAddVisible(false)
    setPage(0)
    setRefreshKey((k) => k + 1)
  }

  const downloadCertificate = async (key: string) => {
    try {
      const blob = await apiFetchBlob(`/api/licences/${key}/certificate`)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `certyfikat-${key}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        alert(
          t('listPage.downloadCertificateError') +
            (message ?? t('listPage.unknownError')),
        )
      }
    }
  }

  const openShop = async (key: string) => {
    setOpeningShop(key)
    try {
      const result = await apiFetch(`/api/licences/${key}/token`, {
        method: 'POST',
      })
      window.open(result.shopUrl, '_blank', 'noopener,noreferrer')
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        alert(
          t('listPage.openShopError') + (message ?? t('listPage.unknownError')),
        )
      }
    } finally {
      setOpeningShop(null)
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <KeyRound size={24} style={{ color: 'var(--color-primary)' }} />
            {t('listPage.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            {t('listPage.description')}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <HelpLink path="/panel-admina/licencje" />
          <button
            onClick={() => setDemoSettingsVisible(true)}
            className="btn-secondary flex items-center gap-2"
          >
            <Settings2 size={16} /> {t('listPage.demoConfigButton')}
          </button>
          <button
            onClick={() => setAddVisible(true)}
            className="btn-primary flex items-center gap-2"
          >
            <Plus size={16} /> {t('listPage.addButton')}
          </button>
        </div>
      </div>

      <div className="mb-4 flex flex-wrap gap-3">
        <input
          type="text"
          className="input-field min-w-0 flex-1 sm:max-w-xs"
          placeholder={t('listPage.searchPlaceholder')}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select
          value={typeFilter}
          onChange={(e) => {
            setTypeFilter(e.target.value as TypeFilter)
            setPage(0)
          }}
          className="input-field w-full sm:w-auto sm:max-w-[160px]"
        >
          <option value="ALL">{t('listPage.typeFilterAll')}</option>
          <option value="DEMO">DEMO</option>
          <option value="STANDARD">{t('listPage.typeFilterStandard')}</option>
        </select>
        <select
          value={expirationFilter}
          onChange={(e) => {
            setExpirationFilter(e.target.value as ExpirationFilter)
            setPage(0)
          }}
          className="input-field w-full sm:w-auto sm:max-w-[160px]"
        >
          <option value="ALL">{t('listPage.expirationFilterAll')}</option>
          <option value="ACTIVE">{t('listPage.expirationFilterActive')}</option>
          <option value="EXPIRED">
            {t('listPage.expirationFilterExpired')}
          </option>
        </select>
      </div>

      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>{t('listPage.keyColumn')}</th>
              <th>{t('listPage.licenseeColumn')}</th>
              <th>{t('listPage.contactColumn')}</th>
              <th>{t('listPage.expirationColumn')}</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} className="py-12 text-center">
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
                <td
                  colSpan={4}
                  className="py-10 text-center text-sm text-red-500"
                >
                  {error}
                </td>
              </tr>
            ) : !data || data.content.length === 0 ? (
              <tr>
                <td
                  colSpan={4}
                  className="py-10 text-center text-sm text-zinc-400"
                >
                  {debouncedSearch
                    ? t('listPage.noResultsForQuery')
                    : t('listPage.noData')}
                </td>
              </tr>
            ) : (
              data.content.map((licence) => (
                <tr key={licence.key}>
                  <td>
                    <div className="flex items-center gap-1.5">
                      <button
                        onClick={() =>
                          navigate(`/licences/${licence.key}/edit`)
                        }
                        className="break-all text-left font-mono text-xs text-amber-700 underline underline-offset-2 hover:text-amber-900"
                      >
                        {licence.key}
                      </button>
                      {licence.type === 'STANDARD' && (
                        <button
                          onClick={() => downloadCertificate(licence.key)}
                          className="shrink-0 rounded p-0.5 text-zinc-400 transition hover:bg-amber-50 hover:text-amber-600"
                          title={t('listPage.downloadCertificateTitle')}
                        >
                          <FileDown size={13} />
                        </button>
                      )}
                    </div>
                    {licence.claims &&
                      Object.keys(licence.claims).length > 0 && (
                        <div className="mt-0.5 text-xs text-zinc-400">
                          {Object.entries(licence.claims)
                            .map(([k, v]) => `${k}=${v}`)
                            .join(', ')}
                        </div>
                      )}
                  </td>
                  <td>
                    {licence.companyName && (
                      <div className="font-medium text-zinc-900">
                        {licence.companyName}
                      </div>
                    )}
                    {(licence.street || licence.postCode || licence.city) && (
                      <div className="mt-0.5 text-xs text-zinc-500">
                        {[
                          licence.street,
                          [licence.postCode, licence.city]
                            .filter(Boolean)
                            .join(' '),
                        ]
                          .filter(Boolean)
                          .join(', ')}
                      </div>
                    )}
                    {licence.vatId && (
                      <div className="text-xs text-zinc-500">
                        {t('listPage.vatIdPrefix')}
                        {licence.vatId}
                      </div>
                    )}
                  </td>
                  <td>
                    <a
                      href={`mailto:${licence.email}`}
                      className="font-medium text-zinc-900 hover:text-amber-700"
                    >
                      {licence.email}
                    </a>
                    {licence.phoneNumber && (
                      <a
                        href={`tel:${licence.phoneNumber}`}
                        className="block text-xs text-zinc-500 hover:text-amber-700"
                      >
                        {licence.phoneNumber}
                      </a>
                    )}
                  </td>
                  <td>
                    <div className="mb-1 flex items-center gap-1">
                      <span className="text-sm text-zinc-700">
                        {licence.expiration
                          ? licence.expiration.slice(0, 10)
                          : '—'}
                      </span>
                      <button
                        onClick={() => openShop(licence.key)}
                        disabled={openingShop === licence.key}
                        className="rounded p-0.5 text-zinc-400 transition hover:bg-amber-50 hover:text-amber-600 disabled:opacity-40"
                        title={t('listPage.openShopTitle')}
                      >
                        {openingShop === licence.key ? (
                          <Loader2 size={13} className="animate-spin" />
                        ) : (
                          <ExternalLink size={13} />
                        )}
                      </button>
                    </div>
                    <div className="flex flex-wrap gap-1">
                      <StatusBadge expiration={licence.expiration} />
                      <DemoBadge type={licence.type} />
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="mt-4 flex items-center justify-center gap-3">
          <button
            onClick={() => setPage((p) => p - 1)}
            disabled={data.first}
            className="btn-secondary"
          >
            &lt;
          </button>
          <span className="text-sm text-zinc-600">
            {t('listPage.pageLabel', {
              current: data.number + 1,
              total: data.totalPages,
            })}
          </span>
          <button
            onClick={() => setPage((p) => p + 1)}
            disabled={data.last}
            className="btn-secondary"
          >
            &gt;
          </button>
        </div>
      )}

      <AddLicenceModal
        visible={addVisible}
        onCancel={() => setAddVisible(false)}
        onAdd={handleAdded}
      />

      {demoSettingsVisible && (
        <DemoLicenceSettingsModal
          onClose={() => setDemoSettingsVisible(false)}
        />
      )}
    </div>
  )
}

export default LicencesPage
