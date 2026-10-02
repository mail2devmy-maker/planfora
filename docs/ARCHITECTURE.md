# PlanFora — Technical Architecture Baseline

## 1. Project Metadata & Environment
* App Name: PlanFora
* Namespace: com.mail2dev.planfora
* Compile SDK: 37 | Target SDK: 36 | Min SDK: 24 (Android 7.0)
* Build Setup: MultiDex Enabled, KSP Code Generation.
* Architecture: Offline-first MVVM / Repository Pattern.

## 2. Core Libraries & Frameworks
* UI: Jetpack Compose (BOM 2024.09.00), Material 3 (androidx.compose.material3), Extended Icons.
* Database & Storage: Room Database (androidx.room), Storage Access Framework (SAF) for JSON Export/Import.
* Navigation: Navigation Compose (androidx.navigation:navigation-compose:2.8.0).
* Media & Attachments: Coil Compose (2.7.0), Native MediaRecorder & MediaPlayer, FileProvider.
* Serialization: kotlinx-serialization-json.

## 3. Package & Directory Map
* data/local/:
  * AppDatabase.kt (Room Database instance)
  * DataBackupManager.kt (SAF JSON Engine)
  * dao/: PlantAssetDao, JournalLogDao, DiySupplyDao, MasterDao, MeasurementToolDao, CustomFieldDao
  * entity/: PlantAssetEntity, JournalLogEntity, DiySupplyEntity, MasterEntities, MeasurementToolEntity, CustomFieldEntities
* data/repository/: JournalRepository.kt, SupplyRepository.kt
* ui/:
  * assets/: AddAssetScreen, AddAssetViewModel, AssetsViewModel
  * components/: CustomFieldComponents, HarvestActivityCard, InlineAudioPlayer, LocationSelectionBottomSheet, MediaAttachmentStrip, MemorySheets, PlanForaBottomBar, SharedComponents, StringPickerSheet
  * logs/: CalendarComponents, LogComponents, LogsViewModel
  * navigation/: Screen.kt (Sealed class routes)
  * profile/: ProfileViewModel, LabAnalyticsManager
  * screens/: LogsScreen, AssetsScreen, SuppliesScreen, NewLogEntryScreen, PlantDetailScreen, SupplyDetailScreen, DiyLogsScreen, ProfileScreen
  * supplies/: AddSupplyScreen, AddSupplyViewModel, SuppliesViewModel
* util/: TimeFormatter.kt

## 4. Coding & Styling Conventions
* UI Theme: PlanForaTheme located in ui/theme/. Palette tokens defined in Color.kt.
* UI State: Collect state safely with collectAsStateWithLifecycle() in composables.
* Database Rules: All Room migrations must preserve user offline data.