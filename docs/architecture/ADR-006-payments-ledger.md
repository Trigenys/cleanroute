# ADR-006 — Offline payment ledger, idempotent submission and explicit reversals

Status: Accepted  
Date: 2026-09-27  
Issue: #8

## Context

CleanRoute replaces spreadsheet payment tracking for a business where the same customer can pay in cash, Orange Money or MTN MoMo, including partial payments. Connectivity cannot be required to accept money.

A naive duplicate rule such as customer + month + amount + method would incorrectly reject two legitimate partial payments with the same amount and method.

## Decision

### Payment methods are domain configuration

The default catalog is defined in the domain layer:

- cash — Espèces;
- orange_money — Orange Money;
- mtn_momo — MTN MoMo.

Compose renders the supplied catalog and does not branch on provider names. A future tenant configuration can therefore replace or extend the catalog without rewriting payment UI logic.

### Idempotency is submission-scoped

Opening a payment form creates a client-side submission ID. The persisted payment ID is deterministically derived from that submission ID.

Retries of the same form therefore target the same payment row and return the already-recorded payment.

Opening a new payment form creates a new submission ID, so a second legitimate payment — even with the same customer, month, amount and method — remains valid.

### Reversals, never deletion

A correction changes the existing payment state from RECORDED to REVERSED and stores reversedAt. The row remains in history and export.

Calling reverse again is a no-op and returns the already-reversed payment.

The existing outbox factory derives payment operation IDs from state + relevant timestamp, so recording and reversal are distinct sync operations while retries remain idempotent.

### Arrears

The global arrears query is calculated from:

monthly service-plan amount − sum of RECORDED payments for the selected YearMonth.

REVERSED payments do not reduce the balance. Results include active customers only, support name/phone/zone search and are ordered by amount outstanding.

## Consequences

- payment collection works offline;
- double taps/retries cannot create a second payment;
- legitimate repeated partial payments remain possible;
- corrections preserve audit history;
- customer detail and the global arrears screen share the same ledger rules.
