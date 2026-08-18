import clsx from 'clsx'
import { useTheme } from 'next-themes'
import { useEffect, useState } from 'react'

export function Logo({
  className,
  ...props
}: React.ComponentPropsWithoutRef<'div'>) {
  const { resolvedTheme } = useTheme()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  const src =
    mounted && resolvedTheme === 'dark' ? '/logo-full-dark.png' : '/logo-full.png'

  return (
    <div className={clsx('inline-flex items-center', className)} {...props}>
      {/* eslint-disable-next-line @next/next/no-img-element -- CSS-driven height (h-*), no fixed dimensions for next/image to target */}
      <img src={src} alt="BeanGuard" className="h-full w-auto" />
    </div>
  )
}
