# ADR-010 — Mapping and route-optimization spike

Status: Accepted as spike only  
Date: 2026-09-27  
Issue: #15

## Context

CleanRoute's collection workflow is already usable without mapping. Before adding map/routing infrastructure to the MVP, this spike tests whether the candidate stack is technically compatible with the Android baseline and whether route ordering can produce a measurable signal on a dense urban fixture.

No production savings claim is made. CleanRoute does not yet have a representative set of customer GPS coordinates or measured driving times.

## Boundary

Mapping remains outside the domain package.

The production-side contract uses only CleanRoute-owned types:

- GeoCoordinate;
- GeocodedCustomer;
- CustomerMapModel;
- RouteMetricProvider;
- RouteMetrics;
- RouteComparison.

A CI test rejects MapLibre or GraphHopper/jsprit imports inside the domain layer.

The map prototype is compiled only from the debug source set. The optimizer is a test-only dependency. MappingFeatureConfig is disabled by default.

Therefore the normal customer, collection and payment flows remain available without a map renderer, routing engine or optimizer.

## Reuse-first decisions

### MapLibre Compose — ADOPT for the internal prototype

Version evaluated: 0.18.0  
Maven: org.maplibre.compose:maplibre-compose:0.18.0  
Android runtime: org.maplibre.compose:maplibre-compose-runtime-vulkan-android:0.18.0  
License: BSD 3-Clause.

Why:
- Compose-native API;
- Android minSdk 24 is compatible with CleanRoute minSdk 26;
- supports GeoJSON sources and offline-map capabilities;
- renderer can remain behind CleanRoute-owned coordinate/map models.

Operational requirements:
- a render runtime must be packaged;
- a tile/style source is required;
- the prototype uses a remote OpenFreeMap style, therefore it is not an offline production solution;
- production adoption would require an intentional tile/offline-pack strategy and attribution review.

Decision boundary:
MapLibre is debugImplementation only in this spike. Do not wire it into the MVP navigation until real customer coordinates and an offline tile strategy exist.

### GraphHopper — LEARN

Version evaluated: 11.0  
License: Apache-2.0.

Why not adopt in the Android MVP:
- road routing needs an OSM road graph, preprocessing and non-trivial graph storage;
- current GraphHopper is designed well as a Java library or routing service, but carrying a Cameroon road graph and update lifecycle on a low/mid Android device is premature;
- the current product has no representative GPS dataset that justifies operating this infrastructure.

Potential future role:
Implement a RouteMetricProvider adapter backed by a server-side/self-hosted GraphHopper instance if real pilot coordinates show that straight-line ordering is insufficient.

No GraphHopper production dependency is added by this spike.

### jsprit — ADAPT

Version evaluated: 2.0.0  
Maven: com.graphhopper:jsprit-core:2.0.0  
License: Apache-2.0.  
Runtime requirement: Java 21+.

Why:
- suitable for TSP/VRP ordering;
- accepts an external transport-cost model;
- can consume a future road-distance/time matrix rather than owning map rendering or geocoding.

How it is adapted:
- testImplementation only;
- the spike projects lat/lon to local metric coordinates for jsprit ordering;
- CleanRoute then calculates comparable distance/time metrics through its own RouteMetricProvider;
- a future implementation should feed road metrics from GraphHopper or another routing adapter.

Do not put jsprit types in Android domain entities or persisted Room models.

## Dense-zone benchmark

Fixture:
- one depot;
- six synthetic customer points within roughly a 500 m radius of the depot;
- coordinates are shaped like a dense Douala neighborhood but are synthetic and are not customer data;
- the manual order intentionally represents an unoptimized zig-zag sequence: A → B → C → D → E → F;
- the comparison is a closed tour returning to the depot.

Metric model:
- Haversine distance between ordered points;
- indicative duration at 18 km/h;
- this is not road-network routing and ignores one-way streets, traffic, turn penalties and road access.

Manual baseline for this fixture:
- approximately 5.89 km;
- approximately 19.6 minutes at the stated synthetic average speed.

The geometric optimum of the fixture is approximately:
- 3.35 km;
- 11.2 minutes at the same synthetic average speed.

The automated jsprit spike does not assert a percentage saving. It only requires:
- all stops remain assigned exactly once;
- the jsprit order is shorter than the manual fixture order;
- the corresponding indicative duration is lower;
- representativeData remains false.

These numbers are an engineering signal only. They must not be used in product copy, pricing or business forecasts.

## What is still missing before a product decision

Before route optimization can move from spike to product feature:

1. collect or geocode a representative pilot set with consent and accuracy checks;
2. define depot/start/end behavior used by the real collector;
3. measure road distance and travel time rather than straight-line distance;
4. test one-way streets, inaccessible roads and local road quality;
5. compare operator-selected order against optimized order for multiple real days;
6. quantify runtime, battery, storage and network requirements on a representative Android device;
7. decide where map tiles and road-routing data are hosted/cached.

## Outcome

The MVP stays map-independent.

- MapLibre: ADOPT for isolated internal map prototyping.
- GraphHopper: LEARN as a future road-metric provider, not an Android MVP dependency.
- jsprit: ADAPT as an optimization engine behind CleanRoute-owned contracts.

No route-savings claim is accepted until representative pilot data is available.
