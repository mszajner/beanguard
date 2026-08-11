# BeanGuard API

Shared DTOs and validators used to communicate between `beanguard-server` and `beanguard-client`. A plain Java library — no Spring dependency of its own beyond `jakarta.validation`.

## Package structure

- `io.beanguard.api.models.licence` — `Licence`, `LicenceType`, and the create/update/transfer request/response DTOs exchanged with the server's licence endpoints.
- `io.beanguard.api.models.shop` — `Product`, `Order`, `OrderItem`, `OrderStatus`, `ProductType`, and the shop's create/update request DTOs.
- `io.beanguard.api.models.auth` — `AuthRequest`.
- `io.beanguard.api.validators` — custom Bean Validation constraints, e.g. `@PolishNIP` for validating a Polish tax ID (NIP) with its checksum, not just its format.

## What's *not* here

The declarative licence-enforcement annotations (`@RequiresValidLicence`, `@RequiresLicenceFeature`, `@RequiresLicenceLimit`, `@DecreasesLicenceLimit`), their matching exceptions, and the AOP aspects that implement them all live in `beanguard-client` (`io.beanguard.client.annotations` / `io.beanguard.client.exceptions` / `io.beanguard.client.aspects`), not in this module. If you're integrating licence checks into your own app, you want [`beanguard-client`](../beanguard-client), which depends on this module for its DTOs.

## License

Apache License 2.0 — see [../licenses/LICENSE-APACHE-2.0.txt](../licenses/LICENSE-APACHE-2.0.txt).
