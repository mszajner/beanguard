import {
  AlertCircle,
  Info,
  Loader2,
  Minus,
  Plus,
  ShieldCheck,
} from 'lucide-react'
import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { HttpError, shopFetch } from '../api'
import { computeCredit, effectiveUnitPrice } from '../pricing'
import {
  CartItem,
  CreateOrderRequest,
  OrderPeriod,
  Product,
  ShopLicenceContext,
} from '../types'
import { useConfig } from '../useConfig'

type CartState = Record<string, number>

const PERIOD_VALUES: OrderPeriod[] = ['ONE_YEAR', 'ONE_MONTH']

const PERIOD_LABEL_KEYS: Record<OrderPeriod, string> = {
  ONE_YEAR: 'period.oneYear',
  ONE_MONTH: 'period.oneMonth',
}

interface FormState {
  email: string
  vatId: string
  companyName: string
  street: string
  postCode: string
  city: string
  phoneNumber: string
}

const EMPTY_FORM: FormState = {
  email: '',
  vatId: '',
  companyName: '',
  street: '',
  postCode: '',
  city: '',
  phoneNumber: '',
}

const SectionHeader: React.FC<{ number: number; title: string }> = ({
  number,
  title,
}) => (
  <div className="mb-5 flex items-center gap-3">
    <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-zinc-900 text-xs font-bold text-white">
      {number}
    </span>
    <h2 className="font-semibold text-zinc-900">{title}</h2>
  </div>
)

const ShopPage: React.FC = () => {
  const { t } = useTranslation()
  const [products, setProducts] = useState<Product[]>([])
  const [cart, setCart] = useState<CartState>({})
  const [period, setPeriod] = useState<OrderPeriod>('ONE_YEAR')
  const [licenceId, setLicenceId] = useState<string | undefined>(undefined)
  const [form, setForm] = useState<FormState>(EMPTY_FORM)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [tokenExpired, setTokenExpired] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [licenceNetAmount, setLicenceNetAmount] = useState<number | null>(null)
  const [licenceLastPeriod, setLicenceLastPeriod] = useState<string | null>(
    null,
  )
  const [licenceExpiration, setLicenceExpiration] = useState<string | null>(
    null,
  )
  const [licenceType, setLicenceType] = useState<'DEMO' | 'STANDARD' | null>(
    null,
  )

  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const {
    title,
    currency,
    termsUrl,
    privacyUrl,
    vatRate,
    companyName,
    companyNip,
    companyKrs,
    companyAddress,
    companyPhone,
    supportEmail,
  } = useConfig()
  const token = searchParams.get('token')
  const isModification = !!token
  const [limitClaims, setLimitClaims] = useState<Record<string, number>>({})
  const [featureClaims, setFeatureClaims] = useState<string[]>([])
  const [prevPeriod, setPrevPeriod] = useState<OrderPeriod>(period)

  const buildInitialCart = (
    prods: Product[],
    claims: Record<string, number>,
    activeFeat: string[],
  ): CartState => {
    const initial: CartState = {}
    prods.forEach((p) => {
      if (p.required || activeFeat.includes(p.claim)) {
        initial[p.id] =
          p.type === 'LIMIT' ? Math.max(1, claims[p.claim] ?? 1) : 1
      }
    })
    return initial
  }

  useEffect(() => {
    const productsPromise = shopFetch('/api/open/shop/products').then(
      (d) => d as Product[],
    )

    if (token) {
      const ctxPromise = shopFetch(
        `/api/open/shop/licence?token=${encodeURIComponent(token)}`,
      )
        .then((d) => d as ShopLicenceContext)
        .catch((err) => {
          if (
            err instanceof HttpError &&
            (err.status === 401 || err.status === 403)
          ) {
            setTokenExpired(true)
          }
          return null
        })

      Promise.all([productsPromise, ctxPromise])
        .then(([prods, ctx]) => {
          const claims = ctx?.limitClaims ?? {}
          const activeFeat = ctx?.featureClaims ?? []
          setProducts(prods)
          setLimitClaims(claims)
          setFeatureClaims(activeFeat)
          setLicenceNetAmount(ctx?.licenceNetAmount ?? null)
          setLicenceLastPeriod(ctx?.licenceLastPeriod ?? null)
          setLicenceExpiration(ctx?.expiration ?? null)
          setLicenceType(ctx?.licenceType ?? null)
          if (
            ctx?.licenceLastPeriod === 'ONE_YEAR' ||
            ctx?.licenceLastPeriod === 'ONE_MONTH'
          ) {
            setPeriod(ctx.licenceLastPeriod)
          }
          setCart(buildInitialCart(prods, claims, activeFeat))
          if (ctx) {
            setLicenceId(ctx.licenceId)
            setForm({
              email: ctx.email,
              vatId: ctx.vatId,
              companyName: ctx.companyName ?? '',
              street: ctx.street ?? '',
              postCode: ctx.postCode ?? '',
              city: ctx.city ?? '',
              phoneNumber: ctx.phoneNumber ?? '',
            })
          }
        })
        .catch((err) => setLoadError((err as Error).message))
        .finally(() => setLoading(false))
    } else {
      productsPromise
        .then((prods) => {
          setProducts(prods)
          setCart(buildInitialCart(prods, {}, []))
          setLimitClaims({})
        })
        .catch((err) => setLoadError((err as Error).message))
        .finally(() => setLoading(false))
    }
    // Intentionally runs once on mount only, from the token present at load time.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // When period changes in modification mode, re-enforce required product pre-selection.
  // Adjusted during render (not an effect) per https://react.dev/learn/you-might-not-need-an-effect
  if (period !== prevPeriod) {
    setPrevPeriod(period)
    if (isModification && products.length > 0) {
      setCart((prev) => ({
        ...prev,
        ...buildInitialCart(products, limitClaims, featureClaims),
      }))
    }
  }

  const setQuantity = (productId: string, qty: number) => {
    setCart((prev) => {
      if (qty <= 0) {
        const next = { ...prev }
        delete next[productId]
        return next
      }
      return { ...prev, [productId]: qty }
    })
  }

  const toggleFeature = (productId: string) => {
    setCart((prev) =>
      prev[productId]
        ? (() => {
            const n = { ...prev }
            delete n[productId]
            return n
          })()
        : { ...prev, [productId]: 1 },
    )
  }

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const cartItems: CartItem[] = Object.entries(cart).map(
    ([productId, quantity]) => ({ productId, quantity }),
  )
  const cartIsEmpty = cartItems.length === 0

  const cartTotal = cartItems.reduce((sum, item) => {
    const p = products.find((x) => x.id === item.productId)
    return sum + (p ? effectiveUnitPrice(p, period) * item.quantity : 0)
  }, 0)

  const credit = isModification
    ? computeCredit(licenceNetAmount, licenceLastPeriod, licenceExpiration)
    : 0
  const netAfterCredit = Math.max(0, cartTotal - credit)

  const orderIsZero = !cartIsEmpty && netAfterCredit === 0

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (cartIsEmpty || orderIsZero) return
    setSubmitting(true)
    setSubmitError(null)
    try {
      const body: CreateOrderRequest = {
        period,
        licenceId,
        vatId: form.vatId,
        email: form.email,
        companyName: form.companyName,
        street: form.street,
        postCode: form.postCode,
        city: form.city,
        phoneNumber: form.phoneNumber,
        items: cartItems.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
        })),
      }
      await shopFetch('/api/open/shop/orders', {
        method: 'POST',
        body: JSON.stringify(body),
      })
      navigate('/confirmation')
    } catch (err) {
      if (err instanceof HttpError && err.status === 409) {
        try {
          const body = JSON.parse(err.message)
          if (
            Array.isArray(body.openOrderIds) &&
            body.openOrderIds.length > 0
          ) {
            setSubmitError(
              t('errors.openOrdersConflict', {
                ids: body.openOrderIds.join(', '),
              }),
            )
            return
          }
        } catch {
          /* fall through */
        }
      }
      setSubmitError((err as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  if (tokenExpired) {
    return (
      <div className="flex flex-1 flex-col bg-zinc-50">
        <div className="flex flex-1 items-center justify-center px-4 py-16">
          <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center ring-1 ring-zinc-900/5">
            <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-amber-100">
              <AlertCircle size={24} className="text-amber-600" />
            </div>
            <h1 className="mb-2 text-lg font-semibold text-zinc-900">
              {t('tokenExpired.heading')}
            </h1>
            <p className="text-sm text-zinc-500">{t('tokenExpired.body')}</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="flex-1 bg-zinc-50">
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:py-12">
        {token && (
          <div className="mb-6 flex items-start gap-3 rounded-xl bg-blue-50 px-4 py-3 text-sm text-blue-800 ring-1 ring-inset ring-blue-200">
            <Info size={16} className="mt-0.5 shrink-0" />
            {licenceType === 'DEMO' ? (
              <span>{t('modificationBanner.demo')}</span>
            ) : (
              <span>{t('modificationBanner.standard')}</span>
            )}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="lg:grid lg:grid-cols-5 lg:items-start lg:gap-10">
            {/* Left column */}
            <div className="space-y-6 lg:col-span-3">
              {/* Section 1 — products */}
              <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
                <SectionHeader number={1} title={t('section1.title')} />

                <div className="mb-6 flex flex-wrap gap-2">
                  {PERIOD_VALUES.map((value) => (
                    <button
                      key={value}
                      type="button"
                      onClick={() => setPeriod(value)}
                      className={`rounded-full px-4 py-1.5 text-sm font-medium transition ${
                        period === value
                          ? 'bg-zinc-900 text-white'
                          : 'bg-zinc-100 text-zinc-600 hover:bg-zinc-200'
                      }`}
                    >
                      {t(PERIOD_LABEL_KEYS[value])}
                    </button>
                  ))}
                </div>

                {loading && (
                  <div className="flex items-center gap-2 py-4 text-sm text-zinc-400">
                    <Loader2 className="animate-spin" size={16} />{' '}
                    {t('section1.loadingProducts')}
                  </div>
                )}
                {loadError && (
                  <div className="flex items-start gap-3 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
                    <AlertCircle size={16} className="mt-0.5 shrink-0" />
                    <span>{loadError}</span>
                  </div>
                )}
                {!loading && !loadError && products.length === 0 && (
                  <p className="py-4 text-sm text-zinc-400">
                    {t('section1.noProducts')}
                  </p>
                )}

                <div className="divide-y divide-zinc-100">
                  {products.map((product) => {
                    const qty = cart[product.id] ?? 0
                    const isSelected = qty > 0
                    const isRequired = product.required
                    const currentQty =
                      isModification && product.type === 'LIMIT'
                        ? (limitClaims[product.claim] ?? 0)
                        : 0
                    const minQty =
                      isModification && product.type === 'LIMIT'
                        ? currentQty
                        : isRequired
                          ? 1
                          : 0
                    const unitPrice = effectiveUnitPrice(product, period)
                    const wasActiveFeat =
                      isModification &&
                      product.type === 'FEATURE' &&
                      featureClaims.includes(product.claim)

                    const handleCheckbox = () => {
                      if (isRequired) return
                      if (product.type === 'FEATURE') {
                        toggleFeature(product.id)
                      } else {
                        setQuantity(product.id, isSelected ? 0 : 1)
                      }
                    }

                    const controls = (
                      <>
                        {product.type === 'LIMIT' && (
                          <div
                            className="flex items-center gap-1.5"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <button
                              type="button"
                              onClick={() => setQuantity(product.id, qty - 1)}
                              disabled={qty <= minQty}
                              className="flex h-6 w-6 items-center justify-center rounded-full bg-zinc-100 text-zinc-600 transition hover:bg-zinc-200 disabled:opacity-40"
                            >
                              <Minus size={11} />
                            </button>
                            <input
                              type="number"
                              min={minQty}
                              max={99}
                              value={qty === 0 ? '' : qty}
                              placeholder="0"
                              onChange={(e) => {
                                const v = parseInt(e.target.value, 10)
                                setQuantity(
                                  product.id,
                                  isNaN(v)
                                    ? minQty
                                    : Math.min(99, Math.max(minQty, v)),
                                )
                              }}
                              className="input-field w-11 px-1 py-1 text-center text-sm font-medium"
                            />
                            <button
                              type="button"
                              onClick={() => setQuantity(product.id, qty + 1)}
                              disabled={qty >= 99}
                              className="flex h-6 w-6 items-center justify-center rounded-full bg-zinc-100 text-zinc-600 transition hover:bg-zinc-200 disabled:opacity-40"
                            >
                              <Plus size={11} />
                            </button>
                          </div>
                        )}
                        <div className="min-w-[4.5rem] text-right">
                          <span className="text-sm font-semibold tabular-nums text-zinc-900">
                            {unitPrice.toFixed(2)}
                          </span>
                          <span className="text-xs text-zinc-400">
                            {' '}
                            {currency}
                          </span>
                        </div>
                      </>
                    )

                    return (
                      <div
                        key={product.id}
                        className={`py-4 transition ${
                          isSelected ? '-mx-6 bg-amber-50 px-6' : ''
                        }`}
                      >
                        <div className="flex items-start gap-3">
                          {/* Checkbox */}
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={handleCheckbox}
                            disabled={isRequired}
                            className={`mt-0.5 h-4 w-4 shrink-0 rounded border-zinc-300 ${isRequired ? 'cursor-not-allowed opacity-70' : 'cursor-pointer'}`}
                            style={{ accentColor: 'var(--color-primary)' }}
                          />

                          {/* Name + description + mobile controls */}
                          <div
                            className={`min-w-0 flex-1 select-none ${isRequired ? 'cursor-default' : 'cursor-pointer'}`}
                            onClick={isRequired ? undefined : handleCheckbox}
                          >
                            <div className="flex flex-wrap items-center gap-2">
                              <p
                                className={`text-sm font-medium leading-snug ${isSelected ? 'text-zinc-900' : 'text-zinc-700'}`}
                              >
                                {product.name}
                              </p>
                              {isRequired && (
                                <span className="text-xs font-normal text-zinc-400">
                                  {t('product.required')}
                                </span>
                              )}
                              {isModification &&
                                product.type === 'LIMIT' &&
                                currentQty > 0 && (
                                  <span className="text-xs font-normal text-zinc-400">
                                    {t('product.currentQtyPrefix')}
                                    {currentQty}
                                  </span>
                                )}
                            </div>
                            {product.description && (
                              <p className="mt-0.5 text-xs leading-relaxed text-zinc-400">
                                {product.description}
                              </p>
                            )}
                            {wasActiveFeat && !isSelected && (
                              <p className="mt-1 text-xs text-amber-600">
                                {t('product.disableWarning')}
                              </p>
                            )}
                            {/* Mobile: controls below description, right-aligned */}
                            <div
                              className="mt-2.5 flex items-center justify-end gap-3 sm:hidden"
                              onClick={(e) => e.stopPropagation()}
                            >
                              {controls}
                            </div>
                          </div>

                          {/* Desktop: controls in the same row */}
                          <div className="hidden shrink-0 items-center gap-3 sm:flex">
                            {controls}
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>

                {isModification && Object.keys(limitClaims).length > 0 && (
                  <p className="mt-4 text-xs text-zinc-400">
                    {t('limitsHint.text')}{' '}
                    {supportEmail ? (
                      <a
                        href={`mailto:${supportEmail}`}
                        className="underline hover:text-zinc-600"
                      >
                        {supportEmail}
                      </a>
                    ) : (
                      t('limitsHint.contactSupportFallback')
                    )}
                    .
                  </p>
                )}
              </div>

              {/* Section 2 — billing */}
              <div className="rounded-2xl bg-white p-6 ring-1 ring-zinc-900/5">
                <SectionHeader number={2} title={t('section2.title')} />

                <div className="space-y-4">
                  <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
                    <div>
                      <label className="label-text" htmlFor="email">
                        {t('form.emailLabel')}
                      </label>
                      <input
                        required
                        type="email"
                        id="email"
                        name="email"
                        value={form.email}
                        onChange={handleFormChange}
                        className="input-field"
                        placeholder={t('form.emailPlaceholder')}
                      />
                    </div>
                    <div>
                      <label className="label-text" htmlFor="vatId">
                        {t('form.vatIdLabel')}
                      </label>
                      <input
                        required
                        type="text"
                        id="vatId"
                        name="vatId"
                        value={form.vatId}
                        onChange={handleFormChange}
                        className="input-field"
                        placeholder={t('form.vatIdPlaceholder')}
                        pattern="[0-9]{10}"
                        title={t('form.vatIdValidation')}
                        maxLength={10}
                      />
                    </div>
                  </div>
                  <div>
                    <label className="label-text" htmlFor="companyName">
                      {t('form.companyNameLabel')}
                    </label>
                    <input
                      required
                      type="text"
                      id="companyName"
                      name="companyName"
                      value={form.companyName}
                      onChange={handleFormChange}
                      className="input-field"
                      placeholder={t('form.companyNamePlaceholder')}
                    />
                  </div>
                  <div>
                    <label className="label-text" htmlFor="street">
                      {t('form.streetLabel')}
                    </label>
                    <input
                      required
                      type="text"
                      id="street"
                      name="street"
                      value={form.street}
                      onChange={handleFormChange}
                      className="input-field"
                      placeholder={t('form.streetPlaceholder')}
                    />
                  </div>
                  <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
                    <div>
                      <label className="label-text" htmlFor="postCode">
                        {t('form.postCodeLabel')}
                      </label>
                      <input
                        required
                        type="text"
                        id="postCode"
                        name="postCode"
                        value={form.postCode}
                        onChange={handleFormChange}
                        className="input-field"
                        placeholder={t('form.postCodePlaceholder')}
                        pattern="[0-9]{2}-[0-9]{3}"
                        title={t('form.postCodeValidation')}
                        maxLength={6}
                      />
                    </div>
                    <div>
                      <label className="label-text" htmlFor="city">
                        {t('form.cityLabel')}
                      </label>
                      <input
                        required
                        type="text"
                        id="city"
                        name="city"
                        value={form.city}
                        onChange={handleFormChange}
                        className="input-field"
                        placeholder={t('form.cityPlaceholder')}
                      />
                    </div>
                  </div>
                  <div>
                    <label className="label-text" htmlFor="phoneNumber">
                      {t('form.phoneLabel')}
                    </label>
                    <input
                      required
                      type="tel"
                      id="phoneNumber"
                      name="phoneNumber"
                      value={form.phoneNumber}
                      onChange={handleFormChange}
                      className="input-field"
                      placeholder={t('form.phonePlaceholder')}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Right column — order summary */}
            <div className="mt-6 lg:col-span-2 lg:mt-0">
              <div className="sticky top-6 overflow-hidden rounded-2xl bg-white ring-1 ring-zinc-900/5">
                <div className="p-6">
                  <h2 className="mb-4 font-semibold text-zinc-900">
                    {t('summary.title')}
                  </h2>

                  {cartIsEmpty ? (
                    <p className="mb-6 text-sm text-zinc-400">
                      {t('summary.emptyCart')}
                    </p>
                  ) : (
                    <>
                      <ul className="mb-3 space-y-2.5">
                        {cartItems.map((item) => {
                          const p = products.find(
                            (x) => x.id === item.productId,
                          )
                          if (!p) return null
                          const lineTotal =
                            effectiveUnitPrice(p, period) * item.quantity
                          return (
                            <li
                              key={item.productId}
                              className="flex justify-between gap-2 text-sm"
                            >
                              <span className="min-w-0 text-zinc-600">
                                {p.name}
                                {item.quantity > 1 && (
                                  <span className="text-zinc-400">
                                    {' '}
                                    ×{item.quantity}
                                  </span>
                                )}
                              </span>
                              <span className="shrink-0 font-medium tabular-nums text-zinc-900">
                                {lineTotal.toFixed(2)} {currency}
                              </span>
                            </li>
                          )
                        })}
                      </ul>

                      {isModification && (
                        <div
                          className={`flex justify-between pb-1 text-sm ${credit > 0 ? 'text-green-600' : 'text-zinc-400'}`}
                        >
                          <span>{t('summary.credit')}</span>
                          <span className="shrink-0 tabular-nums">
                            {credit > 0
                              ? `-${credit.toFixed(2)}`
                              : licenceNetAmount
                                ? '0.00'
                                : '—'}{' '}
                            {currency}
                          </span>
                        </div>
                      )}

                      <div className="flex justify-between pb-3 text-xs text-zinc-400">
                        <span>{t('summary.period')}</span>
                        <span>{t(PERIOD_LABEL_KEYS[period])}</span>
                      </div>

                      {vatRate ? (
                        <div className="mb-6 space-y-1.5 border-t border-zinc-100 pt-3">
                          <div className="flex justify-between text-sm text-zinc-500">
                            <span>{t('summary.net')}</span>
                            <span className="tabular-nums">
                              {netAfterCredit.toFixed(2)} {currency}
                            </span>
                          </div>
                          <div className="flex justify-between text-sm text-zinc-500">
                            <span>
                              {t('summary.vatPrefix')}
                              {vatRate}%
                            </span>
                            <span className="tabular-nums">
                              {((netAfterCredit * vatRate) / 100).toFixed(2)}{' '}
                              {currency}
                            </span>
                          </div>
                          <div className="flex items-baseline justify-between border-t border-zinc-100 pt-1.5">
                            <span className="text-sm font-medium text-zinc-700">
                              {t('summary.grossTotal')}
                            </span>
                            <span className="text-2xl font-bold tabular-nums text-zinc-900">
                              {(netAfterCredit * (1 + vatRate / 100)).toFixed(
                                2,
                              )}{' '}
                              <span className="text-base font-semibold text-zinc-400">
                                {currency}
                              </span>
                            </span>
                          </div>
                        </div>
                      ) : (
                        <div className="mb-6 flex items-baseline justify-between border-t border-zinc-100 pt-3">
                          <span className="text-sm text-zinc-700">
                            {t('summary.total')}
                          </span>
                          <span className="text-2xl font-bold tabular-nums text-zinc-900">
                            {netAfterCredit.toFixed(2)}{' '}
                            <span className="text-base font-semibold text-zinc-400">
                              {currency}
                            </span>
                          </span>
                        </div>
                      )}
                    </>
                  )}

                  {(termsUrl || privacyUrl) && (
                    <label className="mb-4 flex cursor-pointer items-start gap-2.5">
                      <input
                        type="checkbox"
                        required
                        className="mt-0.5 h-4 w-4 shrink-0 rounded border-zinc-300"
                        style={{ accentColor: 'var(--color-primary)' }}
                      />
                      <span className="text-xs leading-relaxed text-zinc-500">
                        {t('consent.prefix')}{' '}
                        {termsUrl && (
                          <a
                            href={termsUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="underline hover:text-zinc-700"
                            onClick={(e) => e.stopPropagation()}
                          >
                            {t('consent.terms')}
                          </a>
                        )}
                        {termsUrl && privacyUrl && t('consent.and')}
                        {privacyUrl && (
                          <a
                            href={privacyUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="underline hover:text-zinc-700"
                            onClick={(e) => e.stopPropagation()}
                          >
                            {t('consent.privacy')}
                          </a>
                        )}{' '}
                        {t('consent.suffix')}
                      </span>
                    </label>
                  )}

                  {submitError && (
                    <div className="mb-4 flex items-start gap-2 rounded-lg bg-red-50 px-3 py-2.5 text-xs text-red-700 ring-1 ring-inset ring-red-200">
                      <AlertCircle size={13} className="mt-0.5 shrink-0" />
                      <span>{submitError}</span>
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={cartIsEmpty || orderIsZero || submitting}
                    className="flex w-full items-center justify-center gap-2 rounded-xl py-3 text-sm font-semibold text-white transition disabled:cursor-not-allowed disabled:opacity-50"
                    style={{
                      backgroundColor:
                        cartIsEmpty || orderIsZero
                          ? '#a1a1aa'
                          : 'var(--color-primary)',
                    }}
                  >
                    {submitting ? (
                      <>
                        <Loader2 className="animate-spin" size={15} />{' '}
                        {t('submit.submitting')}
                      </>
                    ) : cartIsEmpty ? (
                      t('submit.selectProducts')
                    ) : orderIsZero ? (
                      t('submit.zeroValue')
                    ) : (
                      `${isModification ? t('submit.modify') : t('submit.buy')} — ${(vatRate ? netAfterCredit * (1 + vatRate / 100) : netAfterCredit).toFixed(2)} ${currency}`
                    )}
                  </button>
                </div>

                <div className="flex items-center gap-2 border-t border-zinc-100 bg-zinc-50 px-6 py-3 text-xs text-zinc-400">
                  <ShieldCheck size={13} className="shrink-0" />
                  {t('submit.secureNote')}
                </div>
              </div>

              {companyName && (
                <div className="mt-4 rounded-2xl bg-white p-5 ring-1 ring-zinc-900/5">
                  <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-zinc-400">
                    {t('seller.title')}
                  </p>
                  <div className="space-y-1 text-sm text-zinc-700">
                    <p className="font-medium text-zinc-900">{companyName}</p>
                    {companyNip && (
                      <p className="text-zinc-500">
                        {t('seller.vatIdPrefix')}
                        {companyNip}
                      </p>
                    )}
                    {companyKrs && (
                      <p className="text-zinc-500">
                        {t('seller.krsPrefix')}
                        {companyKrs}
                      </p>
                    )}
                    {companyAddress && (
                      <p className="text-zinc-500">{companyAddress}</p>
                    )}
                  </div>
                  {(supportEmail || companyPhone) && (
                    <div className="mt-3 space-y-1 border-t border-zinc-100 pt-3 text-sm">
                      {supportEmail && (
                        <a
                          href={`mailto:${supportEmail}`}
                          className="flex items-center gap-2 text-zinc-500 transition hover:text-zinc-800"
                        >
                          <span className="text-zinc-300">✉</span>{' '}
                          {supportEmail}
                        </a>
                      )}
                      {companyPhone && (
                        <a
                          href={`tel:${companyPhone}`}
                          className="flex items-center gap-2 text-zinc-500 transition hover:text-zinc-800"
                        >
                          <span className="text-zinc-300">☎</span>{' '}
                          {companyPhone}
                        </a>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </form>
      </main>

      <footer className="mt-8 border-t border-zinc-900/5 bg-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-2 px-4 py-4 text-xs text-zinc-400 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <span>
            © {new Date().getFullYear()} {title}
            {t('footer.rightsReserved')}
          </span>
          {(termsUrl || privacyUrl) && (
            <div className="flex gap-4">
              {termsUrl && (
                <a
                  href={termsUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="transition-colors hover:text-zinc-700"
                >
                  {t('footer.terms')}
                </a>
              )}
              {privacyUrl && (
                <a
                  href={privacyUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="transition-colors hover:text-zinc-700"
                >
                  {t('footer.privacy')}
                </a>
              )}
            </div>
          )}
        </div>
      </footer>
    </div>
  )
}

export default ShopPage
