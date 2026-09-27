# ADR-011 — Remote sync and multi-device contract

Status: Accepted as contract, backend not implemented  
Date: 2026-09-27  
Issue: #16

## Context

CleanRoute is already useful as a single-device offline-first application. Future owner + collector multi-device use must be additive: enabling sync must not move business rules into Compose, require network access for core work, or replace the current Room-backed domain model.

The existing outbox is therefore treated as the local mutation journal, while this ADR defines the network contract that a future sync adapter and backend must implement.

## Decision

### Local-first remains authoritative for field work

Customer lookup, collection outcomes and payment recording continue to commit locally before any network operation.

Sync is asynchronous and optional. A missing backend, expired credential or unavailable network must not block local writes.

The current MVP does not instantiate a SyncTransport.

### Protocol version

The first wire contract is protocol version 1.

Every push request, pull request and response carries protocolVersion = 1. Breaking wire changes require a new protocol version rather than silent reinterpretation.

### Canonical IDs

CleanRoute client-generated stable IDs remain the canonical resource IDs on the server.

Examples:
- CustomerId;
- CollectionVisitId;
- PaymentId;
- ContactActionId;
- ReferralId.

The server may use internal database surrogate keys, but they are not exposed as replacements for aggregateId.

operationId is a client-generated idempotency key.

serverRevision is a monotonically increasing server change revision. It is version/order metadata, not identity.

deviceId identifies an installation for diagnostics and sync bookkeeping. It is not an authentication credential.

workspaceId identifies the business data scope after a future setup/auth flow. It is infrastructure metadata and does not enter Customer, Payment or other domain entities.

### Push

POST /sync/v1/push accepts at most 100 mutation envelopes.

Each mutation contains:
- operationId;
- aggregateType;
- aggregateId;
- mutation;
- clientRevision;
- optional baseServerRevision;
- occurredAt;
- typed payload for upsert.

The server processes operations independently and returns one acknowledgement per operation.

A structurally valid batch may contain a mix of applied, duplicate, conflict and rejected acknowledgements.

### Idempotency receipts

The server persists an operation receipt keyed by workspaceId + operationId and a request fingerprint.

If the exact operation is retried:
- return the previously recorded acknowledgement;
- do not execute the mutation again.

If the same operationId is reused with different content:
- reject it with reason operation_id_reused;
- never reinterpret it as a new mutation.

The Android outbox may delete an entry only after APPLIED or DUPLICATE.

CONFLICT and REJECTED entries are not silently discarded.

### Pull

GET /sync/v1/pull returns ordered server changes after an opaque cursor.

Each change has a monotonically increasing serverRevision.

The client applies a page transactionally, then persists nextCursor only after the local application succeeds.

If process death happens before the cursor is advanced, replaying the same pull page must be safe.

### Tombstones

Protocol v1 supports server tombstones so deletion/removal can propagate to devices without resurrecting old data.

A tombstone contains:
- aggregateType;
- aggregateId;
- deletedAt;
- serverRevision.

Client-originated DELETE is disabled for protocol v1 for every current aggregate. CleanRoute currently models suspension/reversal/correction instead of destructive deletion.

A future privacy/admin workflow may create a server tombstone. Payments and collection history must never be casually hard-deleted by a field device.

### Conflict policy

#### Customers

Customers use optimistic concurrency.

If no remote record exists: APPLY.

If payload is already identical: DUPLICATE.

If baseServerRevision matches the current server revision: APPLY.

Otherwise: CONFLICT.

There is intentionally no silent last-write-wins rule. A future conflict UI or deterministic merge policy must be explicit.

#### Collection visits

CollectionVisit.revision is the business conflict version.

- incoming revision greater than stored: APPLY;
- lower revision: STALE;
- same revision + same payload: DUPLICATE;
- same revision + different payload: CONFLICT.

This preserves the existing correction/audit model.

#### Payments

Payment identity and immutable fields are:
- id;
- customerId;
- servicePeriod;
- amountXaf;
- methodCode;
- recordedAt.

If those differ for the same payment ID: CONFLICT.

Allowed monotonic state transition:
RECORDED -> REVERSED.

A reversed payment can never be returned to recorded by stale sync.

The same complete payload is DUPLICATE.

#### Contact actions

Contact actions are append-only and identified by stable action ID.

Same ID + same payload is DUPLICATE. Same ID + different payload is CONFLICT.

#### Referrals

Referral identity and referrer/referred relationship are immutable after attribution.

Reward state is monotonic:
PENDING -> ELIGIBLE -> AWARDED.

Older reward state is STALE. Same state + same payload is DUPLICATE.

### Current outbox limitation

The current Room outbox stores operation metadata only:
- operation id;
- operation kind;
- aggregate id;
- occurrence time.

That is sufficient for durability before a backend exists, but it is not yet the complete v1 wire journal.

Before enabling real network sync, a dedicated migration must add the data required to recreate the exact mutation envelope, including:
- clientRevision;
- baseServerRevision;
- deterministic payload snapshot or equivalent immutable serialization.

The network adapter must not simply hydrate old outbox entries from the latest aggregate state, because doing so could change the meaning of an already-issued operationId.

This migration is intentionally deferred until a pilot requires multi-device sync.

## API boundary

The app owns a pure Kotlin SyncTransport port with:
- push(SyncPushRequest);
- pull(SyncPullRequest).

The contract package has no Android, Compose, Retrofit, OkHttp or Ktor client dependency.

Authentication is intentionally outside SyncTransport request models. A future HTTP adapter injects credentials at the transport layer.

## Service choice — Adopt / Adapt / Build

### Adopt

Adopt:
- HTTP/JSON over TLS as the transport shape;
- PostgreSQL as the durable server store when the service is justified;
- existing client-generated UUID/stable IDs and outbox semantics.

### Adapt

Adapt:
- FastAPI as a thin API implementation candidate, because it can implement the OpenAPI-shaped contract without leaking into Android domain code;
- WorkManager later as the Android scheduling mechanism for retry/backoff, behind the sync adapter;
- the existing Room outbox into a complete immutable mutation journal through a schema migration when sync is actually enabled.

No FastAPI, PostgreSQL or WorkManager dependency is added by this issue.

### Build

Build only the CleanRoute-specific sync coordinator:
- operation receipt/idempotency handling;
- serverRevision allocation;
- conflict rules;
- change feed/cursor;
- tombstone persistence;
- workspace authorization boundary.

These semantics are business-specific enough that hiding them behind a generic direct-database mobile connector would make conflicts less explicit.

### Not selected for protocol ownership

Direct Android-to-database or vendor-owned document-sync semantics are not selected as the source of conflict behavior.

A managed database/platform may still host the future service, but the CleanRoute protocol remains the contract and the server remains responsible for authorization and conflict enforcement.

## Retry policy

A future Android sync worker should:
- retry network/5xx failures with bounded exponential backoff;
- resend the same operationId and identical envelope;
- never create a new operationId just because a request timed out;
- stop automatic retries for non-retryable REJECTED operations;
- surface CONFLICT for resolution instead of overwriting;
- pull again after successful push to converge remote changes.

No exact backoff timing is part of protocol v1.

## Contract assets

- docs/contracts/sync-v1.openapi.yaml
- app/src/test/resources/sync/v1/push-request.json
- app/src/test/resources/sync/v1/push-response.json
- app/src/test/resources/sync/v1/pull-response.json
- pure Kotlin conflict/idempotency contract tests.

## Consequences

- backend implementation can be developed independently of Compose;
- existing local workflows remain backend-free;
- client-generated IDs survive migration to multi-device;
- retries are safe by operation receipt;
- payment and visit conflicts cannot silently overwrite newer state;
- hard-delete propagation is possible without enabling destructive field-device deletes;
- real sync still requires an intentional outbox schema migration and pilot justification.
