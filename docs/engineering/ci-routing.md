# CI/CD change routing

CleanRoute workflows are intentionally scoped by change type so a small edit does not fan out into unrelated builds.

| Change | Android CI | Visual regression | Pilot APK | Landing Pages | Project automation |
| --- | --- | --- | --- | --- | --- |
| Android domain/data code | Yes | No | On main after merge | No | PR/issue events only |
| Android UI/resources | Yes | Yes on PR | On main after merge | No | PR/issue events only |
| Landing under `site/**` | No | No | No | Yes | PR/issue events only |
| Documentation only | No | No | No | No | PR/issue events only |
| Issue/project metadata | No | No | No | No | Yes |

## Responsibilities

- **Android CI**: lint, unit tests and debug assembly. It no longer assembles the pilot APK.
- **Visual regression**: only UI/resource changes and visual baselines.
- **Pilot APK**: packages the installable pilot artifact only after relevant app/build changes land on `main`, or when manually dispatched.
- **Landing Pages**: validates `site/**` and publishes `site/dist` to the existing `gh-pages` branch.
- **Project automation**: issue/PR metadata synchronization; it is not a code-build workflow.

The goal is not zero CI. The goal is that every workflow run has a reason tied to the files or event that changed.
