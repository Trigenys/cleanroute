# ADR-001 — Reuse-first Android foundation

Status: Accepted  
Date: 2026-09-26

## Context

CleanRoute targets neighborhood waste-collection businesses whose operators are primarily comfortable with Android phones and Excel. The first product must remain usable with unreliable connectivity, very little training and low-cost devices while leaving room for multiple collectors, payments, customer retention, mapping and route optimization.

The main engineering risk is not the dashboard. It is creating a field workflow that remains dependable when connectivity is poor and can later synchronize without rewriting the domain model.

## Decision

### Adopt — Trigenys AppFactory Android blueprint

The repository is provisioned from the AppFactory `android-compose` blueprint v3.

We keep:
- Kotlin + Jetpack Compose + Material 3;
- AppFactory Project Automation;
- Roborazzi visual regression;
- GitHub Actions CI;
- RAIDER engineering rules;
- the AppFactory managed-upgrade marker.

This is the product baseline. We do not fork another application as the foundation.

### Build — CleanRoute domain and offline core

The CleanRoute domain is product-specific and will be implemented locally:
- Customer;
- Zone;
- ServicePlan;
- CollectionVisit;
- Payment;
- RouteDay;
- ContactAction;
- SyncOperation.

The Android client will be local-first. Room owns durable local state and an explicit outbox/event boundary will make future remote synchronization additive rather than a rewrite.

### Learn — ODK Collect

ODK Collect is Apache-2.0 and is proven in resource-constrained Android deployments with unreliable connectivity.

We will study its failure-handling, local persistence, retry and field-operation patterns, but we will **not** fork or embed ODK Collect. Its form-engine scope and architecture are much broader than CleanRoute's focused workflow.

Reference: https://github.com/getodk/collect

### Adopt later — MapLibre Compose behind an adapter

MapLibre Compose is a viable mapping layer for the customer/zone map. Its Android API is currently beta, so CleanRoute must depend on an internal mapping interface rather than leak MapLibre types into domain or feature modules.

Reference: https://github.com/maplibre/maplibre-compose

### Learn / Adapt later — GraphHopper + jsprit

Route optimization is intentionally outside the first vertical slice.

GraphHopper provides road routing and jsprit provides Apache-2.0 vehicle-routing/TSP algorithms. A later spike will validate whether this combination is operationally useful for dense neighborhood collection before it becomes a production dependency.

References:
- https://github.com/graphhopper/graphhopper
- https://github.com/graphhopper/jsprit

### Reject — direct reuse of WasteWise code

The reviewed WasteWise repository resembles the domain, but its public repository does not expose a reusable license and its implementation/product scope does not match the Compose + offline-first baseline.

We may learn from domain terminology only. We will not copy code.

Reference: https://github.com/ANAS-Y/WasteWise

## Backend boundary

The first vertical slice does not require a remote backend to prove the field workflow. The local data model and repositories must nevertheless expose sync-safe identifiers and timestamps.

Multi-device synchronization will be introduced behind a `SyncGateway` contract after the single-device pilot flow is stable. This avoids making network availability a prerequisite for the earliest usable build.

## RAIDER consequences

- **Reusable** — generic offline, mapping and sync boundaries live behind interfaces rather than screen-specific code.
- **Agnostic** — no business rule may depend on a specific quartier, phone number, payment provider or backend URL.
- **Idempotent** — imports, sync operations and repeated collection/payment mutations must have stable identifiers and safe retry semantics.
- **Durable** — migrations are additive, local data survives application restarts/upgrades and AppFactory upgrades remain supported.
- **Engineering-grade** — domain, persistence, UI and external integrations stay separated and testable.
- **Reuse-first** — ecosystem options are explicitly evaluated before adding significant capabilities.
- **Retroactive** — Excel migration and future sync are first-class brownfield paths.

## Revisit when

Revisit this ADR if:
- the pilot requires simultaneous multi-device writes before the local-first slice is validated;
- direct XLSX support proves impractical on target Android devices;
- MapLibre Compose stability or packaging constraints become unacceptable;
- route density proves too low for optimization to create measurable value.
