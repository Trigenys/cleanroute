# ADR-002 — Domain and offline boundaries

Status: Accepted  
Date: 2026-09-26  
Issue: #2

## Context

CleanRoute must support a full field day without network access while leaving room for later multi-device synchronization. The domain cannot depend on Compose, Room or an HTTP backend if those capabilities are expected to evolve independently.

The current pilot workbook schema is still being validated in #1, so the domain must encode only invariants already required by the product workflow and keep uncertain policy configurable.

## Decision

### Domain layer

The `com.trigenys.cleanroute.domain` package contains plain Kotlin business types only.

It owns:
- stable business identifiers;
- customers, zones and service plans;
- route days and collection visits;
- payment ledger semantics;
- contact-action records;
- synchronization operation contracts;
- repository interfaces.

It does not import Room, Compose, Android framework APIs or a networking client.

### Stable identifiers

Client-generated IDs are first-class values. A future server identifier must never replace the local ID. Remote correlation belongs in persistence/sync metadata so offline records remain stable before, during and after synchronization.

### Collection visit invariants

A visit begins as `SCHEDULED` and may receive one final field outcome:
- `COLLECTED`;
- `ABSENT`;
- `NO_WASTE`.

Recording the same outcome again is idempotent. Correcting an outcome updates the same visit and increments its revision rather than creating a duplicate visit.

The durable audit history of those revisions belongs to the persistence/outbox layer in #4.

### Payment invariants

Payments are immutable ledger facts except for explicit reversal.

- amounts are positive XAF integers;
- payment methods use a string code rather than a closed provider enum;
- partial and advance/over-payments are representable;
- a reversed payment no longer contributes to paid totals;
- outstanding balance never becomes negative.

Whether partial payment or prepayment is allowed operationally remains a configurable product policy until #1 is validated.

### Offline / sync boundary

Persistence in #4 will implement the repository interfaces using Room.

Future remote synchronization will implement `SyncGateway`. Sync operations use stable operation IDs so retries can be acknowledged without creating duplicate business mutations.

No screen calls a remote API directly.

## Consequences

- UI work can begin against domain fixtures before Room exists.
- Room can evolve without changing business entities.
- A backend can be introduced after the single-device pilot without rewriting the core model.
- Import code can map legacy spreadsheet rows into stable domain objects.
- Route optimization can consume route/customer contracts without becoming a domain dependency.

## Deferred decisions

The following remain outside this ADR:
- exact workbook column aliases;
- definitive collection cadence rules;
- exact payment-policy constraints;
- Room schema;
- outbox payload serialization;
- conflict resolution for remote multi-device writes.

Those decisions are intentionally deferred to #1, #4 and #16 rather than guessed here.
