# ADR-005 — Daily route execution and auditable visit corrections

Status: Accepted  
Date: 2026-09-27  
Issue: #7

## Context

The field workflow must be usable offline and require almost no training. The most common action is confirming that a customer's waste was collected. Mistakes must be correctable without losing the earlier result.

The real pilot workbook has not yet been reviewed under #1, so CleanRoute does not currently have validated service-day rules from which to auto-generate a precise daily schedule.

## Decision

### Pilot route construction

For the pilot baseline, the collector explicitly chooses a zone. CleanRoute then creates today's route from the active customers currently assigned to that zone.

Route-day and visit IDs are deterministic from date + zone/customer. Reopening or starting the same zone again is therefore idempotent and does not create duplicate visits.

This is an intentional temporary rule. Once #1 validates actual service-day/cadence data, route population will be filtered by that schedule without changing the route execution UI or visit model.

### One-tap primary outcome

A scheduled stop exposes **Collecté** as the primary row action. Less common outcomes — **Absent** and **Pas de déchets** — remain one level below under secondary actions.

Call and WhatsApp shortcuts are available from the same expanded stop actions when a phone number exists.

### Auditable corrections

collection_visits remains the current state of a stop.

Database version 2 adds collection_visit_revisions, keyed by (visitId, revision). Every genuine transition away from the current outcome stores:
- visit ID;
- revision number;
- resulting status;
- change timestamp.

Repeating the same outcome is a no-op. Correcting COLLECTED to ABSENT, for example, increments the revision and preserves both revisions.

The existing deterministic outbox ID uses visit revision, so each genuine correction also receives a distinct sync operation while retries of the same revision remain safe.

### Schema migration

The database moves from version 1 to version 2 with an explicit MIGRATION_1_2. Destructive migration is not enabled.

A migration smoke test executes the migration against a real SupportSQLite database and verifies the revision table/index are created.

## Consequences

- a zone can be executed fully offline;
- progress is derived directly from local visit state;
- duplicate taps do not create duplicate visits or audit rows;
- corrections remain inspectable later;
- future schedule automation does not require rebuilding the collector UI.

## Deferred

- automatic service-day filtering waits for #1;
- route optimization waits for #11;
- richer customer messaging templates remain in #9;
- remote conflict resolution remains in #16.