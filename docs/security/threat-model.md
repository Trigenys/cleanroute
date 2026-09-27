# CleanRoute threat model

Status: Active  
Date: 2026-09-27  
Issue: #13

## Security objective

CleanRoute is an offline-first field operations application for neighborhood waste collection. The security goal is to protect customer and business data against accidental disclosure, casual device loss, unsafe exports and future sync mistakes without adding enterprise identity or device-management ceremony before the product needs it.

This document describes the current Android MVP. Any backend or multi-device sync added later must be reviewed against this model before release.

## Sensitive data inventory

CleanRoute currently stores or processes:

- customer names;
- phone numbers;
- zone / neighborhood;
- free-form address labels;
- customer status and service plan;
- collection dates, outcomes and route membership;
- payment amount, service period, method and reversal state;
- customer external/import identifiers;
- contact-action audit records for calls and WhatsApp;
- referral attribution and reward state;
- imported CSV/XLSX source data while parsing;
- exported XLSX files containing customer, payment and collection records.

Location is currently descriptive zone/address data. Precise GPS coordinates are not stored by the MVP.

No card credentials, Mobile Money PINs, banking passwords or WhatsApp message contents are stored.

## Trust boundaries

### Android application sandbox

Room is the system of record for offline operation. Application files and the database are private to the Android application sandbox.

The database is not copied to an app-managed cloud service in the current MVP.

### Android Storage Access Framework

Import and export cross the application boundary through Android document pickers.

Import:
- the operator explicitly selects a source document;
- CleanRoute copies it to an app cache temporary file only for parsing;
- the temporary file is deleted in a finally block;
- the source document itself is never modified.

Export:
- the operator explicitly chooses the destination using Android CreateDocument;
- CleanRoute writes directly to the returned content URI;
- CleanRoute does not retain a second persistent copy;
- after the write completes, the exported file is outside the application security boundary.

### Phone and WhatsApp

Dial and WhatsApp actions intentionally hand customer phone data to the selected external application after an explicit operator action.

CleanRoute does not automatically send messages.

### Future sync boundary

The current domain layer contains no Android credential storage, bearer-token model or HTTP authorization assumptions.

A future sync adapter may authenticate to an API, but credentials must remain in a platform/infrastructure layer and must never be added to Customer, Payment, CollectionVisit, Referral or other domain entities.

## Threats and mitigations

### Lost or stolen device

Threat:
An unlocked device could expose the local customer database, payment history and collection data.

Current mitigations:
- Android application sandbox;
- device-level storage encryption supplied by Android/device hardware;
- Android application backup is disabled with android:allowBackup=false;
- the app does not automatically upload a backup elsewhere.

Operational requirement:
Pilot devices should use a screen lock and the owner's available remote-lock/remote-wipe capability.

Residual risk:
An attacker with an already-unlocked device, rooted device or extracted application sandbox may read local data. See security exceptions.

### Android backup / restore

Threat:
Automatic platform backup could copy the local database to another trust boundary or restore operational data to an unintended device.

Decision:
Application backup is disabled.

The automated security guard verifies the manifest keeps android:allowBackup=false.

### Debug logs and crash output

Threat:
Names, phone numbers, addresses, payment amounts or imported rows can leak into logcat, CI logs or third-party crash tools.

Decision:
Production sources currently have no direct logging API.

The automated guard rejects:
- android.util.Log / Log.*;
- Timber;
- println;
- printStackTrace;
- System.out / System.err.

If observability is added later, it must use a dedicated redacting logger. Logging full Customer, Payment, import rows, exported rows, tokens or request/response bodies remains prohibited.

### Excel export

Threat:
A plain XLSX export contains enough data to identify customers and reconstruct payment/collection activity. It may be copied, emailed or stored in an insecure location after export.

Current behavior:
- export is explicit and operator initiated;
- Android lets the operator choose the destination;
- the workbook contains Clients, Paiements and Collectes sheets;
- CleanRoute writes directly to that destination;
- the exported file is not encrypted by CleanRoute;
- CleanRoute cannot revoke or delete copies after export.

Operational requirement:
Exports should be saved only to a location/account controlled by the business and removed when no longer needed.

### Excel / CSV import

Threat:
A malformed or oversized file could exhaust resources or inject unexpected data.

Current mitigations:
- 20,000-row safety cap;
- bounded detailed row-error reporting;
- header validation;
- temporary app-cache copy;
- guaranteed best-effort temp-file deletion in finally;
- no source-file modification.

Residual risk:
XLSX shared strings are loaded by the narrow OOXML reader and can still consume significant memory for hostile files. Physical-device memory validation is tracked in hardening work.

### Screenshots and screen recording

Threat:
Customer profile, payment and route screens can be captured and shared outside CleanRoute.

Current decision:
The MVP does not set FLAG_SECURE. This is an explicit exception because field support and pilot troubleshooting may require screenshots.

Operators should avoid sharing screenshots containing unnecessary customer details.

This decision must be reviewed before deploying to a larger workforce or introducing more sensitive financial/location data.

### External contact applications

Threat:
When a call or WhatsApp action is opened, the customer phone number and any prefilled message leave the CleanRoute process.

Mitigations:
- explicit operator tap is required;
- no background/automatic sending;
- message templates contain only the data required for the selected business event;
- CleanRoute audits that the external action was opened, but does not store the composed message body.

### Future API credentials

Threat:
Embedding access tokens or authentication semantics in domain models would couple business data to Android credential handling and make accidental persistence/export more likely.

Decision:
Authentication belongs to a future sync/platform adapter.

The automated guard rejects Android dependencies and common bearer/access-token concepts inside the domain source directory.

Expected future controls:
- TLS-only transport;
- short-lived access credentials where supported;
- refresh secrets stored using Android platform-backed secure storage;
- no credentials in Room business entities, exports, logs or screenshots;
- server-side authorization independent of client-provided role claims.

## Security exceptions

The following are conscious MVP tradeoffs, not accidental omissions.

### Room database has no app-level encryption

Decision:
The current Room database is not wrapped in SQLCipher or another application-level database encryption layer.

Rationale:
The pilot relies on Android sandboxing and device storage encryption. Adding database-key lifecycle and recovery before multi-device sync would materially increase operational complexity for limited current benefit.

Review trigger:
Revisit before wider rollout, shared devices, regulatory requirements, or storage of precise coordinates / stronger financial identifiers.

### Screenshots are allowed

Decision:
FLAG_SECURE is not enabled.

Rationale:
Pilot troubleshooting and support benefit from screenshots, while the current app does not store banking credentials or identity-document images.

Review trigger:
Revisit for larger field teams or more sensitive data categories.

### Exported XLSX is not encrypted by CleanRoute

Decision:
Exports remain standard Excel-compatible files.

Rationale:
Excel interoperability is an explicit migration/operational requirement. Encrypting the workbook in-app would make the simple handoff workflow materially harder for the target operator.

Mitigation:
Export is always explicit and destination-selected. No automatic cloud export exists.

## Out of scope for the current MVP

The current MVP does not claim to provide:

- enterprise MDM;
- app-level biometric/PIN lock;
- encrypted Excel packages;
- remote deletion from inside CleanRoute;
- multi-user authorization;
- backend authentication;
- server-side audit retention.

These should be introduced only when the corresponding deployment model exists.

## Review triggers

Re-run this threat model before:

- enabling remote sync;
- storing precise GPS coordinates;
- supporting multiple staff identities or roles;
- adding cloud backup;
- adding crash analytics or remote logs;
- storing identity-document images;
- adding automated messaging;
- expanding beyond a small controlled pilot.
