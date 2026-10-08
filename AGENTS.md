# Agent instructions

This repository follows the Trigenys RAIDER engineering standard.

Changes must remain reusable, configuration-driven, non-regressive, testable and easy to adopt. Prefer stable Android/Jetpack APIs. Do not add a second library when an existing dependency already owns the capability.

Before risky changes, review `docs/engineering/lessons-learned.md`. Significant failures or near misses require a root-cause note and a proportionate prevention mechanism.

CI is change-scoped through AppFactory Impact-Aware CI. When adding a path, folder or workflow, classify it in `.github/appfactory-impact.json` and add a routing case to `.github/appfactory-impact.cases.json`; do not add new copied `paths:` filters. See `docs/engineering/ci-routing.md`.

Pull requests use `.github/pull_request_template.md` and must complete its RAIDER review section.

UI changes must keep Compose previews representative and must update or verify Roborazzi golden screenshots when the visual contract changes intentionally.
