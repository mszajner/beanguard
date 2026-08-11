# Security Policy

BeanGuard is a license server — cryptographic signing and validation are
its core. Security reports are taken seriously and handled with priority.

## Reporting a vulnerability

**Please do not report security vulnerabilities through public GitHub
issues, discussions, or pull requests.**

Instead, use one of these private channels:

1. **GitHub private vulnerability reporting** (preferred):
   [Report a vulnerability](../../security/advisories/new) — this creates a
   private advisory visible only to you and the maintainer.
2. **Email:** mirek@beanguard.dev

Please include:

- The affected component (server / client starter / core) and version
- A description of the vulnerability and its impact
- Steps to reproduce or a proof of concept, if possible
- Any suggested mitigation, if you have one

## What to expect

BeanGuard is maintained by a solo developer, so I'll be honest about
timelines rather than promise a corporate SLA:

- **Acknowledgement** of your report within **72 hours**
- An initial assessment (accepted / needs info / not a vulnerability)
  within **7 days**
- For confirmed vulnerabilities: a fix or documented mitigation as fast
  as severity demands, coordinated disclosure once a patched release is
  available, and credit to you in the release notes (unless you prefer
  to stay anonymous)

## Scope notes

The following are **not** considered vulnerabilities, by design:

- **Bypassing license checks by modifying the client or the licensed
  application's JVM.** The client runs in an environment the end customer
  controls; BeanGuard is a compliance tool, not DRM (see README). Reports
  of "I patched out the check" are expected behavior.
- Vulnerabilities in your own deployment configuration (e.g. running the
  admin UI without TLS, not changing the initial admin password).

## Initial admin credentials

`beanguard-server` does not ship with a fixed default password. On first
startup, if no admin user exists yet, it creates one (`admin@beanguard.dev`)
with a cryptographically random password, printed **once** to the server
logs at `WARN` level and never persisted in plain text anywhere. Change it
immediately after your first login.

The following very much **are** in scope — examples:

- License signature forgery or verification bypass **without** modifying
  the client
- Authentication/authorization flaws in the server admin UI or REST API
- Injection vulnerabilities, insecure deserialization, SSRF in the server
- Key material leakage (private signing keys exposed via API, logs, or
  error messages)

## Supported versions

Security fixes are applied to the latest minor release line. Older
versions: fixes are not backported; please upgrade.

| Version | Supported |
|---|---|
| 0.x (latest) | ✅ |
