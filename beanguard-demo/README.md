# beanguard-demo

Runnable example Spring Boot app showing `beanguard-client` in action: fetching a
licence, displaying its details, refreshing it, and deep-linking into
`beanguard-shop` to extend it.

## Prerequisites

A running `beanguard-server` (see the root `CLAUDE.md` "Run server locally" section):

    cd beanguard-server
    docker-compose up -d
    mvn spring-boot:run

From the admin panel (Ustawienia → Klucze kryptograficzne), grab `LICENCE_PUBLIC_KEY`
and `LICENCE_SECRET_KEY`.

## Configuration

Set these before running (env vars, or edit `src/main/resources/application.yml`
directly):

- `BEANGUARD_SERVER_PUBLIC_KEY` — the server's `LICENCE_PUBLIC_KEY`
- `BEANGUARD_SERVER_SECRET_KEY` — the server's `LICENCE_SECRET_KEY`

`beanguard.demo.server.url` defaults to `http://localhost:8000`.

## Persistence profiles

- `memory` (default) — kept in memory only; a restart clears the licence and
  the form reappears.
- `file` — the generated demo licence's key/secret persist to
  `./beanguard-demo-data/`, surviving app restarts.

Switch with `--spring.profiles.active=file` or by editing `application.yml`.

## Run

    mvn -pl beanguard-demo spring-boot:run

Then open http://localhost:8090.
