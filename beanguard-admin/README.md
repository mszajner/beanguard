# BeanGuard Admin

Admin panel for `beanguard-server` — users, licence templates, licences, orders, and parameters (including the cryptographic keys used to sign/encrypt licences).

## Stack

React + Vite + Tailwind CSS + React Router. Packaged as a standalone Docker image (multi-stage `node:20-alpine` build → `nginx:alpine` runtime) — no application server framework involved.

## Development

```bash
npm install
npm run dev    # http://localhost:8002, expects a beanguard-server running locally
npm run build  # production build -> build/
```

## Runtime configuration

The server, shop and docs URLs are **not** baked in at build time. The Docker image's `docker-entrypoint.sh` renders `/config.js` from the `BEANGUARD_SERVER_URL`, `BEANGUARD_SHOP_URL` and `BEANGUARD_DOCS_URL` environment variables at container start:

```bash
docker run -p 8002:80 \
  -e BEANGUARD_SERVER_URL=https://api.example.com \
  -e BEANGUARD_SHOP_URL=https://shop.example.com \
  -e BEANGUARD_DOCS_URL=https://docs.example.com \
  mszajner/beanguard-admin
```

`src/utils/api.ts` reads them at runtime via `window._env_.serverUrl` / `window._env_.shopUrl` / `window._env_.docsUrl`. `BEANGUARD_DOCS_URL` is optional and defaults to `http://localhost:8003` if unset. This means the same image can point at any server/shop/docs without rebuilding.

`nginx.conf` also handles client-side routing: extension-less paths fall back to `index.html` for React Router, while paths with a dot still 404 if the file doesn't exist (so a typo'd asset URL doesn't silently serve the app shell).

## License

Business Source License 1.1 — see [../LICENSE.md](../LICENSE.md).
