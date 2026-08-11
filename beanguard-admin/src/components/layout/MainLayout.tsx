import {
  BookOpen,
  ChevronDown,
  ExternalLink,
  FileText,
  KeyRound,
  LogOut,
  Mail,
  Menu,
  Package,
  Palette,
  Settings,
  Settings2,
  ShieldCheck,
  ShoppingCart,
  UserCircle,
  Users,
  X,
} from 'lucide-react'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/useAuth'
import { useConfig } from '../../context/useConfig'
import { DOCS_URL, SHOP_URL } from '../../utils/api'
import LanguageSwitcher from '../LanguageSwitcher'

const navGroups = [
  {
    labelKey: 'mainLayout.navGroups.management',
    items: [
      { to: '/licences', icon: KeyRound, labelKey: 'mainLayout.nav.licences' },
    ],
  },
  {
    labelKey: 'mainLayout.navGroups.shop',
    items: [
      { to: '/orders', icon: ShoppingCart, labelKey: 'mainLayout.nav.orders' },
      { to: '/products', icon: Package, labelKey: 'mainLayout.nav.products' },
      { to: '/branding', icon: Palette, labelKey: 'mainLayout.nav.branding' },
      { to: '/legal', icon: Settings2, labelKey: 'mainLayout.nav.legal' },
    ],
  },
  {
    labelKey: 'mainLayout.navGroups.administration',
    items: [
      { to: '/users', icon: Users, labelKey: 'mainLayout.nav.users' },
      {
        to: '/mail-templates',
        icon: Mail,
        labelKey: 'mainLayout.nav.mailTemplates',
      },
      {
        to: '/pdf-templates',
        icon: FileText,
        labelKey: 'mainLayout.nav.pdfTemplates',
      },
      {
        to: '/licence-keys',
        icon: ShieldCheck,
        labelKey: 'mainLayout.nav.licenceKeys',
      },
      {
        to: '/parameters',
        icon: Settings,
        labelKey: 'mainLayout.nav.parameters',
      },
    ],
  },
]

interface SidebarProps {
  onClose?: () => void
}

const Sidebar: React.FC<SidebarProps> = ({ onClose }) => {
  const navigate = useNavigate()
  const location = useLocation()
  const { title, logoUrl } = useConfig()
  const { t } = useTranslation()

  return (
    <div className="flex h-full flex-col bg-zinc-900">
      {/* Logo */}
      <div className="flex h-14 shrink-0 items-center gap-3 border-b border-white/5 px-6">
        <img
          src={logoUrl ?? '/logo-full-dark.png'}
          alt={title}
          className="h-7 shrink-0 rounded-lg object-contain"
        />
        <span className="sr-only">{title}</span>
        {onClose && (
          <button
            onClick={onClose}
            className="ml-auto text-zinc-400 transition-colors hover:text-white lg:hidden"
          >
            <X size={18} />
          </button>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto px-4 py-6">
        {navGroups.map((group) => (
          <div key={group.labelKey} className="mb-6">
            <p className="mb-2 px-3 text-[11px] font-semibold uppercase tracking-wider text-zinc-400">
              {t(group.labelKey)}
            </p>
            <ul className="space-y-0.5">
              {group.items.map((item) => {
                const isActive = location.pathname.startsWith(item.to)
                const Icon = item.icon
                return (
                  <li key={item.to} className="relative">
                    {isActive && (
                      <div
                        className="absolute bottom-[7px] left-0 top-[7px] w-0.5 rounded-full"
                        style={{ backgroundColor: 'var(--color-primary)' }}
                      />
                    )}
                    <button
                      onClick={() => {
                        navigate(item.to)
                        onClose?.()
                      }}
                      className={`flex w-full items-center gap-3 rounded-lg py-2 pl-4 pr-3 text-sm font-medium transition-colors ${
                        isActive
                          ? 'bg-white/5 text-white'
                          : 'text-zinc-400 hover:bg-white/5 hover:text-white'
                      }`}
                    >
                      <Icon
                        size={16}
                        className={isActive ? '' : 'text-zinc-500'}
                        style={
                          isActive ? { color: 'var(--color-primary)' } : {}
                        }
                      />
                      {t(item.labelKey)}
                    </button>
                  </li>
                )
              })}
            </ul>
          </div>
        ))}
      </nav>
    </div>
  )
}

const MainLayout: React.FC = () => {
  const [mobileOpen, setMobileOpen] = useState(false)
  const [userMenuOpen, setUserMenuOpen] = useState(false)
  const { logout } = useAuth()
  const navigate = useNavigate()
  const { t, i18n } = useTranslation()
  const docsLocale = i18n.resolvedLanguage === 'en' ? 'en' : 'pl'

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen bg-zinc-50">
      {/* Desktop sidebar */}
      <div className="hidden lg:fixed lg:inset-y-0 lg:left-0 lg:z-50 lg:flex lg:w-72 lg:flex-col">
        <Sidebar />
      </div>

      {/* Mobile sidebar overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="fixed inset-0 bg-zinc-900/50"
            onClick={() => setMobileOpen(false)}
          />
          <div className="fixed inset-y-0 left-0 flex w-72 flex-col shadow-xl">
            <Sidebar onClose={() => setMobileOpen(false)} />
          </div>
        </div>
      )}

      {/* Main content */}
      <div className="flex min-w-0 flex-1 flex-col lg:pl-72">
        {/* Header */}
        <header className="sticky top-0 z-40 flex h-14 shrink-0 items-center border-b border-zinc-900/10 bg-white/90 px-4 backdrop-blur-sm sm:px-6">
          <button
            onClick={() => setMobileOpen(true)}
            className="-m-2 rounded-lg p-2 text-zinc-700 transition-colors hover:bg-zinc-100 lg:hidden"
          >
            <Menu size={20} />
          </button>

          <div className="flex flex-1 items-center justify-end gap-3">
            <a
              href={`${DOCS_URL}/${docsLocale}`}
              target="_blank"
              rel="noopener noreferrer"
              title={t('mainLayout.docs')}
              className="flex items-center gap-2 rounded-full px-3 py-1.5 text-sm text-zinc-700 ring-1 ring-zinc-900/10 transition-all hover:ring-zinc-900/20"
            >
              <BookOpen size={16} className="text-zinc-400" />
              <span className="hidden font-medium sm:block">
                {t('mainLayout.docs')}
              </span>
            </a>
            <a
              href={SHOP_URL}
              target="_blank"
              rel="noopener noreferrer"
              title={t('mainLayout.openShop')}
              className="flex items-center gap-2 rounded-full px-3 py-1.5 text-sm text-zinc-700 ring-1 ring-zinc-900/10 transition-all hover:ring-zinc-900/20"
            >
              <ExternalLink size={16} className="text-zinc-400" />
              <span className="hidden font-medium sm:block">
                {t('mainLayout.openShop')}
              </span>
            </a>
            <LanguageSwitcher />
            <div className="relative">
              <button
                onClick={() => setUserMenuOpen(!userMenuOpen)}
                className="flex items-center gap-2 rounded-full px-3 py-1.5 text-sm text-zinc-700 ring-1 ring-zinc-900/10 transition-all hover:ring-zinc-900/20"
              >
                <UserCircle size={16} className="text-zinc-400" />
                <span className="hidden font-medium sm:block">
                  {t('mainLayout.administrator')}
                </span>
                <ChevronDown size={14} className="text-zinc-400" />
              </button>

              {userMenuOpen && (
                <>
                  <div
                    className="fixed inset-0"
                    onClick={() => setUserMenuOpen(false)}
                  />
                  <div className="absolute right-0 z-50 mt-2 w-48 rounded-xl bg-white py-1 shadow-lg ring-1 ring-zinc-900/10">
                    <button
                      onClick={handleLogout}
                      className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-zinc-700 transition-colors hover:bg-zinc-50"
                    >
                      <LogOut size={14} className="text-zinc-400" />
                      {t('mainLayout.logout')}
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 p-4 sm:p-6 lg:p-8">
          <div>
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}

export default MainLayout
