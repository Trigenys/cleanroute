# ADR-003 — Room persistence and idempotent local outbox

Status: Accepted  
Date: 2026-09-27  
Issue: #4

## Decision

CleanRoute uses AndroidX Room 2.8.5 for the first persisted schema. Room 3 is stable, but this Android-only pilot does not currently benefit enough from the newer API surface to justify adopting a new major persistence line.

Room entities stay under `data/local/entity`; domain models remain plain Kotlin. Mapper functions isolate SQLite representation choices from business rules.

Customer, collection-visit and payment repositories write the business entity and an outbox operation inside one Room transaction. The outbox primary key is deterministic:

```text
<operation-kind>:<aggregate-id>:<business-revision>
```

Replaying the same logical mutation therefore attempts to insert the same outbox key. `OnConflictStrategy.IGNORE` prevents a duplicate business event. A genuine correction changes the business revision and creates a new operation.

Version 1 is the first persisted schema, so there is no historical migration to execute yet. `CleanRouteMigrations.ALL` is the single migration registry used by production opening and tests. Every future version bump must add an explicit migration and extend the persistence tests.

A Robolectric test writes to a file-backed database, closes it, reopens it through the production migration registry, and checks that both customer state and the outbox event survive.

## Consequences

- offline writes do not wait for a backend;
- retries are safe at the outbox boundary;
- persistence remains replaceable behind domain repository interfaces;
- future sync can consume/acknowledge outbox rows without changing domain objects;
- destructive migration is not a default recovery mechanism.

## Follow-up

#5 consumes customer persistence, #7 collection persistence, #8 payment persistence, #16 remote acknowledgement/conflict semantics, and #14 broader restart/migration failure testing.
