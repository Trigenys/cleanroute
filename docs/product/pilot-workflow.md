# Pilot workflow contract — canonical reference baseline

Status: **Canonical v1 — no legacy workbook exists**  
Issues: #1, #6, #17  
Date: 2026-09-27

## Product reality

CleanRoute is not migrating an existing production workbook. No historical customer spreadsheet exists to validate.

The initial schema is therefore defined by the CleanRoute reference workbook v1 and its versioned CSV first-sheet fixture.

This is intentional: the format stays agnostic and future operator spreadsheets are adapted into the canonical model through explicit header aliases/mapping rather than becoming hard-coded product assumptions.

## Representative day of operations

1. The owner imports or creates customers.
2. The owner selects today's zone.
3. The app shows active customers for that zone.
4. The collector records exactly one current outcome per stop:
   - Collected;
   - Absent;
   - No waste.
5. Corrections preserve an audit revision.
6. Call/WhatsApp actions remain explicit.
7. Payments are recorded independently from collection outcomes.
8. Core work remains available offline.
9. At the end of the cycle, the owner reviews arrears and exports Excel-compatible business-continuity data.

## Canonical customer import contract

| CleanRoute field | Required | Examples / aliases | Rule |
| --- | --- | --- | --- |
| external_id | no, recommended | id, code_client, customer_id, client_id, code | Stable identity for safe re-import |
| name | yes | nom, client, nom_client, customer, customer_name | Human-readable customer label |
| phone | no | telephone, tel, mobile, numero | Contact action input |
| zone | yes | quartier, secteur, area, neighborhood | Operational grouping, free text |
| address_label | no | adresse, repere, reference, location | Human-readable local reference |
| collection_frequency | no | frequence, periodicite, cadence | weekly / biweekly / monthly / custom |
| monthly_fee_xaf | yes | montant, abonnement, tarif, prix, fee | Positive XAF service amount |
| status | no | statut, etat, state | active / suspended |

## Import invariants

- map by normalized headers, never fixed column position;
- trim harmless whitespace/case/accent differences;
- preview create/update/unchanged/invalid before mutation;
- preserve stable source identity;
- repeated import of the same logical customer must not duplicate;
- unknown headers are surfaced;
- rows missing required values fail explicitly;
- import remains capped at 20,000 rows per file.

## Collection model

Current outcomes:

- COLLECTED
- ABSENT
- NO_WASTE

A repeated identical outcome is idempotent.

A correction creates a new revision instead of a second unrelated visit.

The pilot population rule remains all ACTIVE customers in the explicitly selected zone until real scheduling requirements justify a richer calendar.

## Payment model

A payment is a ledger event attached to a customer and service period.

Current supported method labels include:

- cash;
- Orange Money;
- MTN MoMo.

The domain supports partial payments and explicit reversal rather than destructive deletion.

Provider labels remain configurable business values rather than architecture.

## Canonical fixture

Repository fixture:

`app/src/test/resources/fixtures/cleanroute-reference-clients-v1.csv`

Population:

- 40 synthetic customers;
- 4 zones;
- varied cadence / fee / status values;
- no real personal data.

The complete operator workbook also includes payment, collection, catalogue, mapping, scenario and dashboard sheets.

## Exit criteria

#1 no longer waits for a nonexistent workbook. It can close once the canonical v1 model is committed and accepted.

#6 can close once the canonical fixture passes import → retry → export → re-read tests in CI.

#17 remains a field-usability gate: a real operator must still use the app through the runbook. The customer data used in that pilot may remain synthetic.
