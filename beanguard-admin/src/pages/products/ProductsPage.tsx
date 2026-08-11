import {
  closestCenter,
  DndContext,
  DragEndEvent,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical, Loader2, Package, Plus } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import ConfirmDeleteModal from '../../components/ui/ConfirmDeleteModal'
import HelpLink from '../../components/ui/HelpLink'
import { apiFetch } from '../../utils/api'
import AddProductModal from './AddProductModal'
import EditProductModal, { Product } from './EditProductModal'

const TypeBadge: React.FC<{ type: string }> = ({ type }) =>
  type === 'FEATURE' ? (
    <span className="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-600/20">
      FEATURE
    </span>
  ) : (
    <span className="inline-flex items-center rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-medium text-amber-700 ring-1 ring-inset ring-amber-600/20">
      LIMIT
    </span>
  )

interface SortableRowProps {
  product: Product
  onEdit: (p: Product) => void
}

const SortableRow: React.FC<SortableRowProps> = ({ product, onEdit }) => {
  const { t } = useTranslation('products')
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: product.id })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
    background: isDragging ? '#fafafa' : undefined,
  }

  return (
    <tr ref={setNodeRef} style={style}>
      <td>
        <button
          {...attributes}
          {...listeners}
          className="cursor-grab p-1 text-zinc-300 transition-colors hover:text-zinc-500 active:cursor-grabbing"
          aria-label={t('listPage.dragHandleAriaLabel')}
        >
          <GripVertical size={16} />
        </button>
      </td>
      <td
        onClick={() => onEdit(product)}
        className="cursor-pointer hover:bg-zinc-50"
      >
        <div className="flex items-center gap-2">
          <span className="font-medium text-zinc-900">{product.name}</span>
          {product.required && (
            <span className="inline-flex items-center rounded-full bg-red-50 px-2 py-0.5 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-600/20">
              {t('listPage.requiredBadge')}
            </span>
          )}
        </div>
        {product.description && (
          <div className="text-xs text-zinc-400">{product.description}</div>
        )}
      </td>
      <td>
        <TypeBadge type={product.type} />
      </td>
      <td>
        <code className="rounded bg-zinc-100 px-1.5 py-0.5 text-xs">
          {product.claim}
        </code>
      </td>
      <td className="font-medium">
        {Number(product.priceOneMonth).toFixed(2)} PLN
      </td>
      <td className="font-medium">
        {Number(product.priceOneYear).toFixed(2)} PLN
      </td>
      <td>
        <span
          className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
            product.enabled
              ? 'bg-amber-50 text-amber-700 ring-1 ring-inset ring-amber-600/20'
              : 'bg-zinc-100 text-zinc-500 ring-1 ring-inset ring-zinc-400/20'
          }`}
        >
          {product.enabled ? t('listPage.enabledYes') : t('listPage.enabledNo')}
        </span>
      </td>
    </tr>
  )
}

const ProductsPage: React.FC = () => {
  const [data, setData] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [addVisible, setAddVisible] = useState(false)
  const [productToEdit, setProductToEdit] = useState<Product | null>(null)
  const [productToDelete, setProductToDelete] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const { t } = useTranslation('products')

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  )

  const fetchProducts = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await apiFetch('/api/products')
      setData(Array.isArray(result) ? result : [])
    } catch (err) {
      setError(err instanceof Error ? err.message : t('listPage.genericError'))
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    // Fetch-on-mount: idiomatic effect, not a derived-state anti-pattern.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchProducts()
  }, [fetchProducts])

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event
    if (!over || active.id === over.id) return

    const oldIndex = data.findIndex((p) => p.id === active.id)
    const newIndex = data.findIndex((p) => p.id === over.id)
    const reordered = arrayMove(data, oldIndex, newIndex)
    setData(reordered)

    try {
      await apiFetch('/api/products/order', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(reordered.map((p) => p.id)),
      })
    } catch {
      fetchProducts()
    }
  }

  const handleDelete = async (id: string) => {
    setDeleteLoading(true)
    setDeleteError(null)
    try {
      await apiFetch(`/api/products/${id}`, { method: 'DELETE' })
      setData(data.filter((p) => p.id !== id))
      setProductToDelete(null)
    } catch (err) {
      setDeleteError(
        err instanceof Error ? err.message : t('listPage.deleteError'),
      )
    } finally {
      setDeleteLoading(false)
    }
  }

  const colSpan = 7

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-zinc-900">
            <Package size={24} style={{ color: 'var(--color-primary)' }} />
            {t('listPage.heading')}
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            {t('listPage.description')}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <HelpLink path="/panel-admina/produkty" />
          <button
            onClick={() => setAddVisible(true)}
            className="btn-primary flex items-center gap-2"
          >
            <Plus size={16} /> {t('listPage.addButton')}
          </button>
        </div>
      </div>

      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th className="w-8"></th>
              <th>{t('listPage.nameColumn')}</th>
              <th>{t('listPage.typeColumn')}</th>
              <th>{t('listPage.claimColumn')}</th>
              <th>{t('listPage.priceMonthlyColumn')}</th>
              <th>{t('listPage.priceYearlyColumn')}</th>
              <th>{t('listPage.enabledColumn')}</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={colSpan} className="py-12 text-center">
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
                  colSpan={colSpan}
                  className="py-12 text-center text-red-600"
                >
                  {error}
                </td>
              </tr>
            ) : data.length === 0 ? (
              <tr>
                <td
                  colSpan={colSpan}
                  className="py-12 text-center text-zinc-400"
                >
                  {t('listPage.noProducts')}
                </td>
              </tr>
            ) : (
              <DndContext
                sensors={sensors}
                collisionDetection={closestCenter}
                onDragEnd={handleDragEnd}
              >
                <SortableContext
                  items={data.map((p) => p.id)}
                  strategy={verticalListSortingStrategy}
                >
                  {data.map((product) => (
                    <SortableRow
                      key={product.id}
                      product={product}
                      onEdit={setProductToEdit}
                    />
                  ))}
                </SortableContext>
              </DndContext>
            )}
          </tbody>
        </table>
      </div>

      <AddProductModal
        visible={addVisible}
        onCancel={() => setAddVisible(false)}
        onAdd={() => {
          setAddVisible(false)
          fetchProducts()
        }}
      />
      <EditProductModal
        key={productToEdit?.id ?? 'none'}
        product={productToEdit}
        onCancel={() => setProductToEdit(null)}
        onSave={() => {
          setProductToEdit(null)
          fetchProducts()
        }}
        onDelete={() => {
          if (!productToEdit) return
          setProductToDelete(productToEdit.id)
          setProductToEdit(null)
        }}
      />
      {productToDelete !== null && (
        <ConfirmDeleteModal
          title={t('listPage.deleteModalTitle')}
          message={t('listPage.deleteModalMessage')}
          onConfirm={() => productToDelete && handleDelete(productToDelete)}
          onCancel={() => {
            setProductToDelete(null)
            setDeleteError(null)
          }}
          loading={deleteLoading}
          error={deleteError}
        />
      )}
    </div>
  )
}

export default ProductsPage
