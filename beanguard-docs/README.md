# beanguard-docs

BeanGuard's documentation site — architecture, quick start, per-module install docs, client integration, and licence model. Deployed at [beanguard.dev](https://beanguard.dev).

## Stack

Next.js (App Router) + MDX, Tailwind CSS. Standalone Node server (`output: 'standalone'`), packaged as a self-contained Docker image — no application server framework involved.

## Development

```bash
npm install
npm run dev    # http://localhost:3000
npm run build  # production build (.next/standalone)
```

Each page lives at `src/app/<route>/page.mdx`. Site-wide navigation is the `navigation` array in `src/components/Navigation.tsx`.

## Licensing

This module's own code is licensed under the Business Source License 1.1, per the repository's [LICENSE.md](../LICENSE.md).

Its UI is based on the ["Protocol" documentation template](https://tailwindcss.com/plus/templates/protocol) by Tailwind Labs, used under a [Tailwind Plus Personal License](https://tailwindcss.com/plus/license) — permitted for building an open-source end product with publicly available source, as this repository is. It is not a redistribution of the template itself.
