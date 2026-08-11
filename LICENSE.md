# BeanGuard Licensing

BeanGuard uses an **open-core licensing model**. Different parts of this
repository are licensed under different terms:

| Component | Directory                                                                     | License |
|---|-------------------------------------------------------------------------------|---|
| BeanGuard Client | `beanguard-api/`, `beanguard-client/`                                         | [Apache License 2.0](licenses/LICENSE-APACHE-2.0.txt) |
| BeanGuard Server | `beanguard-admin/`, `beanguard-docs/`, `beanguard-server/`, `beanguard-shop/` | [Business Source License 1.1](licenses/LICENSE-BUSL-1.1.txt) |

## What this means in practice

**You CAN, for free:**

- Embed the BeanGuard client/starter in any application, commercial or not,
  under plain Apache 2.0 — no strings attached.
- Run the BeanGuard server in production to license **your own products**
  (this is explicitly permitted by the Additional Use Grant).
- Read, modify, and redistribute the server source code.

**You CANNOT, without a commercial license:**

- Offer the BeanGuard server to third parties as a hosted or embedded
  commercial license-management service (i.e. resell BeanGuard itself
  as a competing product or SaaS).

**Automatic conversion to open source:** each released version of the server
automatically converts to the Apache License 2.0 four years after its
release date (the "Change Date" mechanism of BUSL 1.1).

## Why BUSL for the server?

BeanGuard is developed and maintained by a solo founder and is used in
production in Mirosław Szajner's own products. The BUSL keeps the project sustainable:
everyone can use it freely to license their own software, while preventing
a third party from taking the server and selling it as a competing hosted
service. This is the same model used by MariaDB, HashiCorp, and others.

If the Additional Use Grant does not cover your use case, contact
mirek@beanguard.dev for a commercial license.

---

*Note: This file is a human-readable summary, not a license itself. The
binding texts are in the `licenses/` directory. "Business Source License"
is a trademark of MariaDB Corporation Ab.*
