# BeanGuard Client

Spring Boot autoconfiguration you add to your own application to enforce licences issued by a `beanguard-server` instance. See [beanguard-docs → Integracja klienta](../beanguard-docs) for the full walkthrough.

## Install

```xml
<dependency>
    <groupId>dev.beanguard</groupId>
    <artifactId>beanguard-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

1. Implement `io.beanguard.client.config.BeanGuardConfiguration` — a single bean providing your server URL + cryptographic keys (`getServerConfig()`), your licence key/secret (`getLicenceKeys()`), and a local cache for the last-received licence (`loadLicence()` / `saveLicence()`).
2. The autoconfiguration picks that bean up automatically and registers `LicenceRegistry`, which fetches the licence at startup and refreshes it hourly.
3. Enforce licence terms declaratively:

```java
@RequiresValidLicence
public void exportReport() { ... }

@RequiresLicenceFeature("advanced-reports")
public void generateAdvancedReport() { ... }

@RequiresLicenceLimit("active-users")
public void createUser(UserRequest request) { ... }

@DecreasesLicenceLimit("active-users")
public void deleteUser(UUID userId) { ... }
```

Limits and feature flags are arbitrary string keys you define yourself, matched against the `claims` map of the licence template you configure in the admin panel.

## Protecting your keys

The values you return from `BeanGuardConfiguration` (server URL, RSA public key, AES secret, your own licence key/secret) must not be readable in your compiled JAR. `beanguard-client` itself ships unobfuscated (it's open source, there's nothing to hide) — see [docs/proguard-vendor-guide.md](../docs/proguard-vendor-guide.md) for how to obfuscate *your own* code with ProGuard to protect the keys you embed.

## Package structure

- `io.beanguard.client.config` — `BeanGuardConfiguration`, `ServerConfig`, `LicenceKeys`.
- `io.beanguard.client.annotations` — the four enforcement annotations.
- `io.beanguard.client.aspects` — the AOP aspects that implement them.
- `io.beanguard.client.exceptions` — `MissingOrInvalidLicence`, `MissingLicenceFeature`, `LicenceLimitExceeded`.
- `io.beanguard.client.registries` — `LicenceRegistry` / `LicenceStatus`, the one supported way to read licence state programmatically (e.g. to show an "expired" banner in your UI).
- `io.beanguard.client.usage` — `UsageRegistry`, tracks per-key usage counters for `@RequiresLicenceLimit`/`@DecreasesLicenceLimit`. Ships with an in-memory default; implement your own to persist counters (e.g. in your own database).

## License

Apache License 2.0 — see [../licenses/LICENSE-APACHE-2.0.txt](../licenses/LICENSE-APACHE-2.0.txt).
