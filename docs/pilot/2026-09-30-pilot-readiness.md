# Pilot readiness — 2026-09-30

Issue: #17  
Status: **READY FOR FIELD EXECUTION — NOT YET EXECUTED**

This file records the software state handed to the first real operator session. It is readiness evidence only; it is not pilot-result evidence.

## Build

- Main commit: `65d762f300ec459d316f0abe234d095c515c40fe`
- Release tag: `pilot-latest`
- APK: `CleanRoute-pilot.apk`
- APK SHA-256: `7982b76e65e56b3280b5774de7784d275a983ae308e59dfedc223dd6b7617b9b`
- Public pilot endpoint: `https://cleanroute.trigenys.com/CleanRoute-pilot.apk`
- Release assets refreshed: 2026-09-30

## Gates

- Android CI after merge: PASS
- Pilot APK publish: PASS
- Landing / GitHub Pages publish: PASS
- Final permanent Roborazzi gate before merge: PASS
- UI convergence epic #47: CLOSED
- Child issues #48–#55: CLOSED
- Product/engineering dependencies #1 and #6–#13: CLOSED

## Data choice

The first field pilot can run with either:

1. the canonical synthetic reference data defined in `docs/product/pilot-workflow.md`; or
2. an external/real customer workbook, only after completing `data-approval-checklist.md`.

No real customer data should be committed to this repository.

## Remaining human gate

A real operator must now complete the runbook without developer guidance after onboarding. Record raw facts and metrics, then create a dated report from `report-template.md`.

Issue #17 must stay open until that evidence exists.
