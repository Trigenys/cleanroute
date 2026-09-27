# CleanRoute field pilot

Status: **Prepared, not yet executed**  
Issue: #17  
Date: 2026-09-27

This folder contains the evidence pack for the first real CleanRoute field pilot.

The pilot must not be marked complete from synthetic fixtures, screenshots or developer self-testing. Issue #17 remains open until a real operator completes the agreed workflow with approved/anonymized customer data and the observations are recorded here.

## Current blockers

Two upstream issues remain intentionally open:

- #1 — validate the real operator workflow and actual Excel schema;
- #6 — validate import/export against the approved/anonymized real workbook.

Those issues are not paperwork. They are the evidence that the product matches the business currently operated in Excel.

## Pilot package

A dedicated Android build type named `pilot` is produced with:

- application ID suffix `.pilot`, so it can coexist with development builds;
- version suffix `-pilot`;
- debug signing for controlled internal installation only;
- release dependency fallback, so debug-only experiment dependencies such as the MapLibre spike are not packaged.

The GitHub Actions workflow `Pilot APK` uploads:

- the pilot APK;
- its SHA-256 checksum;
- the exact Git commit SHA.

This is an internal field-test package, not a production Play Store release.

## Evidence files

- `runbook.md` — exact pilot procedure;
- `report-template.md` — report structure and go/no-go gate;
- `metrics-template.csv` — raw task/observation metrics;
- `data-approval-checklist.md` — workbook/data handling gate.

After the pilot, copy the report template to a dated report such as:

`docs/pilot/2026-10-03-first-field-pilot.md`

Do not rewrite observations into solutions while recording them. Capture what happened first; create follow-up issues only from observed evidence.
