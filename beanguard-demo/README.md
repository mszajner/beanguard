# beanguard-demo

Runnable example Spring Boot app showing `beanguard-client` in action: fetching a
licence, displaying its details, refreshing it, and deep-linking into
`beanguard-shop` to extend it.

This is a standalone Maven project, not part of the repo's root reactor — it
depends on a published `dev.beanguard:beanguard-client` release from Maven
Central (pinned in `pom.xml`), the same way an external application would,
rather than the in-repo SNAPSHOT.

## Prerequisites

A running `beanguard-server` (see the root `CLAUDE.md` "Run server locally" section):

    cd beanguard-server
    docker-compose up -d
    mvn spring-boot:run

From the admin panel (Ustawienia → Klucze kryptograficzne), grab `LICENCE_PUBLIC_KEY`
and `LICENCE_SECRET_KEY`.

## Configuration

The server URL, public key, and secret key can be set two ways:

- **In the app** — the "Server connection" panel on the main page (collapsed
  by default) lets you paste in all three and hit **Save & reconnect**,
  no restart needed. This is the easiest way to demo the app, but keep in
  mind it's storing a real cryptographic secret (the AES `secretKey`)
  server-side without any extra protection — fine for a local demo, not a
  pattern to copy into a production client app.
- **Before running** (env vars, or edit `src/main/resources/application.yml`
  directly) — takes effect as the default until something's saved via the
  form above, which then takes precedence:
  - `BEANGUARD_SERVER_URL` — defaults to `http://localhost:8000`
  - `BEANGUARD_SERVER_PUBLIC_KEY` — the server's `LICENCE_PUBLIC_KEY`
  - `BEANGUARD_SERVER_SECRET_KEY` — the server's `LICENCE_SECRET_KEY`

## Persistence profiles

- `memory` (default) — kept in memory only; a restart clears the licence, any
  server connection saved via the form, and the demo-licence form reappears.
- `file` — the generated demo licence's key/secret, and any server
  connection saved via the form, persist to `./beanguard-demo-data/`,
  surviving app restarts.

Switch with `--spring.profiles.active=file` or by editing `application.yml`.

## Language

English and Polish, via `messages.properties`/`messages_pl.properties`. Falls
back to the browser's `Accept-Language` header (English if unsupported); the
`EN`/`PL` links in the page header switch explicitly and persist the choice
in a `beanguard-demo-lang` cookie. See `LocaleConfig`.

## Run

    cd beanguard-demo
    mvn spring-boot:run

Then open http://localhost:8090.
