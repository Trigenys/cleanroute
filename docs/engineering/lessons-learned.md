# Engineering lessons learned

This file is part of the RAIDER failure-memory loop.

For every significant defect, deployment failure, security issue or near miss, record:

1. what happened;
2. the root cause;
3. why existing controls did not catch it;
4. the fix;
5. the reusable prevention rule added to code, CI, tests or documentation.

Do not turn this file into a raw error-log dump. The goal is institutional memory that prevents repeated mistakes.


## 2026-09-24 — Compose BOM exceeded the initial compile SDK

**What happened:** the first Android blueprint CI failed during AAR metadata validation.

**Root cause:** the September 2026 Compose BOM resolved Compose 1.12.1 and Lifecycle 2.11.0, whose metadata requires compileSdk 37 or newer, while the blueprint compiled against API 36.

**Why the control worked:** AppFactory validates the blueprint itself in CI before any product repository is provisioned, so the incompatibility never reached a generated application.

**Fix:** compile against API 37 while retaining targetSdk 36 until the product deliberately adopts the newer runtime behavior.

**Prevention rule:** whenever the Android dependency baseline is upgraded, the blueprint CI must run AAR metadata validation as part of lint/test/assemble; never infer compileSdk compatibility from targetSdk requirements.

## 2026-09-24 — Generated workflows used deprecated Node 20 action runtimes

**What happened:** CI warned that older GitHub Action majors were being force-run on Node 24 because their bundled Node 20 runtime is deprecated.

**Root cause:** the first blueprint draft reused action majors from older Trigenys workflows.

**Fix:** new Android workflows use the current action majors: checkout v7, setup-node v7 where applicable, setup-java v6, Gradle Actions v6 and upload-artifact v7.

**Prevention rule:** a newly introduced blueprint must pin supported current action majors; deprecation warnings are treated as engineering debt, not harmless log noise.


## 2026-09-27 — Explicit Compose `weight` import resolved to an internal symbol

**What happened:** both Android CI and Roborazzi failed to compile the design-system PR at the same `Modifier.weight(1f)` calls.

**Root cause:** `App.kt` explicitly imported `androidx.compose.foundation.layout.weight`. With the current Compose baseline, that name resolves to an internal implementation symbol instead of the public `RowScope.weight` extension intended for children inside a `Row`.

**Why two workflows failed:** Android CI and visual regression are different pipelines, but both compile the same application sources before doing their own work. One Kotlin compilation defect therefore surfaced twice.

**Fix:** remove the explicit `weight` import and let Kotlin resolve `Modifier.weight(...)` through the enclosing `RowScope`. The unused icon import discovered during the same pass was removed as well.

**Prevention rule:** never explicitly import `androidx.compose.foundation.layout.weight` in this Compose baseline. Use `Modifier.weight(...)` only inside a `RowScope` or `ColumnScope`, and treat duplicated compile failures across CI/visual workflows as one source failure until proven otherwise.


## 2026-09-27 — `RoomDatabase` is not a Kotlin `use {}` receiver

**What happened:** Android CI and Roborazzi both failed while compiling `RoomPersistenceTest`.

**Root cause:** the test wrapped `CleanRouteDatabase` in Kotlin's `use { }` helper. `RoomDatabase` exposes `close()`, but it is not a receiver type accepted by the `Closeable.use` extension in this build baseline, so Kotlin could not infer the generic receiver/result types.

**Why two workflows failed:** both pipelines compile the same unit-test sources before their own CI or screenshot tasks.

**Fix:** manage the database lifetime explicitly with `try/finally` and call `database.close()` in the `finally` block.

**Prevention rule:** do not assume that an API exposing `close()` implements Kotlin/JVM `Closeable`. For Room database lifecycle tests, use explicit `try/finally` unless the concrete API contract is verified to implement a compatible closeable interface.

## 2026-09-27 — In-memory Room tests did not prove restart durability

**What happened:** the existing repository tests proved collection/payment idempotency, but almost all of them used an in-memory Room database.

**Root cause:** in-memory databases are convenient and fast, but closing them destroys the state. A passing test therefore could not prove that a committed visit, payment or outbox operation survives process death or a device restart.

**Why existing controls did not catch it:** domain and repository assertions exercised the right business rules but never crossed a real persistence reopen boundary.

**Fix:** add file-backed Room resilience tests that write real customer/collection/payment/outbox data, close the database, reopen it from disk and then replay the same operations. Add a migration-preservation test for existing pilot records.

**Prevention rule:** every offline-first critical write path must have at least one file-backed close/reopen test. In-memory Room tests remain useful for business rules but are not accepted as evidence of restart durability.

## 2026-10-02 — Visual regression path filter watched a resource folder that does not exist

**What happened:** while migrating CI to AppFactory Impact-Aware CI, the inventory showed that `visual-regression.yml` triggered on `app/src/main/res/drawable/**`. The repository has no `drawable/` folder: its rendered assets live in `res/drawable-nodpi/` and `res/mipmap-anydpi-v26/`. A change to the logo or launcher icon therefore ran Android CI but never the Roborazzi golden suite. This was a near miss: no incorrect screenshot was merged.

**Root cause:** routing rules were hand-copied `paths:` lists duplicated across workflows, written against an assumed Android resource layout and never tested themselves. Nothing failed when a filter stopped matching the real tree.

**Why existing controls did not catch it:** a missing trigger produces no run and therefore no red signal; GitHub does not warn about `paths:` patterns that match nothing.

**Fix:** the routing policy now lives in one declarative file, `.github/appfactory-impact.json`, consumed by the AppFactory reusable impact analysis. The UI surface owns all of `app/src/main/res/**`, and unclassified paths fall back to every gate.

**Prevention rule:** routing rules are tested like code. `.github/appfactory-impact.cases.json` lists expected gates per change type (including a `drawable-nodpi` case) and the `impact-policy` gate replays them against the pinned AppFactory engine whenever the policy, the dispatcher or the verifier changes. New path-scoped gates must be expressed in the impact map with a case, not as a new copied `paths:` list.

**Generalized lesson:** a trigger filter that silently matches nothing is a disabled test. Prefer fail-safe routing (unknown paths run everything) and assertions on the routing table over per-workflow path lists.
