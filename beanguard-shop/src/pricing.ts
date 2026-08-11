import { OrderPeriod, Product } from './types'

export function effectiveUnitPrice(
  product: Product,
  period: OrderPeriod,
): number {
  if (period === 'ONE_YEAR') return product.priceOneYear
  return product.priceOneMonth
}

export function computeCredit(
  netAmount: number | null,
  lastPeriod: string | null,
  expiration: string | null,
): number {
  if (!netAmount || !lastPeriod || !expiration) return 0
  const now = Date.now()
  const exp = new Date(expiration).getTime()
  if (exp <= now) return 0
  const expDate = new Date(expiration)
  const startDate = new Date(expDate)
  if (lastPeriod === 'ONE_YEAR') {
    startDate.setFullYear(startDate.getFullYear() - 1)
  } else {
    startDate.setMonth(startDate.getMonth() - 1)
  }
  const totalDays = Math.round(
    (expDate.getTime() - startDate.getTime()) / 86400000,
  )
  const remainingDays = Math.min(
    totalDays,
    Math.max(0, Math.round((exp - now) / 86400000)),
  )
  if (totalDays <= 0) return 0
  return Math.round(((netAmount * remainingDays) / totalDays) * 100) / 100
}
