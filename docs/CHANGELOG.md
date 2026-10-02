# PlanFora Project Changelog

## [Milestone 2.79] - Redesign Log Cards with Smart Parameter Parsing and Passport Design Language
* **Status:** Completed & Verified
* **Goal:** Eliminate title redundancy and unformatted parameter dumps across Log Cards and LogDetailSheet using a Passport-style 2-column metric grid and clean badge hierarchy.

### Changes Summary:
* `ui/screens/NewLogEntryScreen.kt`: Simplified auto-generated log title logic to produce concise activity titles (e.g., `"Chemical Weeding"`, `"Foliar Spray Application"`) without dot-concatenating all parameters.
* `ui/logs/LogComponents.kt`: Created `LogParameterGrid` composable to parse pipe-separated parameters, filter internal/calculation keys (`used_qty`, `used_unit`, `phi_expiry`, `rei_expiry`), combine values with units (e.g. `130 mL`, `15.5 kg`), and render Passport-style 2-column metric grids. Updated `ExpandedLogCard` header, product link, and note container.
* `ui/screens/LogsScreen.kt`: Updated `LogDetailSheet` to render parameter details using `LogParameterGrid`.

---

## [Milestone 2.78] - Fix Logs Visibility, Pure-Production Exclusion Filter, and FAB Date Binding
* **Status:** Completed & Verified
* **Goal:** Ensure all saved activity logs appear on LogsScreen under their targeted calendar date, refine pure-production log filtering, and bind FAB log creation to selected date.

### Changes Summary:
* `ui/logs/LogsViewModel.kt`: Refined `isPureProduction` filter to only exclude genuine `"PRODUCTION"` activity types or `"DIY"` category supplies. Enhanced `locationMatch` check to inspect `log.parameters` for `location:$location` alongside plant asset location notes. Updated `logEventDates` calculation to derive event indicators across all calendar days while respecting active filters.
* `ui/screens/LogsScreen.kt`: Updated FloatingActionButton (`+`) navigation to pass `selectedDate` timestamp parameter to `NewLogEntryScreen` instead of `System.currentTimeMillis()`.

---

## [Milestone 2.77] - Asset Passport UI Cleanup & Dead Legacy Tag Removal
* **Status:** Completed & Verified
* **Goal:** Eliminate hardcoded "N/A" placeholders on Asset Passport by removing legacy tag parsing and rendering only valid entity fields and custom fields.

### Changes Summary:
* `PlantDetailScreen.kt`: Removed legacy tag-parsing logic (`Mother Plant Link`, `Propagated`, `Batch:`, `PhysID:`, `GPS:`, `Rootstock:`, `Plot:`, `ExpHarv:`) from `MetadataGrid()`. Refactored `MetadataGrid()` and `AssetPassportHeader` to conditionally show only valid, top-level entity metrics (`Population / Qty`, `Block / Zone`, `Planted Date`) if present.
* `AddAssetViewModel.kt`: Removed unused internal private state variables (`_motherPlantLink`, `_propagatedDate`) and their reset logic.

---

## [Milestone 2.76] - Fix Custom Field Date/Time Formatting
* **Status:** Completed & Verified
* **Goal:** Format custom field epoch timestamps into human-readable date and time strings across read-only detail and log cards.

### Changes Summary:
* `CustomFieldComponents.kt`: Added `formatCustomFieldValue()` helper function that safely converts epoch millisecond strings into formatted date/time representations using `TimeFormatter`.
* `PlantDetailScreen.kt`: Updated `AssetPassportHeader` to format custom field DATE, TIME, and DATETIME values.
* `SupplyDetailScreen.kt`: Updated `ProductHeaderCard` to format custom field DATE, TIME, and DATETIME values.
* `LogsScreen.kt`: Updated `LogDetailSheet` to format custom field DATE, TIME, and DATETIME values.

---

## [Milestone 2.75] - UI Layout: Reorder Activity Type Above Primary Link
* **Status:** Completed & Verified (15/15 Unit Tests Passing)
* **Goal:** Position Activity Type section above Primary Link section on NewLogEntryScreen.

### Changes Summary:
* `NewLogEntryScreen.kt`: Reordered composables so that Activity Type selection appears above Primary Link (Mandatory) section.

---

## [Milestone 2.74] - UI Refinement: General Log Screen Layout
* **Status:** Completed & Verified
* **Goal:** Initial swap attempt and verification for General Log screen components.

### Changes Summary:
* `NewLogEntryScreen.kt`: Adjusted screen composable layout structure.

---

## [Milestone 2.73] - 2-Stage Sandbox AI Setup
* **Status:** Completed
* **Goal:** Established lightweight Gemini Web + Android Studio development workflow.

### Changes Summary:
* Configured local documentation memory layer in `docs/` (`PROJECT_CONTEXT.md`, `ARCHITECTURE.md`, `CURRENT_TASK.md`, `CHANGELOG.md`, `SYSTEM_PROMPT.md`).
* Streamlined AI execution stack directly inside Android Studio.

---

## [Milestone 2.72] - UI/UX Polish: Weeding & Location Selectors
* **Status:** Completed & Verified (15/15 Unit Tests Passing)
* **Goal:** Polish Weeding Location/Block bottom sheet and card displays.

### Changes Summary:
* `NewLogEntryScreen.kt`: Removed emoji string prefix to resolve double location icon rendering.
* `LocationSelectionBottomSheet.kt`: Redesigned layout using `OutlinedButton` and structured `LazyColumn` featuring bold location names, right-aligned block tags (e.g., `[T2]`), and `primaryContainer` selection highlighting.

---

## [Milestone 2.71] - SubLocation & Block Tracking
* **Status:** Completed
* **Goal:** Enable subLocation/Block selection for location-based activity logs.

### Changes Summary:
* Added Location and Block/Zone selection for activity logging.
* Persisted `subLocation` in log parameters.
* Updated `LogsScreen.kt` log cards to display `📍 Location [Block/Zone]`.
