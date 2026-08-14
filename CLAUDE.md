# CLAUDE.md

## Modules

Multi-module Maven project. Root `pom.xml` aggregates all modules.

| Module | Root package | Description |
|--------|-------------|-------------|
| `beanguard-api` | `dev.beanguard.api` | Shared DTOs, annotations (`@RequiresValidLicence`, `@RequiresLicenceLimit`, `@DecreasesLicenceLimit`), exceptions, validators |
| `beanguard-client` | `dev.beanguard.client` | Spring Boot autoconfiguration for client apps |
| `beanguard-server` | `dev.beanguard.server` | Licence server: Spring Boot REST API + PostgreSQL |
| `beanguard-admin` | `dev.beanguard.admin` | Admin panel: Spring Boot serving React SPA (minimal Java) |
| `beanguard-shop` | `dev.beanguard.shop` | Customer shop: Spring Boot serving React SPA (minimal Java) |
| `beanguard-demo` | `dev.beanguard.demo` | Runnable example app showing `beanguard-client` usage (demo-licence form → licence details → refresh/extend) |

Build order across modules: `api → client → server/admin/shop/demo`.

`beanguard-server`, `beanguard-admin`, `beanguard-shop` have `Dockerfile`s — CI/CD builds Docker images for deployment.

## Commands

Maven artifact repo (`dav:https://maven.rexoft.org`) requires credentials in `~/.m2/settings.xml`.

```bash
mvn clean install -DskipTests          # build all, skip tests and frontend build
mvn test                               # requires Docker (Testcontainers PostgreSQL)
mvn test -Dtest=LicenceAcceptanceSpec  # single Spock spec

# Run server locally:
cd beanguard-server
docker-compose up -d
mvn spring-boot:run                    # Swagger: http://localhost:8000/swagger-ui.html

# Frontend dev (inside beanguard-admin, beanguard-shop, or beanguard-docs):
npm install && npm run dev             # admin/shop proxy API calls to a running beanguard-server
```

## Architecture — beanguard-server

Package structure under `dev.beanguard.server`:

```
controllers/
  AuthController, UsersController, LicencesController,
  LicenceOrdersController, TemplatesController, ParametersController
  open/                  ← no-auth endpoints (public shop/licence endpoints)
    OpenLicencesController, OpenShopController
  GlobalExceptionHandler
config/                  ← SecurityConfig, WebConfig
security/                ← TokenRequestFilter (JWT), TokenEncryptor
services/                ← interfaces
  impl/                  ← implementations
    LicenceServiceImpl, LicenceOrderServiceImpl, LicenceTemplateServiceImpl,
    UserServiceImpl, ParameterServiceImpl, ShopTokenServiceImpl,
    LicenceEncryptor, ServerInitializationService
entities/                ← JPA: LicenceEntity, LicenceOrderEntity, LicenceTemplateEntity,
                               UserEntity, ParameterEntity, ShopLicenceTokenEntity
repositories/            ← Spring Data JPA interfaces
mappers/                 ← MapStruct (entity ↔ DTO)
models/                  ← domain value types, enums (ParameterName, Role, OrderStatus)
ports/                   ← infrastructure interfaces (KeyPairProvider, SecretKeyProvider,
                               InstantProvider, SecureStringProvider)
  impl/                  ← adapters implementing ports
exceptions/              ← domain exceptions (LicenceNotFoundException, etc.)
```

**Key constraints:**
- Business logic goes in `services/impl/`, not controllers
- New config parameters → add to `ParameterName` enum; stored in `parameter` DB table via `ParameterService`
- DB schema changes → Liquibase changeset in `src/main/resources/db/changelog/`
- Endpoints requiring no auth → `controllers/open/` subpackage (excluded from JWT filter in `SecurityConfig`)

## Licence Encryption

Licences are doubly-encrypted JWTs:
1. Inner: JWS signed with RSA key pair (RS256)
2. Outer: JWE encrypted with AES-256-GCM secret key

Keys (`LICENCE_PUBLIC_KEY`, `LICENCE_PRIVATE_KEY`, `LICENCE_SECRET_KEY`) and `LICENCE_ISSUER` stored in `parameter` table. Custom licence fields in JSONB `claims` column. `LicenceEncryptor` (server) / `LicenceDecryptorDefault` (client) handle this — rarely needs modification.

## Architecture — beanguard-client

`BeanGuardClientAutoConfiguration` wires:
- `LicenceRegistryDefault` — fetches licence on `ApplicationReadyEvent`, refreshes hourly via `@Scheduled`, publishes `LicenceLoaded` event
- `LicenceDecryptorDefault` — decrypts JWE/JWS using server's public key + secret key
- AOP aspects: `LicenceValidationAspect`, `LicenceLimitValidationAspect`, `LicenceLimitDecreaseAspect`

Client apps must provide a `LicenceProvider` bean returning the encrypted licence string.

## Architecture — beanguard-admin / beanguard-shop

Both modules are thin Spring Boot wrappers around a React SPA. Java code is a single `WebApplication.java`. All logic lives in `src/main/frontend` (React + TypeScript + Tailwind CSS + lucide-react). `frontend-maven-plugin` builds the frontend and bundles it into the JAR at `target/classes/static`.

## Testing

Groovy + Spock. Integration tests extend `IntegrationSpec` (PostgreSQL via Testcontainers, `@DynamicPropertySource`).

Surefire picks up: `**/*Test.*` and `**/*AcceptanceSpec.*` — new test classes must match one of these patterns.

**Before every `git push`, run the full test suite:**

```bash
mvn test
```

Docker must be running (Testcontainers). Never push without a passing test run.

## Releasing

Pushing a version tag (`vX.Y.Z`) triggers `publish` (Docker images to Docker Hub) and `publish-maven` (`beanguard-api`/`beanguard-client` to Maven Central) in `.github/workflows/ci.yml` — both gated to `refs/tags/v*`. Docker images get two tags, no `v` prefix: `X.Y.Z` and `X.Y-latest` (e.g. `v0.1.2` → `0.1.2` and `0.1-latest`).

Before tagging:
1. Bump `<version>` from `X.Y.Z-SNAPSHOT` to `X.Y.Z` in all five `pom.xml` files (root, `beanguard-api`, `beanguard-client`, `beanguard-server`, `beanguard-demo`).
2. Update the hardcoded version references in `beanguard-docs` (PL+EN) so they point at the new `X.Y.Z`:
   - Docker image tags (`docker pull mszajner/beanguard-*:X.Y.Z` and the `docker-compose` snippets) in `download`/`pobierz`, `quick-start`/`szybki-start`, `server`/`serwer`, `shop`/`sklep`, `admin-panel`/`panel-admina`.
   - The example Maven `<dependency>` version in `download`/`pobierz` and `client`/`klient`.
3. Update `CHANGELOG.md`: rename `[Unreleased]` to `[X.Y.Z] - YYYY-MM-DD`, add a fresh empty `[Unreleased]` above it, and update the `[Unreleased]`/`[X.Y.Z]` compare links at the bottom.
4. Run `mvn test`, commit, tag `vX.Y.Z`, push both the commit and the tag.
5. Bump `<version>` to the next `X.Y.(Z+1)-SNAPSHOT` in the same five `pom.xml` files, commit, push.
