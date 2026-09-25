# PlanFora - Technical Architecture Baseline

## 1. Environment & SDK
* **Namespace:** `com.mail2dev.planfora`
* **Compile SDK:** 37 | **Target SDK:** 36 | **Min SDK:** 24 (Android 7.0)
* **Build Configuration:** MultiDex Enabled, KSP Code Generation.

## 2. Core Libraries & Frameworks
* **UI:** Jetpack Compose (BOM 2024.09.00), Material 3 (`androidx.compose.material3`), Extended Icons.
* **Database & Storage:** Room Database v28 (`androidx.room`), Storage Access Framework (SAF) for JSON Export/Import.
* **Navigation:** Navigation Compose (`androidx.navigation:navigation-compose:2.8.0`).
* **Media & Attachments:** Coil Compose (`2.7.0`), Native `MediaRecorder` & `MediaPlayer`, `FileProvider` with `TakePicture` contract.
* **Serialization:** `kotlinx-serialization-json`.

## 3. Directory & Package Map
* `data/local/`:
  * `AppDatabase.kt` (Room Database, Schema Migrations v1 -> v28)
  * `DataBackupManager.kt` (SAF JSON Engine)
  * `dao/`: `PlantAssetDao`, `JournalLogDao`, `DiySupplyDao`, `MasterDao`, `MeasurementToolDao`, `CustomFieldDao`
  * `entity/`: `PlantAssetEntity`, `JournalLogEntity`, `DiySupplyEntity`, `MasterEntities`, `MeasurementToolEntity`, `CustomFieldEntities`
* `data/repository/`: `JournalRepository.kt`, `SupplyRepository.kt`
* `ui/`:
  * `assets/`: `AddAssetScreen`, `AddAssetViewModel`, `AssetsViewModel`
  * `components/`: `CustomFieldComponents`, `HarvestActivityCard`, `InlineAudioPlayer`, `LocationSelectionBottomSheet`, `MediaAttachmentStrip`, `MemorySheets`, `PlanForaBottomBar`, `SharedComponents`, `StringPickerSheet`
  * `logs/`: `CalendarComponents`, `LogComponents`, `LogsViewModel`
  * `navigation/`: `Screen.kt` (Sealed class routes)
  * `profile/`: `ProfileViewModel`, `LabAnalyticsManager`
  * `screens/`: `LogsScreen`, `AssetsScreen`, `SuppliesScreen`, `NewLogEntryScreen`, `PlantDetailScreen`, `SupplyDetailScreen`, `DiyLogsScreen`, `ProfileScreen`
  * `supplies/`: `AddSupplyScreen`, `AddSupplyViewModel`, `SuppliesViewModel`
* `util/`: `TimeFormatter.kt`

## 4. UI Theme & Styling Standard
* **Theme System:** Material 3 (`PlanForaTheme`) located in `ui/theme/`.
* **Colors:** Palette tokens defined in `ui/theme/Color.kt`. Standard surface and container colors derived from `MaterialTheme.colorScheme`.
* **Typography:** Material 3 Typography hierarchy defined in `ui/theme/Type.kt`.