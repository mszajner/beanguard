#!/usr/bin/env bash
# Release automation for BeanGuard, following the process documented in CLAUDE.md
# ("Releasing" section):
#
#   1. Bump <version> from X.Y.Z-SNAPSHOT to X.Y.Z in all four pom.xml files.
#   2. Update hardcoded version references in beanguard-docs (PL+EN).
#   3. Update CHANGELOG.md: rename [Unreleased] to [X.Y.Z] - YYYY-MM-DD, add a
#      fresh empty [Unreleased] above it, update the compare links.
#   4. Run mvn test, commit, tag vX.Y.Z.
#   5. Bump <version> to the next X.Y.(Z+1)-SNAPSHOT, commit.
#
# This script does everything up through committing the SNAPSHOT bump, but does
# NOT push — pushing the tag triggers real Docker Hub / Maven Central publishes
# in CI, so review `git log` / `git show` on the two new commits and the new
# tag, then push yourself:
#
#   git push origin main && git push origin vX.Y.Z
#
# Usage: ./release.sh [-y|--yes]
#   -y, --yes   Skip the confirmation prompt before making changes.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

REPO_URL="https://github.com/mszajner/beanguard"
VERSIONS_PLUGIN="org.codehaus.mojo:versions-maven-plugin:2.18.0"

ASSUME_YES=false
for arg in "$@"; do
  case "$arg" in
    -y|--yes) ASSUME_YES=true ;;
    *) echo "Unknown argument: $arg" >&2; exit 1 ;;
  esac
done

DOC_FILES=(
  "beanguard-docs/src/app/en/(docs)/download/page.mdx"
  "beanguard-docs/src/app/en/(docs)/quick-start/page.mdx"
  "beanguard-docs/src/app/en/(docs)/server/page.mdx"
  "beanguard-docs/src/app/en/(docs)/shop/page.mdx"
  "beanguard-docs/src/app/en/(docs)/admin-panel/page.mdx"
  "beanguard-docs/src/app/en/(docs)/client/page.mdx"
  "beanguard-docs/src/app/pl/(docs)/pobierz/page.mdx"
  "beanguard-docs/src/app/pl/(docs)/szybki-start/page.mdx"
  "beanguard-docs/src/app/pl/(docs)/serwer/page.mdx"
  "beanguard-docs/src/app/pl/(docs)/sklep/page.mdx"
  "beanguard-docs/src/app/pl/(docs)/panel-admina/page.mdx"
  "beanguard-docs/src/app/pl/(docs)/klient/page.mdx"
)

log() { printf '\n\033[1;34m==>\033[0m %s\n' "$1"; }
die() { printf '\033[1;31merror:\033[0m %s\n' "$1" >&2; exit 1; }

command -v mvn >/dev/null || die "mvn not found on PATH"
command -v git >/dev/null || die "git not found on PATH"

[[ -z "$(git status --porcelain)" ]] || die "working tree is not clean — commit or stash first:
$(git status --short)"

CURRENT_BRANCH="$(git branch --show-current)"
if [[ "$CURRENT_BRANCH" != "main" ]]; then
  if $ASSUME_YES; then
    die "current branch is '$CURRENT_BRANCH', not 'main' (-y requires being on main)"
  fi
  reply=""
  read -r -p "Current branch is '$CURRENT_BRANCH', not 'main'. Continue anyway? [y/N] " reply || true
  [[ "$reply" =~ ^[Yy]$ ]] || die "aborted"
fi

log "Reading current project version"
CURRENT_VERSION="$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)"
[[ "$CURRENT_VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)-SNAPSHOT$ ]] \
  || die "current version '$CURRENT_VERSION' is not X.Y.Z-SNAPSHOT"

MAJOR="${BASH_REMATCH[1]}"
MINOR="${BASH_REMATCH[2]}"
PATCH="${BASH_REMATCH[3]}"
RELEASE_VERSION="$MAJOR.$MINOR.$PATCH"
NEXT_SNAPSHOT="$MAJOR.$MINOR.$((PATCH + 1))-SNAPSHOT"
TAG="v$RELEASE_VERSION"
TODAY="$(date +%F)"

git rev-parse "$TAG" >/dev/null 2>&1 && die "tag $TAG already exists"

for f in "${DOC_FILES[@]}"; do
  [[ -f "$f" ]] || die "expected doc file missing: $f"
done
grep -q "^## \[Unreleased\]\$" CHANGELOG.md || die "CHANGELOG.md has no '## [Unreleased]' heading"
grep -qE "^\[Unreleased\]: ${REPO_URL}/compare/v[0-9]+\.[0-9]+\.[0-9]+\.\.\.HEAD\$" CHANGELOG.md \
  || die "CHANGELOG.md's [Unreleased] compare link doesn't match the expected format"

echo
echo "  current version : $CURRENT_VERSION"
echo "  release version : $RELEASE_VERSION"
echo "  tag             : $TAG"
echo "  next SNAPSHOT   : $NEXT_SNAPSHOT"
echo
if ! $ASSUME_YES; then
  reply=""
  read -r -p "Proceed with this release? [y/N] " reply || true
  [[ "$reply" =~ ^[Yy]$ ]] || die "aborted"
fi

log "1/6 Bumping pom.xml versions to $RELEASE_VERSION"
mvn -q "$VERSIONS_PLUGIN:set" -DnewVersion="$RELEASE_VERSION" -DprocessAllModules=true -DgenerateBackupPoms=false

log "2/6 Updating beanguard-docs version references (EN+PL) to $RELEASE_VERSION"
for f in "${DOC_FILES[@]}"; do
  sed -i -E \
    -e "s#(mszajner/beanguard-(server|admin|shop):)[0-9]+\.[0-9]+\.[0-9]+#\1${RELEASE_VERSION}#g" \
    -e "s#(<version>)[0-9]+\.[0-9]+\.[0-9]+(</version>)#\1${RELEASE_VERSION}\2#g" \
    "$f"
done

log "3/6 Updating CHANGELOG.md"
awk -v ver="$RELEASE_VERSION" -v date="$TODAY" '
  /^## \[Unreleased\]$/ && !done {
    print "## [Unreleased]"
    print ""
    print "## [" ver "] - " date
    done = 1
    next
  }
  { print }
' CHANGELOG.md > CHANGELOG.md.tmp && mv CHANGELOG.md.tmp CHANGELOG.md

sed -i -E "s#^\[Unreleased\]: ${REPO_URL}/compare/v[0-9]+\.[0-9]+\.[0-9]+\.\.\.HEAD\$#[Unreleased]: ${REPO_URL}/compare/v${RELEASE_VERSION}...HEAD#" CHANGELOG.md
sed -i "/^\[Unreleased\]: /a [${RELEASE_VERSION}]: ${REPO_URL}/releases/tag/v${RELEASE_VERSION}" CHANGELOG.md

log "4/6 Running mvn test (requires Docker for Testcontainers)"
if ! mvn test; then
  die "tests failed on release version $RELEASE_VERSION — working tree left dirty for inspection, fix and re-run. To discard these changes instead: git checkout -- . && git clean -fd beanguard-docs"
fi

log "5/6 Committing release $RELEASE_VERSION and tagging $TAG"
git add pom.xml beanguard-api/pom.xml beanguard-client/pom.xml beanguard-server/pom.xml \
  beanguard-docs CHANGELOG.md
git commit -q -m "release: bump version to $RELEASE_VERSION"
git tag -a "$TAG" -m "$TAG"

log "6/6 Bumping pom.xml versions to next SNAPSHOT $NEXT_SNAPSHOT"
mvn -q "$VERSIONS_PLUGIN:set" -DnewVersion="$NEXT_SNAPSHOT" -DprocessAllModules=true -DgenerateBackupPoms=false
git add pom.xml beanguard-api/pom.xml beanguard-client/pom.xml beanguard-server/pom.xml
git commit -q -m "chore: bump version to $NEXT_SNAPSHOT"

log "Done — nothing has been pushed yet"
cat <<EOF

Review the result:
  git log --oneline -3
  git show $TAG

When ready, push the release:
  git push origin $CURRENT_BRANCH
  git push origin $TAG

Pushing the tag triggers the 'publish' and 'publish-maven' CI jobs
(Docker Hub images + Maven Central deploy for beanguard-api/beanguard-client).
EOF
