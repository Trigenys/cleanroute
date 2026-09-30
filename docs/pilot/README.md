# CleanRoute field pilot

Status: **Ready for field execution — not yet executed**  
Issue: #17  
Updated: 2026-09-30

This folder contains the evidence pack for the first real CleanRoute field pilot.

The pilot must not be marked complete from synthetic fixtures, screenshots or developer self-testing. Issue #17 remains open until a real operator completes the agreed workflow and the observations are recorded here.

## Readiness

All upstream product and engineering dependencies are closed:

- #1 — canonical pilot workflow and Excel schema;
- #6 — canonical import/export path;
- #7–#13 — collection, payments, dashboard, contact, retention, golden suite and threat model;
- #47–#55 — Stitch UI convergence and visual-regression coverage.

There is no longer a software blocker to starting the field session.

The canonical v1 pilot may use the repository's synthetic reference data. If an external or real customer workbook is used instead, complete `data-approval-checklist.md` first.

The exact currently published pilot build is recorded in `2026-09-30-pilot-readiness.md`.

## Pilot package

A dedicated Android build type named `pilot` is produced with:

- application ID suffix `.pilot`, so it can coexist with development builds;
- version suffix `-pilot`;
- debug signing for controlled internal installation only;
- release dependency fallback, so debug-only experiment dependencies such as the MapLibre spike are not packaged.

The GitHub Actions workflow `Pilot APK` publishes:

- the pilot APK;
- its SHA-256 checksum;
- the exact Git commit SHA.

This is an internal field-test package, not a production Play Store release.

## Evidence files

- `runbook.md` — exact pilot procedure;
- `report-template.md` — report structure and go/no-go gate;
- `metrics-template.csv` — raw task/observation metrics;
- `data-approval-checklist.md` — external/real workbook handling gate;
- `2026-09-30-pilot-readiness.md` — current build and launch readiness.

After the pilot, copy the report template to a dated report such as:

`docs/pilot/2026-10-03-first-field-pilot.md`

Do not rewrite observations into solutions while recording them. Capture what happened first; create follow-up issues only from observed evidence.
