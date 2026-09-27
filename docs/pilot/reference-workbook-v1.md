# CleanRoute reference workbook v1

Status: Canonical pilot schema  
Date: 2026-09-27  
Related issues: #1, #6, #17

## Product decision

CleanRoute has no legacy customer workbook to migrate from.

The project therefore owns a canonical, agnostic reference workbook instead of waiting for a historical spreadsheet that does not exist.

The reference workbook is designed to be adapted by future operators without imposing:
- a real neighborhood naming scheme;
- a fixed customer count;
- a single collection cadence;
- a single tariff;
- a single payment provider;
- real customer identity or location data.

## Canonical operator workbook

File name:

`CleanRoute_Reference_Workbook_v1.xlsx`

Generated SHA-256:

`68f7251b6805143df6d96f152e8c94757f0184901fccd063e65a8ff59b734636`

The workbook contains:

1. Clients — first sheet and directly importable by the Android MVP;
2. README — adaptation instructions;
3. Paiements — export-contract examples;
4. Collectes — export-contract examples;
5. Catalogue — accepted/reference values;
6. Mapping_Import — canonical fields and aliases;
7. Scenarios_Pilote — resilience/usability scenarios;
8. Dashboard — consistency checks and synthetic KPIs.

The binary workbook is distributed as a pilot/operator artifact. The repository keeps the canonical first-sheet fixture as versionable text:

`app/src/test/resources/fixtures/cleanroute-reference-clients-v1.csv`

## First-sheet contract

Required by the current importer:

- name;
- zone;
- monthly_fee_xaf.

Recommended:

- external_id — stable source identity for idempotent re-import.

Optional:

- phone;
- address_label;
- collection_frequency;
- status.

Canonical v1 column order:

```text
external_id,name,phone,zone,address_label,collection_frequency,monthly_fee_xaf,status
```

Column position is not semantically significant because the importer maps normalized headers.

## Reference population

The canonical fixture contains:

- 40 synthetic customers;
- 4 generic zones;
- active and suspended customers;
- weekly, biweekly, monthly and custom cadence examples;
- multiple synthetic tariffs;
- non-operational synthetic phone values.

No real customer data is used.

## Acceptance for #1

Issue #1 is satisfied when:

- this canonical model is accepted as the initial product schema;
- required/optional fields are documented;
- a representative work cycle is documented;
- payment/collection semantics remain configurable where business evidence does not yet exist;
- no code assumes a nonexistent legacy workbook.

## Acceptance for #6

Issue #6 is satisfied when CI proves against the canonical fixture that:

1. first import creates 40 customers;
2. same-file retry creates no duplicate;
3. second preview reports 40 unchanged;
4. export to XLSX succeeds;
5. re-reading the exported XLSX maps back to the same 40 logical customers.

Unknown export-only columns such as created_at / updated_at may be reported as unknown by the current importer without blocking the import.

## Evolution rule

Future operators may provide their own workbook.

That workbook does not replace the v1 contract silently. New columns/aliases or changed semantics must be:

1. observed;
2. mapped explicitly;
3. covered by a fixture/test;
4. versioned if they change the contract.

This keeps the product adaptable without making one customer's spreadsheet the architecture.
