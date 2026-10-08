# CI/CD change routing

CleanRoute workflows are intentionally scoped by change type so a small edit does not fan out into unrelated builds.

The routing policy is declared once in [`.github/appfactory-impact.json`](../../.github/appfactory-impact.json) and evaluated by the AppFactory [Impact-Aware CI](https://github.com/EagleFox31/appfactory-project-automation/blob/main/docs/impact-aware-ci.md) reusable workflow (`changed paths → impacted surfaces → required gates`). Jobs in [`ci.yml`](../../.github/workflows/ci.yml) are guarded by the returned gates instead of copying `paths:` filters into every workflow.

| Change | Android CI (`android-ci`) | Visual regression (`visual-regression`) | Pilot APK | Landing Pages | Project automation |
| --- | --- | --- | --- | --- | --- |
| Android domain/data code | Yes | No | On main after merge | No | PR/issue events only |
| Android UI, any `res/**`, goldens | Yes | Yes on PR | On main after merge | No | PR/issue events only |
| Shared Gradle/build configuration | Yes | Yes on PR | On main after merge | No | PR/issue events only |
| CI dispatcher or impact policy | Yes + `impact-policy` | Yes on PR | No | No | PR/issue events only |
| Landing under `site/**` | No | No | No | Yes | PR/issue events only |
| Documentation, brand, project metadata | No | No | No | No | PR/issue events only |
| Unclassified path | Yes (safe fallback) | Yes on PR (safe fallback) | Per its own filter | Per its own filter | PR/issue events only |

The `impact` job always runs (a few seconds) so a documentation-only PR shows an explicit no-op instead of silently skipping CI. A manual `workflow_dispatch` of **Android CI** runs every gate.

## Responsibilities

- **Android CI (`ci.yml`)**: dispatcher. Runs the impact analysis, then lint, unit tests and debug assembly (`android-ci`), Roborazzi verification on PRs (`visual-regression`) and the routing-policy check (`impact-policy`).
- **Pilot APK**: packages the installable pilot artifact only after relevant app/build changes land on `main`, or when manually dispatched.
- **Landing Pages**: validates `site/**` and publishes `site/dist` to the existing `gh-pages` branch.
- **Project automation**: issue/PR metadata synchronization; it is not a code-build workflow.

## Changing the policy

1. Edit `.github/appfactory-impact.json`: add the new path to the surface that owns it, or to `ignorePaths` if it must never trigger a gate.
2. Add or update a case in `.github/appfactory-impact.cases.json` describing the expected gates.
3. The `impact-policy` gate replays every case against the pinned AppFactory engine and checks that `ci.yml` references a single AppFactory SHA.

Run it locally with a checkout of the pinned AppFactory runtime:

```bash
node scripts/ci/verify-impact-cases.mjs --engine ../appfactory-project-automation/src/impact/engine.mjs
```

## Migration status

`pilot-apk.yml` and `pages.yml` still use native `paths:` filters. Their surfaces (`pilot-release`, `landing`) are already declared in the impact map so their changes do not fall back to the Android gates. They are not yet guarded by the dispatcher because `pages.yml` is chained to **Pilot APK** through `workflow_run`: a run that only skips jobs still concludes `success` and would trigger an unnecessary Pages publish. Migrating them requires replacing that chaining first.

The goal is not zero CI. The goal is that every workflow run has a reason tied to the files or event that changed.
