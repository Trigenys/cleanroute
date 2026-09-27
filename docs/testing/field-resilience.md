# Field resilience test matrix

Status: Active  
Date: 2026-09-27  
Issue: #14

## Goal

Prove that CleanRoute keeps working under the conditions that justify an offline-first Android app: no network, process death, retries, database upgrades, low storage and interrupted future sync.

## Matrix

| Scenario | Expected behavior | Evidence | Gate |
| --- | --- | --- | --- |
| Daily collection with no network dependency | A route can be created and completed using only local Room data | `FieldResilienceTest.committedCollectionAndPaymentSurviveRestartAndRetry` exercises customer → route → outcome without any network adapter | Automated |
| Process kill after collection outcome | Committed visit status and audit revision survive DB close/reopen | `FieldResilienceTest` uses a file-backed DB and reopens it before assertions | Automated |
| Process kill after payment | Payment survives DB close/reopen | `FieldResilienceTest` | Automated |
| Duplicate collection tap | Same outcome is a no-op: no extra revision/outbox row | `CollectionWorkflowRepositoryTest` + restart replay in `FieldResilienceTest` | Automated |
| Duplicate payment tap/retry | Same submission ID returns the original payment | `PaymentRepositoryTest` + restart replay in `FieldResilienceTest` | Automated |
| Retry same import | No duplicate customers or outbox operations | `CustomerSpreadsheetServiceTest.csvImportIsPreviewedAndIdempotent` | Automated |
| Schema migration 1 → 2 → 3 | Existing customer/payment/collection records remain unchanged | `PilotDataMigrationPreservationTest` | Automated |
| Interrupted future sync before acknowledgement | Pending outbox rows survive restart | `OutboxRecoveryTest` | Automated |
| Partial acknowledgement then restart | Only explicitly acknowledged outbox rows disappear | `OutboxRecoveryTest` | Automated |
| 5,000-row CSV preview | Representative large pilot import remains usable | `LargeImportTest` | Automated |
| Low storage during import/export | Operation fails without corrupting committed Room data; operator gets a recovery action | actionable messages in `DataTransferRoute`; verify on physical pilot phone | Manual before pilot |
| Device restart | Committed data remains after Android restart | file-backed test covers persistence boundary; repeat once on physical pilot phone | Manual before pilot |
| Airplane mode | Collection, customer lookup and payment entry remain usable | local-only repository tests; repeat complete route on physical pilot phone with airplane mode enabled | Manual before pilot |

## Manual pilot procedure

### Airplane mode

1. Import or create at least two customers while online or offline.
2. Enable airplane mode before opening the daily route.
3. Start a zone route.
4. Record at least one Collecté, one Absent and one Pas de déchets.
5. Record a payment.
6. Leave and reopen the app.
7. Confirm all outcomes and the payment are still present.

Pass condition: no network prompt blocks the workflow and committed records are unchanged.

### Process/device restart

1. Record a collection outcome and payment.
2. Force-stop CleanRoute.
3. Reopen and verify both.
4. Restart the phone.
5. Reopen and verify again.

Pass condition: no committed record is lost or duplicated.

### Low storage

1. Reduce free storage on the pilot device to a deliberately constrained but safe test level.
2. Attempt import and export with representative data.
3. If the operation fails, confirm the UI tells the operator to free space/change destination and retry.
4. Free space and retry.
5. Confirm previously committed customers, visits and payments remain intact.

Pass condition: failure is recoverable and no committed business data changes unexpectedly.

## Recovery-message contract

The UI must tell the operator what to do next instead of surfacing stack traces or raw storage exceptions.

Current import/export recovery actions are:

- verify the source file;
- free device/storage-provider space;
- choose another export destination;
- retry the operation;
- safely retry an interrupted import because deterministic import IDs prevent duplicate records.

## Proof gate

After these resilience tests are enabled, Android CI must pass twice consecutively on the same commit before #14 is merged.
