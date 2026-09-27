# ADR-007 — Owner dashboard from drillable local records

Status: Accepted  
Date: 2026-09-27  
Issue: #9

## Context

The owner needs a fast operational view without turning CleanRoute into a BI product. Every displayed number must be explainable from local operational records and remain useful while offline.

## Decision

The home screen is backed by a single Room dashboard repository. It reads the same local customer, route, visit and payment tables already used by field workflows.

The snapshot exposes six operational metrics:

- collection progress for the current local date;
- active clients;
- payments recorded during the current local date;
- arrears for the current service month;
- clients created during the current local date;
- route zones created for the current local date.

No historical trend, growth percentage or comparison is displayed unless a future feature provides an explicit comparable dataset.

### Local-day boundaries

Payments and newly created clients are timestamped as Instants. The dashboard converts the device's current LocalDate into start/end Instants using the device time zone, then queries the half-open interval [start, next-day-start).

Route/visit metrics continue to use their persisted ISO local date.

### Drill-down contract

The snapshot contains the exact record lists used to calculate every KPI. Tapping a KPI therefore renders those same records rather than running a second, potentially divergent calculation.

Collection progress is derived as completed non-SCHEDULED visits divided by all persisted visits for the day. The UI shows raw counts and only renders a progress bar when at least one visit exists.

### First use

If there are no active clients, the dashboard explains that clients must be added/imported first and exposes the client action directly.

If clients exist but no route has been started, collection shows "Aucune tournée démarrée" and links to the existing collection workflow.

## Consequences

- dashboard numbers work offline;
- every KPI is traceable to records visible in the UI;
- no speculative trend is shown;
- the dashboard adds no new database schema and no analytics dependency;
- future server-side reporting can reuse the domain snapshot contract without changing field data ownership.
