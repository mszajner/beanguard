import clsx from 'clsx'
import { type Metadata } from 'next'

import { Button } from '@/components/Button'
import { MarketingFooter } from '@/components/marketing/MarketingFooter'
import { MarketingHeader } from '@/components/marketing/MarketingHeader'
import { TokenCard } from '@/components/marketing/TokenCard'
import { SiteConfigProvider } from '@/components/SiteConfig'
import { plexMono, plexSans } from '@/lib/fonts'

export const metadata: Metadata = {
  title: 'BeanGuard — Licensing infrastructure for Java & Spring Boot',
  description:
    'BeanGuard issues, encrypts, and verifies licences for Java and Spring Boot applications — self-hosted, source-available, and built to enforce what you sell.',
}

const problems = [
  'Hand-rolled licence keys that are hard to audit, rotate, or revoke.',
  'No self-serve way for a customer to buy and activate a licence.',
  <>
    Feature gating scattered across{' '}
    <code className="font-[family-name:var(--font-plex-mono)] text-[13px]">
      if
    </code>
    -statements instead of enforced in one place.
  </>,
]

const audiences = [
  {
    who: 'Indie developers',
    what: 'Selling a Spring Boot backend, a plugin, or a self-hosted product and tired of writing licensing code instead of product code.',
  },
  {
    who: 'Agencies',
    what: 'Licensing white-label software per client, with different feature sets and limits for each deployment.',
  },
  {
    who: 'B2B vendors',
    what: "Shipping on-prem or self-hosted deployments where you can't rely on a SaaS control plane to enforce anything.",
  },
]

const flow = [
  {
    step: '01',
    title: 'Issue',
    module: 'beanguard-server',
    body: 'Signs and encrypts a licence, stores the keys, exposes the REST API.',
  },
  {
    step: '02',
    title: 'Configure',
    module: 'beanguard-admin',
    body: 'Manage templates, limits, orders, and customers from a panel.',
  },
  {
    step: '03',
    title: 'Sell',
    module: 'beanguard-shop',
    body: 'Customers buy and activate their own licence, no manual step.',
  },
  {
    step: '04',
    title: 'Enforce',
    module: 'beanguard-client',
    body: "Your app checks it with one annotation. That's the whole integration.",
  },
]

const securityPoints = [
  {
    title: 'Signed',
    tag: 'JWS · RS256',
    body: 'Every licence is signed with an RSA key pair — tampering breaks the signature.',
  },
  {
    title: 'Encrypted',
    tag: 'JWE · AES-256-GCM',
    body: "The signed licence is then encrypted, so its claims aren't readable in transit or at rest.",
  },
  {
    title: 'Key stays put',
    tag: null,
    body: "The RSA private key never leaves the server's database. Nothing to leak from a client binary.",
  },
]

export default function Home() {
  const siteUrl = process.env.SITE_URL ?? 'https://beanguard.dev'

  return (
    <SiteConfigProvider siteUrl={siteUrl} locale="en">
      <div
        className={clsx(
          plexSans.variable,
          plexMono.variable,
          'font-[family-name:var(--font-plex-sans)]',
        )}
      >
        <MarketingHeader />

        {/* Hero */}
        <section className="mx-auto max-w-6xl px-6 pt-16 pb-20 sm:pt-20">
          <div className="grid items-center gap-14 lg:grid-cols-[1.05fr_0.95fr]">
            <div>
              <p className="font-[family-name:var(--font-plex-mono)] text-xs font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
                Open source · Apache 2.0 / BSL 1.1
              </p>
              <h1 className="mt-4 text-4xl leading-[1.08] font-semibold tracking-tight text-balance text-zinc-900 sm:text-5xl dark:text-white">
                You don&rsquo;t have to build your own license server.
              </h1>
              <p className="mt-5 max-w-[46ch] text-lg leading-relaxed text-zinc-600 dark:text-zinc-400">
                BeanGuard issues, encrypts, and verifies licences for Java and
                Spring Boot applications — every limit and feature you sell can
                be enforced by a signed, tamper-proof token, instead of a string
                compared in an{' '}
                <code className="font-[family-name:var(--font-plex-mono)] text-base">
                  if
                </code>{' '}
                statement.
              </p>
              <div className="mt-8 flex flex-wrap items-center gap-3">
                <Button href="/en/quick-start" arrow="right">
                  Get started
                </Button>
                <Button href="/en/download" variant="outline">
                  Download BeanGuard
                </Button>
              </div>
              <p className="mt-5 font-[family-name:var(--font-plex-mono)] text-xs text-zinc-400 dark:text-zinc-600">
                $ docker compose up -d · mvn install · dev.beanguard
              </p>
            </div>

            <TokenCard />
          </div>
        </section>

        {/* Problem */}
        <section className="border-t border-zinc-900/7.5 py-20 dark:border-white/7.5">
          <div className="mx-auto max-w-6xl px-6">
            <p className="font-[family-name:var(--font-plex-mono)] text-xs font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
              The problem
            </p>
            <h2 className="mt-3 text-3xl font-semibold text-balance text-zinc-900 dark:text-white">
              Every vendor rebuilds this — and it rarely goes well.
            </h2>

            <div className="mt-10 grid divide-y divide-zinc-900/7.5 overflow-hidden rounded-xl border border-zinc-900/7.5 sm:grid-cols-3 sm:divide-x sm:divide-y-0 dark:divide-white/7.5 dark:border-white/7.5">
              {problems.map((problem, i) => (
                <div key={i} className="p-6">
                  <p className="text-[15px] text-zinc-700 dark:text-zinc-300">
                    {problem}
                  </p>
                </div>
              ))}
            </div>

            <p className="mt-8 text-base text-zinc-700 dark:text-zinc-300">
              BeanGuard can replace all three with infrastructure you run
              yourself —{' '}
              <strong className="font-semibold text-sky-700 dark:text-sky-400">
                self-hosted, source-available, and truly yours.
              </strong>
            </p>
          </div>
        </section>

        {/* Who it's for */}
        <section className="border-t border-zinc-900/7.5 py-20 dark:border-white/7.5">
          <div className="mx-auto max-w-6xl px-6">
            <p className="font-[family-name:var(--font-plex-mono)] text-xs font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
              Who it&rsquo;s for
            </p>
            <h2 className="mt-3 max-w-[22ch] text-3xl font-semibold text-balance text-zinc-900 dark:text-white">
              Built for the vendor, not the enterprise buyer.
            </h2>
            <p className="mt-4 max-w-[58ch] text-base text-zinc-600 dark:text-zinc-400">
              If you sell software with limits — seats, projects, features,
              tiers — BeanGuard can be the layer between &ldquo;we agreed on a
              contract&rdquo; and &ldquo;the app actually enforces it.&rdquo;
            </p>

            <div className="mt-10 divide-y divide-zinc-900/7.5 border-t border-zinc-900/7.5 dark:divide-white/7.5 dark:border-white/7.5">
              {audiences.map((a) => (
                <div
                  key={a.who}
                  className="grid gap-1 py-6 sm:grid-cols-[220px_1fr] sm:gap-6"
                >
                  <div className="text-[15px] font-semibold text-zinc-900 dark:text-white">
                    {a.who}
                  </div>
                  <div className="max-w-[60ch] text-[15px] text-zinc-600 dark:text-zinc-400">
                    {a.what}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* How it works */}
        <section
          id="how"
          className="border-t border-zinc-900/7.5 py-20 dark:border-white/7.5"
        >
          <div className="mx-auto max-w-6xl px-6">
            <p className="font-[family-name:var(--font-plex-mono)] text-xs font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
              How it works
            </p>
            <h2 className="mt-3 text-3xl font-semibold text-balance text-zinc-900 dark:text-white">
              Four modules. One licence, start to finish.
            </h2>

            <div className="mt-12 grid gap-8 sm:grid-cols-2 lg:grid-cols-4 lg:gap-0">
              {flow.map((f, i) => (
                <div
                  key={f.step}
                  className={clsx(
                    'relative pl-6 lg:border-l lg:border-zinc-900/10 lg:pr-6 lg:pl-6 dark:lg:border-white/10',
                    i === 0 && 'lg:border-l-0 lg:pl-0',
                  )}
                >
                  <span className="font-[family-name:var(--font-plex-mono)] text-sm font-semibold text-sky-700 dark:text-sky-400">
                    {f.step}
                  </span>
                  <h3 className="mt-2.5 text-base font-semibold text-zinc-900 dark:text-white">
                    {f.title}
                  </h3>
                  <span className="mt-0.5 block font-[family-name:var(--font-plex-mono)] text-xs text-zinc-400 dark:text-zinc-600">
                    {f.module}
                  </span>
                  <p className="mt-2.5 max-w-[30ch] text-sm text-zinc-600 dark:text-zinc-400">
                    {f.body}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Client library + security */}
        <section
          id="code"
          className="border-t border-zinc-900/7.5 py-20 dark:border-white/7.5"
        >
          <div className="mx-auto max-w-6xl px-6">
            <p className="font-[family-name:var(--font-plex-mono)] text-xs font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
              Client library
            </p>
            <h2 className="mt-3 text-3xl font-semibold text-balance text-zinc-900 dark:text-white">
              One annotation, not a licensing system.
            </h2>

            <div className="mt-10 grid gap-12 lg:grid-cols-[1.1fr_0.9fr]">
              <div>
                <div className="overflow-hidden rounded-2xl bg-zinc-900 shadow-md dark:ring-1 dark:ring-white/10">
                  <div className="flex h-9 items-center border-b border-white/7.5 bg-white/2.5 px-4">
                    <span className="font-[family-name:var(--font-plex-mono)] text-xs text-zinc-400">
                      TeamService.java
                    </span>
                  </div>
                  <pre className="overflow-x-auto p-5 font-[family-name:var(--font-plex-mono)] text-[13.5px] leading-7 text-zinc-200">
                    <span className="text-emerald-300">
                      @RequiresValidLicence
                    </span>
                    {'\n'}
                    <span className="text-emerald-300">
                      @IncreasesLicenceLimit
                    </span>
                    (<span className="text-sky-300">&quot;seats&quot;</span>)
                    {'\n'}
                    <span className="text-emerald-300">
                      @DecreasesLicenceLimit
                    </span>
                    (<span className="text-sky-300">&quot;seats&quot;</span>)
                    {'\n'}
                    <span className="text-sky-400">public</span> User
                    inviteTeamMember(TeamId team) {'{'}
                    {'\n'}
                    {'    '}
                    <span className="text-zinc-500 italic">
                      {'// your code — licence already checked'}
                    </span>
                    {'\n'}
                    {'    '}...{'\n'}
                    {'}'}
                  </pre>
                </div>
                <p className="mt-4 text-sm text-zinc-500 dark:text-zinc-500">
                  <code className="font-[family-name:var(--font-plex-mono)]">
                    beanguard-client
                  </code>{' '}
                  fetches, decrypts, and caches the licence on startup and
                  refreshes it hourly — your code just asks.
                </p>
              </div>

              <div id="security">
                <p className="font-[family-name:var(--font-plex-mono)] text-xs font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
                  Under the hood
                </p>
                <div className="mt-5 grid gap-6">
                  {securityPoints.map((s) => (
                    <div key={s.title} className="flex gap-3.5">
                      <span className="mt-2 h-2 w-2 shrink-0 rounded-full bg-sky-500 dark:bg-sky-400" />
                      <div>
                        <h3 className="text-[15px] font-semibold text-zinc-900 dark:text-white">
                          {s.title}
                          {s.tag && (
                            <span className="ml-2 font-[family-name:var(--font-plex-mono)] text-[11.5px] font-normal text-sky-700 dark:text-sky-400">
                              {s.tag}
                            </span>
                          )}
                        </h3>
                        <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">
                          {s.body}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Trust strip */}
        <section
          id="license"
          className="border-t border-zinc-900/7.5 bg-zinc-50 dark:border-white/7.5 dark:bg-white/2.5"
        >
          <div className="mx-auto max-w-6xl space-y-10 px-6 py-14">
            <div className="grid gap-10 sm:grid-cols-[1.2fr_1fr]">
              <div>
                <div className="font-[family-name:var(--font-plex-mono)] text-[11.5px] font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
                  Open core
                </div>
                <div className="mt-2.5 flex items-baseline gap-2 text-sm">
                  <span className="rounded-md bg-sky-100 px-1.5 py-0.5 font-[family-name:var(--font-plex-mono)] text-[10.5px] text-sky-700 dark:bg-sky-400/10 dark:text-sky-400">
                    Apache 2.0
                  </span>
                  <span className="text-zinc-700 dark:text-zinc-300">
                    beanguard-api, beanguard-client
                  </span>
                </div>
                <div className="mt-1.5 flex items-baseline gap-2 text-sm">
                  <span className="rounded-md bg-sky-100 px-1.5 py-0.5 font-[family-name:var(--font-plex-mono)] text-[10.5px] text-sky-700 dark:bg-sky-400/10 dark:text-sky-400">
                    BSL 1.1
                  </span>
                  <span className="text-zinc-700 dark:text-zinc-300">
                    server, admin, shop, docs
                  </span>
                </div>
                <p className="mt-3 max-w-[46ch] text-[13.5px] text-zinc-500 dark:text-zinc-500">
                  BSL converts to Apache 2.0 four years after each release.
                  Self-hosting to licence your own product is free, permanently.
                </p>
              </div>
              <div>
                <div className="font-[family-name:var(--font-plex-mono)] text-[11.5px] font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
                  Source
                </div>
                <a
                  href="https://github.com/mszajner/beanguard"
                  className="mt-2.5 block font-[family-name:var(--font-plex-mono)] text-sm text-zinc-700 transition hover:text-zinc-900 dark:text-zinc-300 dark:hover:text-white"
                >
                  github.com/mszajner/beanguard
                </a>
              </div>
            </div>

            <div className="grid gap-10 sm:grid-cols-[1.2fr_1fr]">
              <div>
                <div className="font-[family-name:var(--font-plex-mono)] text-[11.5px] font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
                  Maven Central
                </div>
                <a
                  href="https://central.sonatype.com/artifact/dev.beanguard/beanguard-client"
                  className="mt-2.5 block font-[family-name:var(--font-plex-mono)] text-sm text-zinc-700 transition hover:text-zinc-900 dark:text-zinc-300 dark:hover:text-white"
                >
                  dev.beanguard
                </a>
              </div>
              <div>
                <div className="font-[family-name:var(--font-plex-mono)] text-[11.5px] font-medium tracking-wider text-zinc-500 uppercase dark:text-zinc-500">
                  Docker Hub
                </div>
                <a
                  href="https://hub.docker.com/u/mszajner"
                  className="mt-2.5 block font-[family-name:var(--font-plex-mono)] text-sm text-zinc-700 transition hover:text-zinc-900 dark:text-zinc-300 dark:hover:text-white"
                >
                  mszajner/beanguard-*
                </a>
              </div>
            </div>
          </div>
        </section>

        {/* Final CTA */}
        <section className="mx-auto max-w-6xl px-6 py-24">
          <h2 className="max-w-[20ch] text-3xl font-semibold text-balance text-zinc-900 sm:text-4xl dark:text-white">
            You can issue your first licence this afternoon.
          </h2>
          <p className="mt-4 max-w-[50ch] text-base text-zinc-600 dark:text-zinc-400">
            One{' '}
            <code className="font-[family-name:var(--font-plex-mono)]">
              docker compose up
            </code>{' '}
            gets the server, admin panel, and shop running locally, and you can
            add the client library when you&rsquo;re ready to start enforcing
            licences.
          </p>
          <div className="mt-8 flex flex-wrap items-center gap-3">
            <Button href="/en/quick-start" arrow="right">
              Quick start
            </Button>
            <Button href="/en/download" variant="outline">
              Download BeanGuard
            </Button>
          </div>
        </section>

        <MarketingFooter />
      </div>
    </SiteConfigProvider>
  )
}
