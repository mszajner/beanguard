export interface Product {
  id: string
  enabled: boolean
  type: 'LIMIT' | 'FEATURE'
  claim: string
  priceOneMonth: number
  priceOneYear: number
  name: string
  description: string | null
  required: boolean
  sortOrder: number
}

export interface CartItem {
  productId: string
  quantity: number
}

export interface CreateOrderItemRequest {
  productId: string
  quantity: number
}

export type OrderPeriod = 'ONE_YEAR' | 'ONE_MONTH'

export interface CreateOrderRequest {
  period: OrderPeriod
  licenceId?: string
  vatId: string
  email: string
  companyName?: string
  street?: string
  postCode?: string
  city?: string
  phoneNumber?: string
  items: CreateOrderItemRequest[]
}

export interface ShopLicenceContext {
  licenceId: string
  companyName: string | null
  vatId: string
  email: string
  street: string | null
  postCode: string | null
  city: string | null
  phoneNumber: string | null
  expiration: string | null
  limitClaims: Record<string, number>
  featureClaims: string[]
  licenceNetAmount: number | null
  licenceLastPeriod: string | null
  licenceType: 'DEMO' | 'STANDARD' | null
}
