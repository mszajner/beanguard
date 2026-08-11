# BeanGuard

BeanGuard is a licensing system for Java/Spring Boot applications. It issues, encrypts, and verifies licences — you decide what limits and features each one unlocks in your own product.

Full documentation: **[beanguard-docs](beanguard-docs)** (deployed at [beanguard.dev](https://beanguard.dev) once published).

## Architecture

Four independent modules, each runnable on its own:

| Module | What it is | Stack |
|---|---|---|
| [`beanguard-server`](beanguard-server) | REST API + PostgreSQL. Issues licences, stores cryptographic keys and licence templates. | Spring Boot |
| [`beanguard-admin`](beanguard-admin) | Admin panel — manage users, licence templates, orders, parameters. | React + Vite |
| [`beanguard-shop`](beanguard-shop) | Public storefront — customers self-serve purchase and activate licences. | React + Vite |
| [`beanguard-client`](beanguard-client) | Library you add to your own app to read licence status and enforce limits/features. | Spring Boot |
| [`beanguard-docs`](beanguard-docs) | Documentation site. | Next.js + MDX |
| [`beanguard-api`](beanguard-api) | Shared DTOs and validators used by server and client. | Java library |

Licences are doubly-encrypted JWTs: an inner JWS signed with an RSA key pair (RS256) guarantees the licence hasn't been tampered with, and an outer JWE encrypted with an AES-256-GCM secret protects the content. The RSA private key never leaves the server's database.

`beanguard-admin`, `beanguard-shop`, and `beanguard-docs` are plain static/SPA apps packaged as standalone Docker images (nginx or Next's own server) — no application server framework involved on the frontend side.

## Quick start

```bash
cp .env.example .env   # adjust values if needed
docker compose up -d
```

This starts PostgreSQL plus all four services:

| Service | URL |
|---|---|
| Server (API + Swagger UI at `/swagger-ui.html`) | http://localhost:8000 |
| Shop | http://localhost:8001 |
| Admin | http://localhost:8002 |
| Docs | http://localhost:8003 |

On first boot, `beanguard-server` runs its own database migrations (Liquibase) and, if no admin user exists yet, creates one (`admin@beanguard.dev`) with a **randomly generated password printed once to the server logs** (`docker compose logs server | grep -A5 "No admin user found"`) — it's never stored in plain text anywhere. Log in and change it immediately.

## Integrating BeanGuard into your own app

Add `beanguard-client` as a Maven dependency, implement `BeanGuardConfiguration` with your server URL and keys (generated in the admin panel under **Settings → Cryptographic keys**), and use `@RequiresValidLicence`, `@RequiresLicenceFeature`, `@RequiresLicenceLimit`, and `@DecreasesLicenceLimit` to enforce licence terms declaratively. Full walkthrough, including protecting your embedded keys with ProGuard: **[beanguard-docs → Integracja klienta](beanguard-docs)**.

## Development

Requires Java 21, Node 20+, Docker (for `mvn test`, which uses Testcontainers).

```bash
mvn clean install -DskipTests   # build beanguard-api / beanguard-client / beanguard-server
mvn test                        # full test suite, requires Docker running

cd beanguard-server && docker compose up -d && mvn spring-boot:run   # run the server alone

cd beanguard-admin && npm install && npm run dev   # any frontend, proxies to a running server
```

See [CLAUDE.md](CLAUDE.md) for a deeper tour of the codebase structure and conventions.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before participating.

## Security

See [SECURITY.md](SECURITY.md) for the vulnerability reporting process and supported versions.

## License

BeanGuard uses an open-core model: `beanguard-api`/`beanguard-client` are Apache 2.0; `beanguard-admin`/`beanguard-docs`/`beanguard-server`/`beanguard-shop` are Business Source License 1.1 (converting to Apache 2.0 four years after each release), with an Additional Use Grant that lets you run the server to license your own products for free. See [LICENSE.md](LICENSE.md) for the full breakdown.
