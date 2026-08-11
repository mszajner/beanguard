# BeanGuard Server

REST API + PostgreSQL. Issues, signs, and encrypts licences; stores licence templates, users, orders, and the cryptographic keys used to sign/encrypt licences. The only place the RSA private key exists.

## Run locally

```bash
docker compose up -d db   # from repo root
mvn spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui.html once running (default port from `application.yml`; the Docker image itself listens on port 80, see `Dockerfile`).

Migrations (Liquibase, `src/main/resources/db/changelog/`) run automatically at startup — no manual schema setup needed. If no admin user exists yet, `ServerInitializationService` also creates one (`admin@beanguard.dev`) with a randomly generated password, logged **once** at startup (`WARN` level) and never stored in plain text. Log in and change it immediately — see [../SECURITY.md](../SECURITY.md).

## Environment variables

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | PostgreSQL connection |
| `BEANGUARD_ADMIN_URL` | Public admin panel URL, used in outbound emails |
| `BEANGUARD_SHOP_URL` | Public shop URL |

The rest of the configuration (cryptographic keys, mail settings, licence demo defaults) lives in the `parameter` database table — see the `ParameterName` enum — and is edited through the admin panel under **Settings**.

## Package structure

See [../CLAUDE.md](../CLAUDE.md) for the full package-by-package breakdown (`controllers`, `services`, `entities`, `repositories`, `mappers`, `ports`, ...).

## License

Business Source License 1.1, converting to Apache License 2.0 four years after each release, with an Additional Use Grant permitting you to run this server to licence your own products. See [../LICENSE.md](../LICENSE.md).
