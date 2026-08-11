#!/bin/sh
set -e

SERVER_URL="${BEANGUARD_SERVER_URL:-http://localhost:8000}"
ESCAPED_SERVER_URL=$(printf '%s' "$SERVER_URL" | sed "s/'/\\\\'/g")

SHOP_URL="${BEANGUARD_SHOP_URL:-http://localhost:8001}"
ESCAPED_SHOP_URL=$(printf '%s' "$SHOP_URL" | sed "s/'/\\\\'/g")

DOCS_URL="${BEANGUARD_DOCS_URL:-http://localhost:8003}"
ESCAPED_DOCS_URL=$(printf '%s' "$DOCS_URL" | sed "s/'/\\\\'/g")

cat > /usr/share/nginx/html/config.js <<EOF
window._env_ = { serverUrl: '${ESCAPED_SERVER_URL}', shopUrl: '${ESCAPED_SHOP_URL}', docsUrl: '${ESCAPED_DOCS_URL}' };
EOF

exec "$@"
