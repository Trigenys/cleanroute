# Pilot data approval checklist

Issue links: #1, #6, #17

Use this checklist before any real workbook is used in the pilot.

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

#1 and #6 should remain open until this checklist is supported by actual workbook evidence.
