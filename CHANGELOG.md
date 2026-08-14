# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `beanguard-demo` — a runnable example Spring Boot + Thymeleaf app demonstrating `beanguard-client`: generate a demo licence, view its details, refresh it, deep-link into `beanguard-shop` to extend it, and transfer it to a new machine (email-confirmed, recovers a licence key/secret lost e.g. on a `memory`-profile restart). Licence key/secret persistence is pluggable via the `memory` (default) or `file` Spring profile.
- Client integration docs — an "Example app" page (EN+PL) walking through running `beanguard-demo`, its `memory`/`file` persistence profiles, and what it lets you exercise (issuing, refreshing, extending, and transferring a licence).

### Changed

- **Breaking:** Java packages renamed from `io.beanguard` to `dev.beanguard` across `beanguard-api`, `beanguard-client`, `beanguard-server`, and `beanguard-demo`, aligning them with the Maven `groupId` (already `dev.beanguard`). Update your imports and any `BeanGuardConfiguration`/`ProGuard` rules referencing the old package.

### Fixed

- `LicenceTokenResponse` (`beanguard-api`) was missing a no-args constructor, so generic Jackson deserialization of it failed. Added `@NoArgsConstructor`/`@AllArgsConstructor`, matching the pattern already used by `LicenceDemoCreateRequest`.

## [0.1.2] - 2026-08-12

### Added

- A marketing landing page at `/en` and `/pl` (replacing the previous docs-style homepage), with a hero showcasing the double-encrypted licence token, a problem/audience/how-it-works breakdown, a client integration snippet, and links to Docker Hub, Maven Central, and GitHub.
- A cookie consent banner (shown only when `GA_ID` is configured) that gates Google Analytics behind Google Consent Mode v2 — `analytics_storage` stays denied until the visitor opts in, with English and Polish translations.

### Changed

- Docker images now also get an `X.Y-latest` tag alongside `X.Y.Z` (e.g. `v0.1.2` → `0.1.2` and `0.1-latest`), so vendors can track a minor version without pinning to an exact patch.

### Fixed

- Removed the outdated "images are placeholders" notices from the quick-start docs — the project is genuinely published on Docker Hub now.
- Removed some vulnerabilities in server and docs
- Fixed addresses in docker-compose.yml in docs

## [0.1.1] - 2026-08-11

First successful Maven Central release — `dev.beanguard:beanguard-api:0.1.1` and `beanguard-client:0.1.1` are live (`v0.1.0` uploaded but never got approved, see below).

### Changed

- Docker image tags dropped the `v`-prefixed variant — each release now gets a single `X.Y.Z` tag instead of both `vX.Y.Z` and `X.Y.Z`.

### Fixed

- Maven Central rejected the `v0.1.0` `beanguard-api`/`beanguard-client` deployment: the plugin uploads each module's raw `pom.xml` as written, so the `<url>`, `<scm>`, and `<developers>` inherited from the parent — and dependency versions coming from its `<dependencyManagement>` — weren't actually present in the deployed file. Added `flatten-maven-plugin` (in the `release` profile) to generate a self-contained pom with everything resolved before deploy, and gave `beanguard-api`/`beanguard-client` their own explicit `<url>`/`<scm>` so Maven's default child-path inheritance doesn't turn them into `.../beanguard-client`-style broken URLs.
- `v0.1.1` retried the deploy and the pom passed validation, but the build then crashed polling Central for the deployment's status: `central-publishing-maven-plugin:0.7.0` doesn't know about a `"warnings"` field the Central API now returns and fails to deserialize the response. Bumped the plugin to `0.11.0`.

## [0.1.0] - 2026-08-11

First public release.

### Changed

- Migrated `beanguard-admin`, `beanguard-docs`, and `beanguard-shop` from Spring Boot–wrapped frontends to standalone apps (Next.js standalone server / nginx-served static builds), each with their own Dockerfile.
- Migrated `beanguard-admin` from Create React App to Vite.
- Maven `groupId` changed from `io.beanguard` to `io.github.mszajner` in preparation for Maven Central publishing.
- Maven `groupId` changed again, from `io.github.mszajner` to `dev.beanguard` — a domain-verified namespace instead of a GitHub-verified one, so publishing isn't tied to a personal GitHub login.
- Repository moved from a private GitLab instance to [github.com/mszajner/beanguard](https://github.com/mszajner/beanguard).
- CI publishes Docker images (`server`, `admin`, `shop`, `docs`) to Docker Hub instead of GHCR.
- CI publishes Docker images only on version tags (`v*`) now, instead of on every push to `main` — no more `-SNAPSHOT`-tagged images.
- CI publishes `beanguard-api` and `beanguard-client` to Maven Central on version tags (`v*`).
- `beanguard-client` no longer obfuscates its own JAR with ProGuard — it's open source now, so there was nothing left to hide. The `docs/proguard-vendor-guide.md` guide is rewritten to focus entirely on how *you* protect the keys you embed in your own `BeanGuardConfiguration` implementation.

### Removed

- ProGuard build step, the `beanguard-client:plain` classifier, and the bundled `META-INF/proguard/beanguard-client.pro` consumer rules file from `beanguard-client`.

## 0.0.1 - 2026-07-14

Initial internal release — predates the public repository and was never tagged or published anywhere.

[Unreleased]: https://github.com/mszajner/beanguard/compare/v0.1.2...HEAD
[0.1.2]: https://github.com/mszajner/beanguard/releases/tag/v0.1.2
[0.1.1]: https://github.com/mszajner/beanguard/releases/tag/v0.1.1
[0.1.0]: https://github.com/mszajner/beanguard/releases/tag/v0.1.0
