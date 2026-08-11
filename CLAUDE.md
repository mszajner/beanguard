# CLAUDE.md

## Modules

Multi-module Maven project. Root `pom.xml` aggregates all modules.

| Module | Root package | Description |
|--------|-------------|-------------|
| `beanguard-api` | `io.beanguard.api` | Shared DTOs, annotations (`@RequiresValidLicence`, `@RequiresLicenceLimit`, `@DecreasesLicenceLimit`), exceptions, validators |
| `beanguard-client` | `io.beanguard.client` | Spring Boot autoconfiguration for client apps |
| `beanguard-server` | `io.beanguard.server` | Licence server: Spring Boot REST API + PostgreSQL |
| `beanguard-admin` | `io.beanguard.admin` | Admin panel: Spring Boot serving React SPA (minimal Java) |
| `beanguard-shop` | `io.beanguard.shop` | Customer shop: Spring Boot serving React SPA (minimal Java) |

Build order across modules: `api → client → server/admin/shop`.

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
mvn spring-boot:run                    # Swagger: http://localhost:8080/swagger-ui.html

# Frontend dev (inside beanguard-admin, beanguard-shop, or beanguard-docs):
npm install && npm run dev             # admin/shop proxy API calls to a running beanguard-server
```

## Architecture — beanguard-server

Package structure under `io.beanguard.server`:

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
