# Fix Missing Product Name in Search Supplies UI and Log Views

Fix issue where product names (e.g. "Necide 41") were not shown in the "Search Supplies" bottom sheet, input fields, log entry titles, and log card displays because `supply.batchCode` (which is empty `""` for store products) was being rendered instead of `supply.name` or `displayName`.

## Proposed Changes

### Data Model / Entities

#### [DiySupplyEntity.kt](file:///C:/109Backup/Project/PlanFora/app/src/main/java/com/mail2dev/planfora/data/local/entity/DiySupplyEntity.kt)

- Add a `displayName` extension property on `DiySupplyEntity`:
```kotlin
val DiySupplyEntity.displayName: String
    get() = when {
        name.isNotBlank() && batchCode.isNotBlank() && name != batchCode -> "$name ($batchCode)"
        name.isNotBlank() -> name
        batchCode.isNotBlank() -> batchCode
        else -> "Unnamed Supply"
    }
```

---

### UI Components & Screens

#### [NewLogEntryScreen.kt](file:///C:/109Backup/Project/PlanFora/app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt)

- Update `SupplyItemRow` to display `supply.displayName` instead of `supply.batchCode`.
- Update Search Supplies bottom sheet filter to include `displayName` and `activeIngredient`.
- Update input text fields for Product / Herbicide / DIY selection (`PestControlSection`, `WeedingSection`, `ProductionSection`, etc.) to use `selectedSupply?.displayName`.
- Update title preview generator to use `supply.displayName` for "Pest Control", "Feeding", and "Weeding (Chemical)" activities.

#### [LogComponents.kt](file:///C:/109Backup/Project/PlanFora/app/src/main/java/com/mail2dev/planfora/ui/logs/LogComponents.kt)

- Update log card renderers to use `supply.displayName` instead of `supply.batchCode` when displaying linked supply names.

#### [DiyLogsScreen.kt](file:///C:/109Backup/Project/PlanFora/app/src/main/java/com/mail2dev/planfora/ui/screens/DiyLogsScreen.kt)

- Update ledger entry to pass `supply.displayName` instead of `supply.name`.

---

### Unit Tests

#### [NEW] [DiySupplyEntityTest.kt](file:///C:/109Backup/Project/PlanFora/app/src/test/java/com/mail2dev/planfora/data/local/DiySupplyEntityTest.kt)

- Add unit test verifying `displayName` formatting for:
  - Store supply with product name and empty `batchCode`
  - DIY supply with product name and distinct `batchCode`
  - Supply with identical `name` and `batchCode`
  - Supply with missing `name` but valid `batchCode`

## Verification Plan

### Automated Tests
- Run Gradle unit test task:
  ```powershell
  ./gradlew app:testDebugUnitTest
  ```

### Manual Verification
- Render Compose Preview or check UI components for `SupplyItemRow` and Search Supplies picker.
