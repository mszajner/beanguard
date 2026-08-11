#!/bin/sh
set -e

SERVER_URL="${BEANGUARD_SERVER_URL:-http://localhost:8000}"
ESCAPED_SERVER_URL=$(printf '%s' "$SERVER_URL" | sed "s/'/\\\\'/g")

cat > /usr/share/nginx/html/config.js <<EOF
window._env_ = { serverUrl: '${ESCAPED_SERVER_URL}' };
EOF

exec "$@"
