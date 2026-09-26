# PlanFora Project Changelog

## [Milestone 2.74] - Layout Reorder: Activity Type & Primary Link
* **Status:** Completed & Verified (15/15 Unit Tests Passing)
* **Goal:** Swap Primary Link and Activity Type sections on General Log / Observation screen.

### Changes Summary:
* `NewLogEntryScreen.kt`: Reordered Jetpack Compose layout components so that Activity Type selection appears above Primary Link (Mandatory) / Target Asset card.

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
