import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { useConfig } from '../context/useConfig'
import { apiFetch } from '../utils/api'

const Login: React.FC = () => {
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [sessionExpired] = useState(() => {
    if (sessionStorage.getItem('sessionExpired')) {
      sessionStorage.removeItem('sessionExpired')
      return true
    }
    return false
  })
  const navigate = useNavigate()
  const { login } = useAuth()
  const { title, logoUrl, supportEmail } = useConfig()

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!email || !password) {
      setError(t('login.emptyFieldsError'))
      return
    }
    setLoading(true)
    setError('')
    try {
      const response = await apiFetch('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      })
      login(response)
      navigate('/licences')
    } catch (err) {
      setError(err instanceof Error ? err.message : t('login.genericError'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-white">
      {/* Left decorative panel */}
      <div className="hidden flex-col justify-between bg-zinc-900 p-12 lg:flex lg:w-1/2">
        <div className="flex items-center gap-3">
          <img
            src={logoUrl ?? '/logo-full-dark.png'}
            alt={title}
            className="h-8 rounded-lg object-contain"
          />
          <span className="sr-only">{title}</span>
        </div>
        <div>
          <p className="text-2xl font-semibold leading-snug text-white">
            {t('login.tagline')}
          </p>
          <p className="mt-3 text-sm leading-relaxed text-zinc-400">
            {t('login.subtitle')}
          </p>
        </div>
        <div className="space-y-1 text-xs text-zinc-600">
          <p>
            {t('login.copyrightPrefix')}
            {title}
          </p>
          {supportEmail && (
            <p>
              {t('login.supportLabel')}{' '}
              <a
                href={`mailto:${supportEmail}`}
                className="underline hover:text-zinc-400"
              >
                {supportEmail}
              </a>
            </p>
          )}
        </div>
      </div>

      {/* Right login form */}
      <div className="flex flex-1 flex-col justify-center px-6 py-12 lg:px-12">
        <div className="mx-auto w-full max-w-sm">
          {/* Mobile logo */}
          <div className="mb-10 flex items-center gap-3 lg:hidden">
            <img
              src={logoUrl ?? '/logo-full-dark.png'}
              alt={title}
              className="h-8 rounded-lg object-contain"
            />
            <span className="sr-only">{title}</span>
          </div>

          <h2 className="text-2xl font-bold tracking-tight text-zinc-900">
            {t('login.heading')}
          </h2>
          <p className="mt-2 text-sm text-zinc-500">
            {t('login.instructions')}
          </p>

          {(sessionExpired || error) && (
            <div className="mt-6 space-y-3">
              {sessionExpired && (
                <div className="rounded-xl bg-amber-50 p-3 text-sm text-amber-800 ring-1 ring-inset ring-amber-200">
                  {t('login.sessionExpiredMessage')}
                </div>
              )}
              {error && (
                <div className="rounded-xl bg-red-50 p-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
                  {error}
                </div>
              )}
            </div>
          )}

          <form onSubmit={handleLogin} className="mt-8 space-y-5">
            <div>
              <label htmlFor="email" className="label-text">
                {t('login.emailLabel')}
              </label>
              <input
                id="email"
                type="email"
                required
                autoComplete="email"
                className="input-field"
                placeholder={t('login.emailPlaceholder')}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="password" className="label-text">
                {t('login.passwordLabel')}
              </label>
              <input
                id="password"
                type="password"
                required
                autoComplete="current-password"
                className="input-field"
                placeholder={t('login.passwordPlaceholder')}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full !rounded-xl py-2.5"
            >
              {loading
                ? t('login.submitButtonLoading')
                : t('login.submitButton')}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

export default Login
