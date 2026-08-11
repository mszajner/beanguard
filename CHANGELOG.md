# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

## [0.1.0] - 2026-07-14

Initial internal release.

[Unreleased]: https://github.com/mszajner/beanguard/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/mszajner/beanguard/releases/tag/v0.1.0
