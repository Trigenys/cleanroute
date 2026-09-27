# Visual regression testing

CleanRoute treats Roborazzi screenshots as a visual contract for core business flows.

## Canonical viewports

- Phone: 390 × 844 dp.
- Tablet representative: 840 × 900 dp.

The phone viewport is the primary product contract. At least one tablet golden remains in the suite to catch layout regressions that only appear on wider screens.

## Covered flows

The committed golden suite covers:

- home / owner dashboard;
- first-use home state;
- daily collection route;
- collection load error;
- customer directory empty/populated;
- customer detail;
- payments unpaid/paid;
- payment load error;
- reversed payment history;
- retention/referral customer state;
- Excel import preview;
- light and dark app foundation;
- tablet owner dashboard.

## Determinism rules

Screenshot fixtures must use fixed dates, times, IDs, names, amounts and statuses. Do not use random UUIDs, system clock values or network data inside screenshot tests.

The GitHub workflow also fixes the runner timezone to UTC. Business screens that need local dates receive explicit fixture dates.

## Updating goldens

Intentional visual changes require an explicit golden update:

```bash
gradle :app:recordRoborazziDebug
```

Review the changed PNGs before committing them. A pull request with an existing baseline runs:

```bash
gradle :app:verifyRoborazziDebug
```

A missing or changed golden fails the visual workflow.

## Inspecting CI failures

When verification fails, the workflow uploads the `roborazzi-visual-diff` artifact containing Roborazzi comparison output plus the committed baselines.

For the initial bootstrap only, when no committed PNG baseline exists, CI records candidates and uploads `roborazzi-bootstrap-goldens`.

## Release gate

A visual-baseline change is accepted only after the verification workflow passes twice consecutively against the same committed goldens, with no further PNG update between the two runs.
