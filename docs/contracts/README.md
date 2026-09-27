# CleanRoute sync protocol v1

This folder defines the future remote-sync boundary. No backend is implemented by issue #16.

## Server invariants

- aggregateId is the canonical resource ID and comes from the client.
- operationId is the idempotency key.
- serverRevision is monotonic change metadata, not a replacement resource ID.
- same operationId + same request fingerprint returns the same acknowledgement.
- same operationId + different fingerprint is rejected.
- only applied/duplicate acknowledgements allow Android to remove an outbox item.
- pull cursors advance only after a page is committed locally.
- protocol v1 does not permit client-originated hard deletes.

## Conflict summary

- Customer: optimistic concurrency by baseServerRevision; no silent last-write-wins.
- Collection visit: highest business revision wins; equal revision with different content is conflict.
- Payment: immutable identity/value fields; only RECORDED -> REVERSED is allowed.
- Contact action: append-only.
- Referral: immutable attribution; reward status only moves forward.

See ADR-011 for rationale and the OpenAPI file for the wire shape.
