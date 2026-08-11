import clsx from 'clsx'

export function Logo({
  className,
  ...props
}: React.ComponentPropsWithoutRef<'div'>) {
  return (
    <div className={clsx('inline-flex items-center', className)} {...props}>
      {/* eslint-disable-next-line @next/next/no-img-element -- CSS-driven height (h-*), no fixed dimensions for next/image to target */}
      <img src="/logo-full.png" alt="BeanGuard" className="h-full w-auto" />
    </div>
  )
}
