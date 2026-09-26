# Pilot workflow contract — provisional discovery baseline

Status: **Provisional — awaiting the operator's real workbook/schema**  
Issue: #1  
Date: 2026-09-26

## Why this document exists

CleanRoute must replace a workflow that currently lives in a phone plus Excel without inventing business rules that do not exist in the operator's real process.

This document defines the minimum product contract we can safely use for engineering fixtures now. Every field marked **unverified** remains a discovery item and must not become a hard-coded production assumption.

## Representative day of operations

1. The owner opens CleanRoute and selects today's zone.
2. The app shows customers expected for collection in that zone.
3. The collector opens or expands a stop and records exactly one current outcome:
   - Collected;
   - Absent;
   - No waste.
4. If a result was entered incorrectly, it can be corrected without deleting the audit trail.
5. The collector can call or open WhatsApp for the customer when contact is required.
6. The owner can record a payment independently from the collection result.
7. The app remains fully usable while offline.
8. At the end of the work cycle, the owner can identify unpaid customers and export business-continuity data that can be opened in Excel.

## Proposed customer import contract

The real workbook headers are **not yet available**, so the following names are canonical CleanRoute fields, not claims about the current spreadsheet.

| CleanRoute field | Required | Proposed import aliases | Verification status |
| --- | --- | --- | --- |
| external_id | no | id, code_client, customer_id | unverified |
| name | yes | nom, client, nom_client | unverified |
| phone | yes for contact actions | telephone, téléphone, tel, mobile | unverified |
| zone | yes | zone, quartier, secteur | unverified |
| address_label | no | adresse, repere, repère | unverified |
| collection_frequency | yes | frequence, fréquence, periodicite | unverified |
| service_day | no | jour, jour_passage, passage | unverified |
| monthly_fee_xaf | yes for payment ledger | montant, abonnement, tarif | unverified |
| status | no | statut, etat, état | unverified |
| notes | no | note, notes, observation | unverified |

### Import rules we can safely implement

- match columns by normalized header names, never fixed column positions;
- trim whitespace and tolerate harmless casing/accent differences;
- preview create/update/skip counts before mutation;
- preserve a source identifier when present;
- never silently discard an unknown column;
- importing the same logical customer twice must not create duplicates;
- reject rows that cannot satisfy the minimum required fields instead of guessing values.

## Proposed collection model

A scheduled stop begins without a final outcome. A collector may then record one of:

- `COLLECTED`
- `ABSENT`
- `NO_WASTE`

A correction creates a new revision of the same visit rather than a second independent visit for the same customer/day.

### Unverified business questions

- Can one customer receive more than one collection on the same day?
- Are collection days fixed by subscription or chosen dynamically by zone?
- Are there weekly, biweekly and monthly plans, or another cadence?
- Does "absent" still count as a completed attempted visit?
- Are collectors assigned to fixed zones?

These questions must stay configurable or unresolved until pilot evidence answers them.

## Proposed payment model

A payment is a ledger event associated with a customer and a service period. The first implementation must support:

- cash;
- Orange Money;
- MTN MoMo;
- explicit correction/reversal rather than destructive deletion.

### Unverified payment questions

- Is the subscription always monthly?
- Can a customer make partial payments?
- Can a customer prepay several periods?
- Are collection fees flat per customer or dependent on plan/frequency?
- Is a receipt/reference number currently captured?
- Are historical arrears already present in the workbook?

Until confirmed, payment methods are configurable values and the domain must not assume one provider.

## Minimum fields for engineering fixtures

The synthetic fixture committed with this note deliberately contains only fields required to exercise:

- customer search;
- zone filtering;
- active/suspended state;
- collection cadence;
- payment amount;
- import idempotency.

No real customer information is included.

## Discovery exit criteria

Issue #1 should only be closed when the operator's actual workbook or an anonymized copy has been reviewed and:

- real column names are mapped to CleanRoute fields;
- required/optional fields are confirmed;
- at least one real collection-day scenario is confirmed;
- payment and arrears behavior is confirmed;
- any unsupported legacy data has an explicit migration decision.

Until then, this file is a safe engineering baseline, not a claim that the existing Excel format has been validated.
