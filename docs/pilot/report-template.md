# First field pilot report — TEMPLATE

Status: **Not executed**  
Issue: #17

> Copy this file to a dated report after the actual pilot. Do not fill evidence with synthetic values.

## Session identity

- Pilot date:
- Build commit SHA:
- APK SHA-256:
- Operator alias:
- Observer:
- Approved workbook reference:
- Airplane-mode test performed: yes/no
- Force-stop/reopen test performed: yes/no
- Device class/model:
- Android version:

## Preconditions

- [ ] #1 real workflow/schema evidence complete.
- [ ] #6 real/anonymized workbook validation complete.
- [ ] Android CI green for pilot commit.
- [ ] Visual regression green for pilot commit.
- [ ] Pilot APK checksum verified before install.
- [ ] No real customer data committed to Git.

## Task results

| Task | Attempted | Completed without guidance | Duration | Corrections | Support interventions | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| Import workbook |  |  |  |  |  |  |
| Find customer |  |  |  |  |  |  |
| Start/open route |  |  |  |  |  |  |
| Record collection outcomes |  |  |  |  |  |  |
| Offline + restart recovery |  |  |  |  |  |  |
| Record payment |  |  |  |  |  |  |
| Review arrears |  |  |  |  |  |  |
| Recover failed import/export |  |  |  |  |  |  |
| Export final workbook |  |  |  |  |  |  |

## Required metrics

- Total task attempts:
- Completed without developer guidance:
- Corrections:
- Failed imports:
- Duplicate attempts:
- Duplicate financial records created:
- Support interventions after onboarding:
- Offline writes recovered after reopen:
- Import/export recovery attempts completed:

Attach or reference the raw `metrics-template.csv` copy used for the session.

## Raw usability observations

Record observed behavior before proposing fixes.

1.
2.
3.

## Data integrity review

- [ ] No P0 data-loss issue observed.
- [ ] No unresolved duplicate financial record.
- [ ] Same-import retry created no duplicate customers.
- [ ] Offline committed collection/payment survived reopen.
- [ ] Exported customer/payment/collection records match the app.
- [ ] Any correction/reversal remains auditable.

Evidence/notes:

## Import/export recovery evidence

What failed:

What message/action was shown:

What the operator did next:

Outcome:

## Failures and near misses

List facts, reproduction steps and severity.

If significant, add an entry to `docs/engineering/lessons-learned.md`.

## Go / no-go gate

A **GO for the next iteration** requires all of the following:

- every core task was completed by the operator without developer guidance after onboarding;
- no open P0 data-loss/corruption issue;
- no unresolved duplicate financial-record defect;
- approved workbook import/export path demonstrated;
- offline + reopen path demonstrated;
- observations and raw metrics are recorded;
- any blocking P1 found in the pilot has an explicit mitigation or decision.

A **NO-GO** is required if any of these remain false.

Decision: **PENDING — real pilot not yet executed**

Rationale:

## Evidence-derived follow-up issues

Create these only after the pilot and only when supported by an observation.

- None yet.
