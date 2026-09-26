# CleanRoute

> Offline-first Android operations for neighborhood waste-collection businesses.

CleanRoute is a Trigenys product experiment designed for operators who currently run customer lists, collection rounds and payments with a phone plus Excel. The product goal is deliberately simple: make the daily field workflow easier than the spreadsheet without trapping the business in a complex ERP.

**Status:** Discovery / foundation  
**Repository:** public during the product-validation phase  
**Delivery board:** https://github.com/orgs/Trigenys/projects/5

## Product loop

```text
Customer → subscription → daily route → collection outcome → payment → reminder/retention
```

The first usable release is gated on a real field workflow, not on feature count.

### MVP scope

- customer directory and client detail;
- zones and daily collection workflow;
- one-tap outcomes: collected, absent, no waste;
- payments and arrears;
- guided Excel migration plus Excel-compatible export;
- offline-first operation and safe retry semantics;
- call and WhatsApp actions;
- a compact owner dashboard.

Mapping, route optimization, remote multi-device sync and referral mechanics are intentionally downstream of the core field loop.

## Android baseline

Provisioned through **Trigenys AppFactory** using the managed `android-compose` blueprint.

- Kotlin + Jetpack Compose + Material 3
- Android Gradle Plugin 9.4.0
- compileSdk 37 / targetSdk 36 / minSdk 26
- Compose previews for phone, tablet and dark mode
- Roborazzi visual regression
- GitHub Actions CI
- AppFactory Project Automation
- RAIDER engineering rules

The target architecture is local-first: domain rules remain independent from Android persistence, Room owns durable device state, and future synchronization is introduced behind explicit boundaries rather than becoming a prerequisite for basic use.

## Reuse-first decisions

CleanRoute follows the RAIDER **Adopt → Adapt → Learn → Build** discipline.

- **Adopt:** Trigenys AppFactory Android foundation.
- **Learn:** ODK Collect for resilient field/offline patterns.
- **Adopt later behind an adapter:** MapLibre Compose for mapping.
- **Evaluate later:** GraphHopper + jsprit for routing/VRP optimization.
- **Do not copy:** unlicensed or mismatched waste-management repositories.

The rationale and license notes live in [ADR-001](docs/architecture/ADR-001-reuse-first.md).

## Delivery

The dependency graph, MVP gate and shared Proof of Done are documented in [the delivery map](docs/product/delivery-map.md).

Every implementation issue is tracked by AppFactory Project Automation with priority, work type, phase and size. The board phases are:

```text
Discovery → Foundation → Offline Core → Field Operations → Payments
→ Retention → Mapping & Routing → Visual QA → Hardening → Pilot → Release
```

## RAIDER

Changes must stay:

- **Reusable** — reusable capability is not buried inside a screen;
- **Agnostic** — no hard-coded customer, quartier, provider or deployment assumption;
- **Idempotent** — imports, retries and mutations are safe to repeat;
- **Durable** — upgrades and migrations preserve existing product work and data;
- **Engineering-grade** — clear boundaries, tests and observable failure modes;
- **Reuse-first** — ecosystem reconnaissance precedes substantial new machinery;
- **Retroactive** — improvements have a safe adoption path for existing installations.

Significant failures and near misses are recorded in `docs/engineering/lessons-learned.md`.

## Build

Use Android Studio or Gradle 9.6+ with JDK 21.

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

## Visual regression

When no golden baseline exists, the visual workflow records candidate screenshots and uploads them as an artifact. Accepted goldens live under `app/src/test/screenshots/`.

```bash
gradle :app:recordRoborazziDebug
gradle :app:verifyRoborazziDebug
```

## Data policy during development

Do not commit real customer names, phone numbers, payment records or exact home locations. Fixtures must be synthetic or explicitly anonymized.
