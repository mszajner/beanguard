import { AlertTriangle, Loader2, Palette, X } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch } from '../../utils/api'

interface Parameter {
  name: string
  value: string | null
  createdAt: string | null
  updatedAt: string | null
}

const BRANDING_PARAM_NAMES = [
  'BRANDING_TITLE',
  'BRANDING_LOGO_URL',
  'BRANDING_WEBSITE_URL',
  'BRANDING_FAVICON_URL',
  'BRANDING_SUPPORT_EMAIL',
  'BRANDING_PRIMARY_COLOR',
] as const

const HEX_COLOR_RE = /^#[0-9a-fA-F]{6}$/

const BrandingPage: React.FC = () => {
  const { t } = useTranslation('branding')
  const [params, setParams] = useState<Record<string, Parameter>>({})
  const [formValues, setFormValues] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)
  const [showReloadPrompt, setShowReloadPrompt] = useState(false)
  const [logoBroken, setLogoBroken] = useState(false)
  const [faviconBroken, setFaviconBroken] = useState(false)

  const fetchParams = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result: Parameter[] = await apiFetch('/api/parameters')
      const byName: Record<string, Parameter> = Object.fromEntries(
        result
          .filter((p) =>
            (BRANDING_PARAM_NAMES as readonly string[]).includes(p.name),
          )
          .map((p): [string, Parameter] => [p.name, p]),
      )
      setParams(byName)
      setFormValues(
        Object.fromEntries(
          BRANDING_PARAM_NAMES.map((name): [string, string] => [
            name,
            byName[name]?.value ?? '',
          ]),
        ),
      )
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('genericError'))
      }
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    // Fetch-on-mount: idiomatic effect, not a derived-state anti-pattern.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchParams()
  }, [fetchParams])

  const handleChange = (name: string, value: string) => {
    setFormValues((prev) => ({ ...prev, [name]: value }))
    if (name === 'BRANDING_LOGO_URL') setLogoBroken(false)
    if (name === 'BRANDING_FAVICON_URL') setFaviconBroken(false)
  }

  const changedNames = BRANDING_PARAM_NAMES.filter(
    (name) => formValues[name] !== (params[name]?.value ?? ''),
  )
  const isDirty = changedNames.length > 0

  const handleSave = async () => {
    setSaving(true)
    setError(null)
    setSaved(false)
    try {
      await Promise.all(
        changedNames.map((name) =>
          apiFetch(`/api/parameters/${name}`, {
            method: 'PUT',
            body: JSON.stringify({ value: formValues[name] }),
          }),
        ),
      )
      await fetchParams()
      setSaved(true)
      setShowReloadPrompt(true)
      setTimeout(() => setSaved(false), 2000)
    } catch (err) {
      const message = err instanceof Error ? err.message : undefined
      if (message !== 'session_expired') {
        setError(message || t('genericError'))
      }
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="py-12 text-center">
        <Loader2
          className="inline-block animate-spin text-amber-500"
          size={24}
        />
        <p className="mt-2 text-sm text-zinc-400">{t('loading')}</p>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <Palette size={24} style={{ color: 'var(--color-primary)' }} />
            {t('page.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">{t('page.description')}</p>
        </div>
        <HelpLink path="/panel-admina/branding" />
      </div>

      {error && (
        <div className="mb-6 rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
          {error}
        </div>
      )}

      {saved && (
        <div className="mb-6 rounded-xl bg-emerald-50 p-3 text-sm text-emerald-700 ring-1 ring-inset ring-emerald-200">
          {t('saved')}
        </div>
      )}

      <div className="space-y-6">
        <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
          <h2 className="mb-4 text-xs font-semibold uppercase tracking-wider text-zinc-400">
            {t('groups.general')}
          </h2>
          <div className="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
            <div>
              <label className="label-text" htmlFor="branding-title">
                {t('fields.title')}
              </label>
              <input
                id="branding-title"
                type="text"
                value={formValues.BRANDING_TITLE ?? ''}
                onChange={(e) =>
                  handleChange('BRANDING_TITLE', e.target.value)
                }
                className="input-field"
              />
              <p className="mt-1.5 text-xs text-zinc-500">
                {t('descriptions.title')}
              </p>
            </div>

            <div>
              <label className="label-text" htmlFor="branding-website-url">
                {t('fields.websiteUrl')}
              </label>
              <input
                id="branding-website-url"
                type="text"
                value={formValues.BRANDING_WEBSITE_URL ?? ''}
                onChange={(e) =>
                  handleChange('BRANDING_WEBSITE_URL', e.target.value)
                }
                className="input-field"
              />
              <p className="mt-1.5 text-xs text-zinc-500">
                {t('descriptions.websiteUrl')}
              </p>
            </div>

            <div>
              <label className="label-text" htmlFor="branding-support-email">
                {t('fields.supportEmail')}
              </label>
              <input
                id="branding-support-email"
                type="email"
                value={formValues.BRANDING_SUPPORT_EMAIL ?? ''}
                onChange={(e) =>
                  handleChange('BRANDING_SUPPORT_EMAIL', e.target.value)
                }
                className="input-field"
              />
              <p className="mt-1.5 text-xs text-zinc-500">
                {t('descriptions.supportEmail')}
              </p>
            </div>
          </div>
        </div>

        <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
          <h2 className="mb-4 text-xs font-semibold uppercase tracking-wider text-zinc-400">
            {t('groups.visual')}
          </h2>
          <div className="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
            <div>
              <label className="label-text" htmlFor="branding-logo-url">
                {t('fields.logoUrl')}
              </label>
              <input
                id="branding-logo-url"
                type="text"
                value={formValues.BRANDING_LOGO_URL ?? ''}
                onChange={(e) =>
                  handleChange('BRANDING_LOGO_URL', e.target.value)
                }
                className="input-field"
              />
              <p className="mt-1.5 text-xs text-zinc-500">
                {t('descriptions.logoUrl')}
              </p>
              {formValues.BRANDING_LOGO_URL && !logoBroken && (
                <img
                  src={formValues.BRANDING_LOGO_URL}
                  alt=""
                  onError={() => setLogoBroken(true)}
                  className="mt-2 h-10 w-auto rounded object-contain"
                />
              )}
            </div>

            <div>
              <label className="label-text" htmlFor="branding-favicon-url">
                {t('fields.faviconUrl')}
              </label>
              <input
                id="branding-favicon-url"
                type="text"
                value={formValues.BRANDING_FAVICON_URL ?? ''}
                onChange={(e) =>
                  handleChange('BRANDING_FAVICON_URL', e.target.value)
                }
                className="input-field"
              />
              <p className="mt-1.5 text-xs text-zinc-500">
                {t('descriptions.faviconUrl')}
              </p>
              {formValues.BRANDING_FAVICON_URL && !faviconBroken && (
                <img
                  src={formValues.BRANDING_FAVICON_URL}
                  alt=""
                  onError={() => setFaviconBroken(true)}
                  className="mt-2 h-6 w-6 rounded object-contain"
                />
              )}
            </div>

            <div>
              <label className="label-text" htmlFor="branding-primary-color">
                {t('fields.primaryColor')}
              </label>
              <div className="flex items-center gap-2">
                <input
                  id="branding-primary-color"
                  type="text"
                  value={formValues.BRANDING_PRIMARY_COLOR ?? ''}
                  onChange={(e) =>
                    handleChange('BRANDING_PRIMARY_COLOR', e.target.value)
                  }
                  className="input-field font-mono"
                />
                <input
                  type="color"
                  aria-label={t('fields.primaryColor')}
                  value={
                    HEX_COLOR_RE.test(formValues.BRANDING_PRIMARY_COLOR ?? '')
                      ? formValues.BRANDING_PRIMARY_COLOR
                      : '#000000'
                  }
                  onChange={(e) =>
                    handleChange('BRANDING_PRIMARY_COLOR', e.target.value)
                  }
                  className="h-10 w-10 shrink-0 cursor-pointer rounded border border-zinc-200"
                />
              </div>
              <p className="mt-1.5 text-xs text-zinc-500">
                {t('descriptions.primaryColor')}
              </p>
            </div>
          </div>
        </div>

        <div className="flex justify-end">
          <button
            type="button"
            onClick={handleSave}
            className="btn-primary"
            disabled={!isDirty || saving}
          >
            {saving ? t('saving') : t('save')}
          </button>
        </div>
      </div>

      {showReloadPrompt && (
        <div
          role="status"
          aria-live="polite"
          className="fixed inset-x-0 bottom-20 z-50 mx-auto flex w-[calc(100%-2rem)] max-w-md items-center gap-3 rounded-2xl bg-amber-50 px-4 py-3 shadow-xl ring-1 ring-inset ring-amber-200"
        >
          <AlertTriangle className="shrink-0 text-amber-600" size={20} />
          <span className="flex-1 text-sm font-medium text-amber-900">
            {t('reloadPrompt.message')}
          </span>
          <div className="flex shrink-0 items-center gap-2">
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="rounded-full bg-amber-500 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-amber-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-600 focus-visible:ring-offset-2"
            >
              {t('reloadPrompt.reloadButton')}
            </button>
            <button
              type="button"
              onClick={() => setShowReloadPrompt(false)}
              aria-label={t('reloadPrompt.dismiss')}
              className="text-amber-500 transition-colors hover:text-amber-700"
            >
              <X size={18} />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export default BrandingPage
