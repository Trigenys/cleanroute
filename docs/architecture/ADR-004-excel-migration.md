# ADR-004 — Excel migration without a heavyweight Android spreadsheet runtime

Status: Accepted  
Date: 2026-09-27  
Issue: #6

## Context

The pilot business currently treats Excel as its operational source of truth. CleanRoute must therefore import an existing customer workbook without making adoption depend on manual re-entry, while still providing an export that the owner can open in Excel if the app is unavailable.

The target is Android API 26+ on ordinary phones. A spreadsheet dependency must therefore be judged on Android compatibility, binary size, memory behavior, licensing and how much of the Excel feature set CleanRoute actually needs.

## Reuse-first evaluation

### Apache POI / Android ports — Learn, do not adopt

Apache POI is the dominant Java spreadsheet library, but it is not an Android-first runtime. Android ports/shaded builds exist, yet they add a large dependency surface and platform-specific workarounds for a feature set far beyond CleanRoute's needs.

Reference: https://github.com/SUPERCILEX/poi-android

### DroidXLS — Learn, defer commercial adoption

DroidXLS is Android-native and supports streaming XLSX access, but its commercial use requires a paid license. That may be reasonable later if advanced spreadsheet compatibility becomes a product requirement, but it is not justified for the first pilot.

Reference: https://github.com/youichi-uda/droidxls

### Decision — small OOXML boundary

CleanRoute implements a deliberately small XLSX boundary using Android XML pull parsing and the platform ZIP APIs.

Import supports:
- `.xlsx` first worksheet;
- `.csv` fallback with comma or semicolon delimiter;
- shared-string, inline-string, numeric, boolean and cached formula values needed for ordinary customer tables;
- header-based field matching rather than fixed column positions.

Export writes a standards-based `.xlsx` workbook with three worksheets:
- `Clients`;
- `Paiements`;
- `Collectes`.

The implementation does **not** attempt to become a general spreadsheet engine. It intentionally does not support macros, charts, styles, encrypted workbooks or legacy `.xls`.

## Import contract

Canonical customer fields currently supported:

| Field | Required | Example aliases |
| --- | --- | --- |
| `external_id` | no | id, code_client, customer_id |
| `name` | yes | nom, client, nom_client |
| `phone` | no | telephone, tel, mobile |
| `zone` | yes | quartier, secteur |
| `address_label` | no | adresse, repere |
| `collection_frequency` | no | frequence, periodicite |
| `monthly_fee_xaf` | yes | montant, abonnement, tarif |
| `status` | no | statut, etat |

Unknown columns are surfaced in the preview and are never silently treated as mapped data.

## Idempotency

An imported row receives a stable source identifier:

1. use the workbook's explicit external/customer ID when present;
2. otherwise derive a SHA-256 fingerprint from normalized name, phone, zone and address.

The corresponding CleanRoute customer ID is deterministic. Re-importing the same row therefore targets the same customer.

Before creating an update, CleanRoute compares the stored customer, zone and plan values. If the row is unchanged, it is counted as `UNCHANGED` and no new outbox event is created.

Applying the **same preview plan twice** also remains safe because the underlying outbox operation ID is deterministic.

## Memory and device safety

The file is streamed from Android's Storage Access Framework to a cache file rather than retained as a byte array.

- CSV rows are parsed as a character stream.
- XLSX content uses `ZipFile` random access so the worksheet itself is parsed as a stream.
- Import preview is capped at **20,000 data rows**.
- Row-level error details are capped at 100 while the total invalid count remains accurate.
- CI includes a 5,000-row preview test, far above the expected first pilot size.

A physical low/mid-range-device resilience pass remains part of #14; this ADR prevents unbounded file loading before that hardening stage.

## Consequences

- Excel remains an escape hatch rather than a runtime dependency.
- CleanRoute can import common `.xlsx` workbooks directly without a commercial library.
- Unsupported spreadsheet complexity fails explicitly instead of being guessed.
- The OOXML boundary can later be replaced behind the transfer service if real pilot files expose compatibility gaps.

## Revisit when

Revisit this decision if the real workbook requires:
- password protection/encryption;
- formulas without cached values;
- multiple-sheet import rules;
- macros;
- advanced date/style interpretation;
- broader compatibility that is cheaper to license than to maintain.
