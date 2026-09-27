# Security exceptions register

Date: 2026-09-27  
Owner: CleanRoute product engineering  
Related threat model: docs/security/threat-model.md

## EX-001 — Room without application-level encryption

Status: Accepted for MVP pilot.

Risk:
A rooted/unlocked compromised device may expose the Room database.

Rationale:
Android sandboxing and device storage encryption are the current baseline. Introducing database-key creation, backup/recovery and rotation now would add disproportionate operational complexity.

Compensating controls:
- Android backup disabled;
- no automatic cloud sync;
- no banking credentials or identity-document images stored;
- pilot devices require screen lock.

Review trigger:
Wider rollout, shared devices, regulatory requirement, precise GPS storage, or stronger financial identifiers.

## EX-002 — Screenshots are not blocked

Status: Accepted for MVP pilot.

Risk:
A user can capture customer/payment screens and share them outside the application boundary.

Rationale:
Screenshots remain useful for field support during the pilot and current data excludes banking secrets and identity-document images.

Compensating controls:
- staff guidance to avoid unnecessary customer details;
- no automatic screenshot capture/upload by CleanRoute.

Review trigger:
Larger workforce, external contractors, or more sensitive data categories.

## EX-003 — XLSX exports are plain files

Status: Accepted because Excel compatibility is a product requirement.

Risk:
After export, the workbook may be copied or stored in an insecure location.

Rationale:
Standard XLSX interoperability is required to migrate from and fall back to the existing Excel workflow.

Compensating controls:
- export requires an explicit operator action;
- Android document picker controls destination selection;
- CleanRoute keeps no second persistent copy;
- no automatic export/upload exists.

Review trigger:
External sharing, cloud synchronization, regulated datasets, or customer self-service access.
