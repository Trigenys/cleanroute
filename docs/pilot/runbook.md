# First field pilot runbook

Issue: #17  
Status: **Ready to execute**

## Objective

Determine whether a real operator can complete a representative CleanRoute work cycle with less friction than the current phone + Excel workflow, without developer guidance after onboarding.

The pilot is an operational validation, not a feature demonstration.

## Participants

Record roles, not unnecessary personal data:

- operator alias;
- observer/facilitator;
- developer present: yes/no.

The developer may onboard the operator before the timed work cycle, but should not coach individual tasks during measurement. Any intervention after onboarding counts as a support intervention.

## Prerequisites

Before starting:

1. Install the current `pilot-latest` build recorded in `2026-09-30-pilot-readiness.md`.
2. Verify the APK SHA-256 before installation.
3. Confirm Android CI, Pilot APK and visual-regression gates are green for the recorded build lineage.
4. Choose the canonical synthetic reference data, or complete `data-approval-checklist.md` before using any external/real customer workbook.
5. Ensure the device has enough storage and a screen lock.
6. Keep workbook/export destinations under operator or business control.
7. Never commit real customer data to Git.

## Onboarding

Maximum target: 10 minutes.

Show only:

- Home;
- Clients;
- Collection;
- Impayés/Paiements;
- Données & Excel;
- call/WhatsApp actions.

Explain that collection/payment writes work offline and that WhatsApp only prepares a message; the operator still sends explicitly.

Do not demonstrate every edge case before the operator attempts the workflow.

## Pilot cycle

### 1. Import

- Select the approved workbook or canonical pilot workbook.
- Review create/update/unchanged/invalid preview.
- If an error occurs, record the exact recovery action the operator tries.
- Apply the import.
- Retry the same import once.

Expected:
- no duplicate customers;
- replay is unchanged/idempotent;
- unsupported rows/columns are surfaced rather than guessed.

Record:
- import attempts;
- failed imports;
- support interventions;
- operator comments verbatim where useful.

### 2. Customer lookup

Ask the operator to find at least three customers using normal work cues such as name, phone or zone.

Expected:
- no developer guidance;
- customer state and payment/collection context are understandable.

### 3. Representative collection route

Use one representative zone/day.

The operator should independently:

- start/open the route;
- mark at least one Collecté;
- mark at least one Absent if the session presents one;
- mark Pas de déchets if the session presents one;
- correct one outcome only if a mistaken tap occurs or as an explicitly announced resilience check;
- open call/WhatsApp if operationally needed.

Do not manufacture business outcomes. If a status is absent from the session, record it as not observed.

### 4. Offline recovery

Before at least one collection/payment step:

1. enable airplane mode;
2. continue normal work;
3. force-stop CleanRoute after at least one committed write;
4. reopen;
5. verify the committed state remains;
6. restore connectivity only after verification.

Expected:
- local workflow remains available;
- no committed collection/payment disappears;
- no duplicate is created by retry.

### 5. Payments

Record only pilot-approved payment examples.

Cover where naturally available:

- cash;
- Orange Money;
- MTN MoMo;
- partial payment.

If a duplicate tap/retry occurs, record it. Do not deliberately duplicate a real financial record unless using an approved test customer.

Expected:
- one financial event per intended submission;
- reversal/correction remains auditable;
- arrears recalculate correctly.

### 6. Import/export recovery path

Demonstrate both recovery paths:

- one invalid or unsupported import file/row, then correction/retry;
- one export failure/reselection scenario when safely reproducible, or document why physical storage failure could not be induced safely.

Expected:
- message tells the operator what to do next;
- previously committed data remains intact.

### 7. Export

Export the resulting workbook.

Verify:
- Clients sheet opens;
- Paiements sheet opens;
- Collectes sheet opens;
- representative records from the session are present;
- no duplicate financial event was introduced by retry.

## Observation method

Record observable facts separately from interpretation.

Good:
- "Operator opened Clients twice before finding Impayés."
- "Operator asked what 'Annuler' meant on the payment correction dialog."
- "Second import showed 184 unchanged and 0 created."

Avoid:
- "The menu is confusing; redesign navigation."
- "We need a larger button."

Those are potential solutions, not raw observations.

## Metrics

Capture at minimum:

- task attempted/completed;
- corrections;
- failed imports;
- duplicate attempts;
- support interventions;
- offline/restart recoveries;
- approximate task duration where practical.

Use `metrics-template.csv`.

## Immediate stop conditions

Stop the pilot and preserve evidence if any occurs:

- P0 data loss;
- duplicate financial record that cannot be safely explained/reversed;
- corrupted Room/database state;
- import overwrites the wrong customer;
- export materially misrepresents a payment or collection;
- migration/startup prevents access to existing pilot data.

Create no speculative follow-up issue during the incident. First preserve the facts, reproduction steps and affected build SHA.

## End-of-session

1. Export the final workbook.
2. Save the raw metrics CSV.
3. Complete the report template.
4. Record any failure/near miss in `docs/engineering/lessons-learned.md`.
5. Create follow-up issues only from observed evidence.
6. Apply the go/no-go gate in the report.
