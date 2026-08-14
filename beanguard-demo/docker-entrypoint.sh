#!/bin/sh
set -e

# JAVA_OPTS defaults to empty (unset) and is intentionally left unquoted below
# so it word-splits into separate java args (e.g. JAVA_OPTS="-Xmx512m -Xms256m").
exec java -Dserver.port=80 $JAVA_OPTS -jar /app.jar "$@"
