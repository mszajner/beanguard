export function TokenCard() {
  return (
    <div className="overflow-hidden rounded-2xl bg-zinc-900 shadow-xl ring-1 shadow-zinc-900/10 ring-white/10 dark:shadow-none">
      <div className="flex h-9 items-center justify-between border-b border-white/7.5 bg-white/2.5 px-4">
        <span className="font-[family-name:var(--font-plex-mono)] text-xs text-zinc-400">
          beanguard-server · POST /licences/issue
        </span>
        <div className="flex gap-1.5">
          <span className="h-2 w-2 rounded-full bg-white/15" />
          <span className="h-2 w-2 rounded-full bg-white/15" />
          <span className="h-2 w-2 rounded-full bg-white/15" />
        </div>
      </div>

      <div className="p-5 font-[family-name:var(--font-plex-mono)] text-[13px] leading-7">
        <p className="break-all text-zinc-500">
          eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIn0.k3Jd9Qm2vB7…Zx8pL4wN.9fq2X_rT.a1F7cE0kQmZs…v6Yb3Lp9Rw2Tn
        </p>

        <div className="my-4 flex items-center gap-3 text-[11px] tracking-wide text-emerald-400 uppercase">
          <span>✓ signature verified · RS256</span>
          <span className="h-px flex-1 bg-linear-to-r from-emerald-400/40 to-transparent" />
        </div>

        <p className="text-zinc-200">
          {'{'}
          <br />
          &nbsp;&nbsp;<span className="text-sky-300">&quot;type&quot;</span>
          <span className="text-zinc-500">:</span>{' '}
          <span className="text-emerald-300">&quot;STANDARD&quot;</span>,
          <br />
          &nbsp;&nbsp;
          <span className="text-sky-300">&quot;expiration&quot;</span>
          <span className="text-zinc-500">:</span>{' '}
          <span className="text-emerald-300">&quot;2027-01-01&quot;</span>,
          <br />
          &nbsp;&nbsp;<span className="text-sky-300">&quot;claims&quot;</span>
          <span className="text-zinc-500">:</span> {'{'}
          <br />
          &nbsp;&nbsp;&nbsp;&nbsp;
          <span className="text-sky-300">&quot;seats&quot;</span>
          <span className="text-zinc-500">:</span>{' '}
          <span className="text-emerald-300">&quot;25&quot;</span>,
          <br />
          &nbsp;&nbsp;&nbsp;&nbsp;
          <span className="text-sky-300">&quot;advanced-reports&quot;</span>
          <span className="text-zinc-500">:</span>{' '}
          <span className="text-emerald-300">&quot;true&quot;</span>
          <br />
          &nbsp;&nbsp;{'}'}
          <br />
          {'}'}
        </p>
      </div>
    </div>
  )
}
