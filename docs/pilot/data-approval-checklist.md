# Pilot data approval checklist

Issue links: #1, #6, #17

Use this checklist before any **external or real customer workbook** is used in the pilot.

The canonical v1 pilot may instead use the synthetic reference dataset defined by `docs/product/pilot-workflow.md`. Issues #1 and #6 are already closed because the canonical schema and import → retry → export contract are implemented and covered independently of a legacy production workbook.

## Source approval

- [ ] Business owner/operator approved the workbook for pilot use.
- [ ] Prefer an anonymized copy when real identity is not required.
- [ ] The source file is not committed to the public repository.
- [ ] Storage location is controlled by the business.
- [ ] A backup/original copy exists outside CleanRoute before migration testing.

## Schema validation

- [ ] Real column headers mapped to CleanRoute fields.
- [ ] Required vs optional fields confirmed.
- [ ] Customer identity/deduplication rule confirmed.
- [ ] Zone/quartier semantics confirmed.
- [ ] Collection cadence/day semantics confirmed.
- [ ] Monthly fee/payment semantics confirmed.
- [ ] Historical arrears handling confirmed.
- [ ] Unsupported legacy columns have an explicit keep/ignore/migrate decision.

## Import rehearsal

- [ ] Preview counts manually reconciled against source.
- [ ] At least a small sample of names/phones/zones checked.
- [ ] Same-file retry results in zero new duplicates.
- [ ] Invalid rows are actionable.
- [ ] Unknown columns are surfaced.

## Export rehearsal

- [ ] Clients sheet opens in Excel-compatible tooling.
- [ ] Paiements sheet opens and representative totals/events reconcile.
- [ ] Collectes sheet opens and representative outcomes reconcile.
- [ ] Export destination is understood to be outside CleanRoute's security boundary.

## Exit

If an external/real workbook is used, all applicable checks above must be evidenced before the operator starts the measured field cycle. If the canonical synthetic pilot data is used, record that choice in the field report and this external-data gate is not required.
