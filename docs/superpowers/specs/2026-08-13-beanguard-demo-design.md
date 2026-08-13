# beanguard-demo — design spec

Date: 2026-08-13

## Purpose

A new Maven module, `beanguard-demo`, showcasing `beanguard-client` capabilities to
prospective vendors/integrators: fetching a licence, showing its details, refreshing
it on demand, and deep-linking into `beanguard-shop` to extend/change it. Single
Spring Boot MVC controller + Thymeleaf view, run standalone against a local
`beanguard-server`.

## Module & build

- New module `beanguard-demo`, added to root `pom.xml`'s `<modules>` (after
  `beanguard-server`, alongside `api`/`client`/`server` in the reactor).
- `beanguard-demo/pom.xml`: `<parent>` = `dev.beanguard:beanguard:0.1.3-SNAPSHOT`
  (matches other modules). Dependencies: `beanguard-client` (project version),
  `spring-boot-starter-web`, `spring-boot-starter-thymeleaf`,
  `spring-boot-starter-validation`, `lombok` (optional, matching `beanguard-client`'s
  usage).
- Package root: `io.beanguard.demo`.
- No `Dockerfile`. Not referenced by `.github/workflows/ci.yml` — this module is not
  built into a release artifact and is excluded from the Docker/publish pipeline.
- No automated test suite (Spock or otherwise) — this is a runnable example app, not
  product logic. Verified by running it end-to-end against a local `beanguard-server`
  and exercising both flows (generate demo licence, refresh, extend) in a browser.

## Configuration

`beanguard-demo/src/main/resources/application.yml`:

```yaml
server:
  port: 8090

spring:
  application:
    name: beanguard-demo
  profiles:
    active: file   # or: memory

beanguard:
  demo:
    server:
      url: http://localhost:8080
      public-key: ${BEANGUARD_SERVER_PUBLIC_KEY:}
      secret-key: ${BEANGUARD_SERVER_SECRET_KEY:}
    storage-path: ./beanguard-demo-data   # only used by the "file" profile
```

`public-key`/`secret-key` correspond to the `LICENCE_PUBLIC_KEY`/`LICENCE_SECRET_KEY`
parameters visible in the admin panel (Ustawienia → Klucze kryptograficzne) of the
`beanguard-server` instance the demo points at.

Bound via a `@ConfigurationProperties(prefix = "beanguard.demo")` class in
`io.beanguard.demo.config` (`DemoProperties`, with nested `Server` record/class for
`url`/`publicKey`/`secretKey`, plus `storagePath`). This class is local to
`beanguard-demo` — `beanguard-client` itself exposes no `@ConfigurationProperties` (by
design; confirmed via repo grep), so binding YAML→config is the consuming app's job,
same as any real integrator would do.

## Licence key/secret persistence — two profiles

`beanguard-client`'s `BeanGuardConfiguration` interface requires:

```java
ServerConfig getServerConfig();
Optional<LicenceKeys> getLicenceKeys();
Optional<String> loadLicence();
void saveLicence(String licence);
```

`ServerConfig` (server URL + server's RSA public key + AES secret) is fixed
per-deployment and comes straight from `DemoProperties` in both profiles.
`LicenceKeys` (the *specific licence's own* key+secret, obtained only after the user
generates a demo licence) is what needs to persist — or not — across app restarts.
This is the two-profile split the user asked for:

A small interface in `io.beanguard.demo.licence`:

```java
public interface DemoLicenceKeyStore extends BeanGuardConfiguration {
    void storeLicenceKeys(LicenceKeys keys);
}
```

Two `@Component` implementations, mutually exclusive via Spring profiles:

- **`FileBeanGuardConfiguration`** (`@Profile("file")`) — persists `LicenceKeys` to a
  properties file (`<storage-path>/licence-keys.properties`, keys `key`/`secret`)
  under `DemoProperties.storagePath`. `getLicenceKeys()` reads the file if present.
  `storeLicenceKeys(...)` (over)writes it. Survives app restart: on next boot,
  `LicenceRegistryDefault`'s `ApplicationReadyEvent`-triggered `refreshLicence()` finds
  the persisted keys and re-fetches the same licence automatically — the details view
  appears immediately, no form.
- **`InMemoryBeanGuardConfiguration`** (`@Profile("memory")`) — same interface, holds
  `LicenceKeys` in a `volatile` field. Lost on restart — the app comes back up with no
  keys, `refreshLicence()` finds nothing, and the form reappears.

Default active profile is `file` (set in `application.yml`); switch to `memory` via
`--spring.profiles.active=memory` or editing the YAML.

`loadLicence()`/`saveLicence()`: verified by grep that no code in `beanguard-client`
(`BeanGuardServerDefault`, `LicenceRegistryDefault`) currently calls these two
methods — they exist on the interface but are unused by the SDK today. Both demo
implementations still implement them honestly (file profile: read/write a
`<storage-path>/licence.raw` file; memory profile: no-op / `Optional.empty()`) with a
one-line comment noting the SDK doesn't invoke them yet. No behavior is faked around
them.

## Controller

Single `@Controller`, `io.beanguard.demo.web.DemoController`, constructor-injected
with `LicenceRegistry`, `BeanGuardServer`, `DemoLicenceKeyStore`, `DemoProperties`, and
a `RestClient` (Spring's synchronous HTTP client, for the one call not wrapped by
`beanguard-client`: the shop token endpoint).

- **`GET /`** — reads `licenceRegistry.getStatus()` / `getLicence()`. Renders
  `index.html`: if `status == LicenceStatus.LOADED`, the licence-details block;
  otherwise (`NOT_LOADED`, `MISSING_KEY`, `WRONG_KEY`, `WRONG_SECRET`, `EXPIRED`) the
  form block, with a status-specific message (e.g. "brak licencji" vs "licencja
  wygasła"). Also carries any flash error/validation-error attributes from a prior
  POST (post-redirect-get).
- **`POST /demo-licence`** — binds a form DTO `{ @NotBlank @Email String email;
  @NotBlank @PolishNIP String vatId; }` (`@PolishNIP` reused from `beanguard-api`,
  `io.beanguard.api.validators`). On validation failure: redirect to `/` with flash
  field errors. On success: calls
  `beanGuardServer.createDemoLicence(new LicenceDemoCreateRequest(email, vatId))`,
  extracts `key`/`secret` from the returned `Licence`, calls
  `demoLicenceKeyStore.storeLicenceKeys(new LicenceKeys(key.toString(), secret))`,
  then calls `licenceRegistry.refreshLicence()` (reuses the SDK's normal fetch/decrypt/
  publish path rather than hand-rolling state) and redirects to `/`. Any
  `BeanGuardServerException` is caught and surfaced as a flash error banner.
- **`POST /refresh`** — calls `licenceRegistry.refreshLicence()`, redirects to `/`.
- **`GET /extend`** — requires a currently loaded licence (`licenceRegistry.getLicence()`
  non-null; if absent, redirect to `/` with an error). Builds
  `Authorization: KeySecret base64(key:secret)` from the current licence's own
  key/secret, `POST`s to `{server.url}/api/open/licences/token` via `RestClient`,
  deserializes the response into `io.beanguard.api.models.shop.LicenceTokenResponse`
  (shared DTO, already defined in `beanguard-api`), and issues
  `redirect:<shopUrl>` — a real HTTP 302. The "Przedłuż licencję" button in the view is
  a plain `<a href="/extend" target="_blank">`, so the browser opens a new tab, hits
  this endpoint, and follows the redirect into `beanguard-shop` in that same new tab.
  On any HTTP/connection error, redirect to `/` with a flash error banner instead.

## View

`beanguard-demo/src/main/resources/templates/index.html` — one Thymeleaf template,
small embedded `<style>` block (no external CDN/framework, so the demo stays runnable
without internet access), two mutually-exclusive `th:if`/`th:unless` sections:

- **Form section** (shown when licence isn't loaded): status message, `email` +
  `vatId` (NIP) inputs with inline validation error display, submit button
  "Wygeneruj licencję demo" (`POST /demo-licence`).
- **Details section** (shown when `LOADED`): licence key, company name, address
  (street/postCode/city), VAT ID, email, phone, licence type, net amount, formatted
  expiration date, and the `claims` map rendered as a simple key/value list
  (`th:each`). Below: "Przedłuż licencję" link (`GET /extend`, `target="_blank"`) and
  "Odśwież licencję" button (`POST /refresh`).
- A shared flash-message area (validation errors / server-call errors) rendered at the
  top of the page regardless of which section is active.

## Data flow (end-to-end)

1. Boot → `LicenceRegistryDefault.refreshLicence()` fires on `ApplicationReadyEvent` →
   `getLicenceKeys()` empty (first run, or `memory` profile after restart) →
   `BeanGuardServer.getLicence()` → `Optional.empty()` → status `NOT_LOADED` →
   `GET /` renders the form.
2. User submits email + NIP → `POST /demo-licence` → `createDemoLicence(...)` → keys
   stored via the active `DemoLicenceKeyStore` → `refreshLicence()` re-fetches through
   the normal path → redirect to `/` → status `LOADED` → details view.
3. "Odśwież licencję" → `POST /refresh` → `refreshLicence()` → redirect to `/` →
   view reflects the latest server-side state (e.g. new expiration after a shop
   purchase).
4. "Przedłuż licencję" → new tab → `GET /extend` → token call to
   `beanguard-server` → 302 into `beanguard-shop` (own tab, token valid 15 minutes,
   single use). User buys/extends there, then returns to the still-open demo tab and
   clicks "Odśwież licencję".
5. Restart with `file` profile → keys reloaded from disk → licence re-fetched
   automatically at startup → details view immediately, no form. Restart with
   `memory` profile → keys lost → form reappears, user must generate a new demo
   licence.

## Error handling

- Bean Validation errors (`@Email`, `@PolishNIP`) on the generate-demo-licence form:
  shown inline next to the relevant field via flash attributes + PRG redirect.
- `BeanGuardServerException` from `createDemoLicence`, `refreshLicence`'s underlying
  call, or the `/extend` token call: caught in the controller, surfaced as a flash
  error banner ("Nie udało się połączyć z serwerem licencji: …"). No stack traces
  shown to the user.
- No additional startup-time validation of `application.yml` values (e.g. blank
  server URL) — left as a runtime `BeanGuardServerException` on first request, since
  this is a demo, not production code, and a `README.md` in the module documents the
  required properties.

## Out of scope

- Automated tests (explicitly decided against — runnable example, verified manually).
- Docker packaging / CI publishing.
- Any UI framework/CDN dependency — plain embedded CSS only.
- Handling `beanguard-shop`'s own purchase/checkout flow — that's `beanguard-shop`'s
  existing responsibility; the demo only deep-links into it.
