import { ShieldCheck } from 'lucide-react'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router-dom'
import HelpLink from '../../components/ui/HelpLink'
import KeyPairSection from './KeyPairSection'

type TabKey = 'licence' | 'token'

const TABS: Array<{ key: TabKey; labelKey: string }> = [
  { key: 'licence', labelKey: 'tabs.licence' },
  { key: 'token', labelKey: 'tabs.token' },
]

const LicenceKeysPage: React.FC = () => {
  const { t } = useTranslation('licence-keys')
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab: TabKey =
    searchParams.get('tab') === 'token' ? 'token' : 'licence'

  const handleTabChange = (tab: TabKey) => {
    setSearchParams(tab === 'licence' ? {} : { tab })
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <ShieldCheck size={24} style={{ color: 'var(--color-primary)' }} />
            {t('page.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">{t('page.description')}</p>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1 rounded-full p-0.5 ring-1 ring-zinc-900/10">
            {TABS.map(({ key, labelKey }) => (
              <button
                key={key}
                type="button"
                onClick={() => handleTabChange(key)}
                className={`rounded-full px-3 py-1.5 text-sm font-semibold transition-colors ${
                  activeTab === key
                    ? 'bg-zinc-900 text-white'
                    : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                {t(labelKey)}
              </button>
            ))}
          </div>
          <HelpLink path="/panel-admina/klucze-kryptograficzne" />
        </div>
      </div>

      {activeTab === 'licence' ? (
        <KeyPairSection
          key="licence"
          namespace="licence-keys"
          issuerParam="LICENCE_ISSUER"
          publicKeyParam="LICENCE_PUBLIC_KEY"
          privateKeyParam="LICENCE_PRIVATE_KEY"
          secretKeyParam="LICENCE_SECRET_KEY"
          regenerateEndpoint="/api/licence-keys/regenerate"
        />
      ) : (
        <KeyPairSection
          key="token"
          namespace="token-keys"
          issuerParam="TOKEN_ISSUER"
          publicKeyParam="TOKEN_PUBLIC_KEY"
          privateKeyParam="TOKEN_PRIVATE_KEY"
          secretKeyParam="TOKEN_SECRET_KEY"
          expirationParam="TOKEN_EXPIRATION"
          regenerateEndpoint="/api/token-keys/regenerate"
        />
      )}
    </div>
  )
}

export default LicenceKeysPage
