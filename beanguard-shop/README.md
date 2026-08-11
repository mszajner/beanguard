# BeanGuard Shop

Public storefront where your customers self-serve purchase and activate licences for products you've configured in `beanguard-admin`. Talks only to `beanguard-server`'s unauthenticated `/api/open/shop` endpoints — no login required.

## Stack

React + Vite + Tailwind CSS + React Router. Packaged as a standalone Docker image (multi-stage `node:20-alpine` build → `nginx:alpine` runtime) — no application server framework involved.

## Development

```bash
npm install
npm run dev    # http://localhost:8001, expects a beanguard-server running locally
npm run build  # production build -> build/
```

## Runtime configuration

Same mechanism as `beanguard-admin`: the server URL is read from `window._env_.serverUrl`, populated by `/config.js` at container start from the `BEANGUARD_SERVER_URL` environment variable — not baked in at build time.

```bash
docker run -p 8001:80 -e BEANGUARD_SERVER_URL=https://api.example.com mszajner/beanguard-shop
```

`nginx.conf` handles client-side routing the same way: extension-less paths fall back to `index.html` for React Router, dotted paths still 404 if missing.

## License

Business Source License 1.1 — see [../LICENSE.md](../LICENSE.md).
