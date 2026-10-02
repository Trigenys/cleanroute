# CleanRoute Stitch UI reference

Status: **visual source of truth for the Android APK**  
Reference package: Stitch export supplied 2026-09-28.

The HTML files in the Stitch export are design references only. CleanRoute remains a native Jetpack Compose application; business logic, Room persistence and offline-first behavior stay in the existing Android architecture.

## Screen mapping

| Stitch reference | Native target |
| --- | --- |
| Accueil / démarrage | `ui/dashboard/DashboardScreens.kt` |
| Sélection zone de collecte | `ui/collection/CollectionScreens.kt` |
| Configuration zone collecte | `ui/collection/CollectionScreens.kt` |
| Répertoire clients | `ui/customer/CustomerScreens.kt` |
| Nouveau client / formulaire | `ui/customer/CustomerScreens.kt` |
| Fiche détail client | `ui/customer/CustomerScreens.kt` |
| Paiements & impayés | `ui/payment/PaymentsScreen.kt` |
| Données & Excel | `ui/transfer/DataTransferScreen.kt` |
| Shared shell/navigation | `ui/App.kt`, `ui/navigation/AppDestination.kt` |
| Shared tokens/components | `ui/theme/Theme.kt`, `ui/components/CleanRouteComponents.kt` |

## Reference images

Parity is judged against the Stitch PNGs stored in [`docs/ui/stitch/`](stitch/), not against our own Roborazzi baselines (which only prove stability, see #65 and #73).

| File | Stitch screen | Native target |
| --- | --- | --- |
| `stitch/accueil.png` | Accueil / démarrage | `ui/dashboard/DashboardScreens.kt` |
| `stitch/collecte-selection-zone.png` | Sélection zone de collecte | `ui/collection/CollectionScreens.kt` |
| `stitch/clients-repertoire.png` | Répertoire clients | `ui/customer/CustomerScreens.kt` |
| `stitch/nouveau-client.png` | Nouveau client / formulaire | `ui/customer/CustomerScreens.kt` |

Still missing from the repository: Configuration zone collecte, Fiche détail client, Paiements & impayés, Données & Excel.

The reference screens use a Dakar context (+221, Médina, Plateau). CleanRoute targets Cameroon (+237, Douala neighbourhoods): parity applies to composition, hierarchy and styling, not to the illustrative locale data.

## Visual contract

- Plus Jakarta Sans for every text style, bundled in `res/font` (SIL OFL 1.1, licence shipped in `assets/licenses/`); never a downloadable/network font.
- Real CleanRoute mark (`drawable-nodpi/cleanroute_mark.png`) in the shell header, never a placeholder letter tile.
- Emerald primary with blue-slate tonal surfaces.
- `#F9F9FF` app canvas and white elevated content cards.
- 24dp-class card radius and pill-shaped actions/statuses.
- Minimum 56dp primary field actions.
- Strong, highly legible metric hierarchy.
- Explicit offline status in the shell and relevant operational cards.
- Bottom navigation remains Accueil / Collecte / Clients / Plus.
- Screen density must stay practical for one-handed field use.
- No UI change may introduce a runtime network dependency.

## Implementation order

1. #48 Design tokens and reusable primitives
2. #49 App shell and navigation
3. #50 Home dashboard
4. #51 Collection
5. #52 Customer directory/create
6. #53 Customer detail
7. #54 Payments + Data & Excel
8. #55 Visual regression baseline/gates

## Rule for mock-only concepts

The Stitch package contains some illustrative concepts (for example RFID, municipal registry wording, material allocation, GPS sync or payment-provider details). They are not automatically product requirements. A visual element is implemented only when the current CleanRoute domain model supports it, or after a separate functional issue adds the required domain behavior.
