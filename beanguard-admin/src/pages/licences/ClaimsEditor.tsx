import { Plus, Trash2 } from 'lucide-react'
import React from 'react'
import { useTranslation } from 'react-i18next'

export interface ClaimEntry {
  key: string
  value: string
}

interface ClaimsEditorProps {
  value: ClaimEntry[]
  onChange: (claims: ClaimEntry[]) => void
}

const ClaimsEditor: React.FC<ClaimsEditorProps> = ({ value, onChange }) => {
  const { t } = useTranslation('licences')
  const add = () => onChange([...value, { key: '', value: '' }])
  const remove = (i: number) => onChange(value.filter((_, idx) => idx !== i))
  const update = (i: number, field: 'key' | 'value', v: string) =>
    onChange(value.map((c, idx) => (idx === i ? { ...c, [field]: v } : c)))

  return (
    <div>
      <p className="label-text mb-2">{t('claimsEditor.heading')}</p>
      {value.length > 0 && (
        <div className="mb-2 overflow-hidden rounded-xl border border-zinc-200">
          <div className="grid grid-cols-[1fr_1fr_32px] gap-2 border-b border-zinc-200 bg-zinc-50 px-3 py-1.5">
            <span className="text-xs font-semibold uppercase tracking-wide text-zinc-500">
              {t('claimsEditor.keyColumn')}
            </span>
            <span className="text-xs font-semibold uppercase tracking-wide text-zinc-500">
              {t('claimsEditor.valueColumn')}
            </span>
            <span />
          </div>
          {value.map((claim, i) => (
            <div
              key={i}
              className="grid grid-cols-[1fr_1fr_32px] items-center gap-2 border-b border-zinc-100 px-3 py-2 last:border-0"
            >
              <input
                type="text"
                value={claim.key}
                onChange={(e) => update(i, 'key', e.target.value)}
                placeholder={t('claimsEditor.keyPlaceholder')}
                className="input-field py-1 text-sm"
                aria-label={t('claimsEditor.keyAriaLabel', { index: i + 1 })}
              />
              <input
                type="text"
                value={claim.value}
                onChange={(e) => update(i, 'value', e.target.value)}
                placeholder={t('claimsEditor.valuePlaceholder')}
                className="input-field py-1 text-sm"
                aria-label={t('claimsEditor.valueAriaLabel', {
                  index: i + 1,
                })}
              />
              <button
                type="button"
                onClick={() => remove(i)}
                className="flex items-center justify-center text-red-400 transition hover:text-red-600"
                aria-label={t('claimsEditor.removeAriaLabel', {
                  index: i + 1,
                })}
              >
                <Trash2 size={14} />
              </button>
            </div>
          ))}
        </div>
      )}
      <button
        type="button"
        onClick={add}
        className="btn-secondary flex items-center gap-1.5 text-sm"
      >
        <Plus size={14} /> {t('claimsEditor.addButton')}
      </button>
    </div>
  )
}

export default ClaimsEditor
