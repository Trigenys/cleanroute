# Excel migration guide

## Import

From **Plus → Données & Excel → Choisir un fichier**, select an `.xlsx` or `.csv` customer file.

CleanRoute first displays a preview with:
- customers to create;
- customers to update;
- unchanged rows;
- invalid rows;
- unmapped columns.

Nothing is written until **Confirmer l'import** is pressed.

The first worksheet is used for `.xlsx` files. Legacy `.xls` files must be saved as `.xlsx` first.

## Export

Use **Exporter vers Excel** to create `CleanRoute-export.xlsx`.

The workbook contains:
- `Clients` — identity, contact, zone, subscription, status and timestamps;
- `Paiements` — period, amount, method, state and timestamps;
- `Collectes` — visit, date, status and revision.

This export is intended as a business-continuity file and can be opened directly in Excel.

## Round-trip rule

A customer's explicit imported `external_id` is preserved. If no source ID exists, CleanRoute creates a deterministic fingerprint identifier so importing the same logical row again does not create a second customer.

Real workbook aliases remain provisional until issue #1 is completed with an anonymized copy of the operator's spreadsheet.
