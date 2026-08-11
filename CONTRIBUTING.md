# Contributing to BeanGuard

Thanks for your interest! BeanGuard is maintained by a solo developer and
used in production in my own products, so contributions are genuinely
welcome — and this document is honest about how the process works.

## Where things happen

Development happens on GitHub at
[github.com/mszajner/beanguard](https://github.com/mszajner/beanguard) —
issues, discussions, and pull requests all live there.

## Before you start

- **Bug fixes:** just go ahead — open an issue first only if the fix is
  non-obvious or you want to confirm the diagnosis.
- **Features:** please open a
  [feature request](../../issues/new?template=feature_request.yml) or an
  [Ideas discussion](../../discussions/categories/ideas) **before**
  writing code. BeanGuard has a deliberately conservative scope, and I'd
  hate for you to build something I can't commit to maintaining.
- **Documentation:** typo fixes and clarifications are always welcome,
  no prior discussion needed. The published docs site lives in
  `beanguard-docs/` (`src/app/<route>/page.mdx`) and is deployed to
  [beanguard.dev](https://beanguard.dev). Internal/contributor-facing
  notes (like the ProGuard vendor guide) live in `docs/`.

## Development setup

```bash
git clone https://github.com/mszajner/beanguard.git
cd beanguard
mvn clean install -DskipTests   # build beanguard-api / beanguard-client / beanguard-server
mvn test                        # full test suite - needs Docker running (Testcontainers)

docker compose up -d postgres   # local PostgreSQL for the server
mvn spring-boot:run -pl beanguard-server   # Swagger UI at http://localhost:8080/swagger-ui.html
```

The three frontends (`beanguard-admin`, `beanguard-shop`, `beanguard-docs`)
are standalone apps, not Maven modules — see each one's own `README.md`
for its dev commands (`npm install && npm run dev`).

Requirements: Java 21+, Node 20+, Docker (for `mvn test` and the local
database).

## Pull request guidelines

- Keep PRs focused — one logical change per PR.
- Include tests for behavior changes; `mvn test` must pass (requires Docker
  for Testcontainers).
- Follow the existing code style (standard Java conventions; there's no
  enforced formatter/linter yet, so match the surrounding code by eye).
- Update documentation in `beanguard-docs` if your change affects
  configuration or user-facing behavior.
- Write commit messages in the imperative mood
  ("Add grace period metric", not "Added...").

## Licensing of contributions

BeanGuard is open-core (see [LICENSE.md](LICENSE.md)): client and core
modules are Apache 2.0, the server is BUSL 1.1. By submitting a
contribution, you agree that it is licensed under the license of the
module it touches, and you certify the
[Developer Certificate of Origin](https://developercertificate.org/) —
i.e. that you have the right to submit the code. Sign-off
(`git commit -s`) is appreciated but not required.

## Communication

- Bugs → [Issues](../../issues)
- Questions → [Discussions Q&A](../../discussions/categories/q-a)
- Security → [SECURITY.md](SECURITY.md), never public issues
- Response times: I check issues regularly, but this is a one-person
  project alongside two commercial products — expect days, not hours.
