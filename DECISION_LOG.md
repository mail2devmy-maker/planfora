# PlanFora Technical Blueprint & Decision Log

## 1. Project Overview & Philosophy

### Core Mission
PlanFora is a high-fidelity, open-ended botanical notebook designed for advanced horticultural tracking, trial logging, and DIY supply management. Unlike standard gardening apps that rely on rigid, pre-defined inventory lists, PlanFora prioritizes the "Experimental Method"—giving users the tools to log specific variables, observations, and timelines in a local-first environment.

### Design Principles
- **Flexibility Over Rigidity**: Reject hardcoded drop-downs for "Health" or "Growth Stage." Instead, use **Dynamic Tags** and **Key-Value Parameters** to let the user define the metrics that matter for their specific setup.
- **Local-First Reliability**: All data is persisted in a Room Database to ensure zero-latency logging and offline capability in the field or greenhouse.
- **Contextual History**: Every Plant Asset is more than a record; it is a container for its own chronological log history.

---

## 2. Documentation & Design References

The project follows a structured documentation approach to ensure architectural consistency:

*   **`docs/ARCHITECTURE.md`**: Details the Clean Architecture implementation (Data, Domain, UI layers) and Reactive Data Flow (Room -> Repository -> Flow -> ViewModel -> UI).
*   **`app/schemas/`**: Version-controlled Room schema JSONs used for data integrity validation and migration testing.
*   **`docs/DESIGN.md`**: Defines the "Forest & Soil" design system, using **ForestGreen** (#228B22), **SageGreen** (#8FBC8F), and **DarkBackground** (#121212) tokens with a 12dp corner rounding standard.
*   **`docs/design_reference/logs_calendar/`**: Visual HTML/PNG blueprints for the Horizontal Scroller and Month-View calendar components.
*   **Backup Schema**: JSON-based export format containing `version`, `exportTimestamp`, and lists of `plants`, `logs`, and `supplies` entities. Uses Android SAF for file I/O.

---

## 3. Database Architecture (Room DB v7)

The schema is optimized for a flat, high-performance local storage model.

### PlantAssetEntity
Master record for individual plants or nursery groups.
- `id`: Long (Primary Key)
- `name`: String (e.g., "Ficus Bonsai")
- `category`: String (Tree, Crop/Veggie, Seedling, Cutting)
- `tags`: String (Serialized: "#Indoor,#Perennial")
- `locationNote`: String (e.g., "North Rack, Row 2")
- `acquisitionDate`: Long (Timestamp)
- `plantedDate`: Long (Timestamp)
- `totalLogsCount`: Int (Counter for UI badges)
- `lastActionDate`: Long (Timestamp of latest log)
- `zones`: String (NEW: Comma-separated zone names, e.g., "Row 1,Row 2")

### JournalLogEntity
Rich log entries for trials, feeding, or maintenance.
- `id`: Long (Primary Key)
- `assetId`: Long (Foreign Key to PlantAssetEntity.id, Nullable for general logs)
- `title`: String
- `note`: String (Multi-line text)
- `timestamp`: Long
- `tags`: String (Serialized tags)
- `parameters`: String (Serialized Key-Value pairs, e.g., "EC:1.2|pH:6.5")
- `imageUris`: String (Comma-separated internal file paths for photo attachments)
- `activityType`: String (e.g., "Pruning", "Feeding", "Feeding")
- `parentLogId`: Long? (Reference to parent log for follow-ups)
- `supplyId`: Long? (Optional reference to associated DIY/Store supply)
- `customInputName`: String? (Free-text for one-off inputs)
- `photoPath`: String?
- `audioFilePath`: String?
- `targetZones`: String? (NEW: Comma-separated zones targeted by this activity)

### MasterLocationEntity
Global list of validated locations/zones.
- `name`: String (Primary Key)

### MasterTagEntity
Global list of custom tags for assets and logs.
- `name`: String (Primary Key)

---

## 4. Decision Timeline & Changelog

- **[2026-09-01] Foundation Layer**: Setup Room DB v1 with initial entities. Established Repository pattern and Flow-based state updates.
- **[2026-09-01] DIY Batching Logic**: Implemented `getMaxBatchNumber` DAO query to support auto-incrementing batch names (e.g., "Compost Tea #3").
- **[2026-09-02] Experiment Editor Refactor**: Replaced the constrained `AddLogBottomSheet` with the full-screen `NewLogEntryScreen`. Introduced dynamic FlowRow tags and parameter map entry to accommodate technical logging.
- **[2026-09-02] Production-Ready Database Migrations**:
    - Replaced `fallbackToDestructiveMigration()` with explicit Room migrations to protect user data.
    - Implemented `MIGRATION_1_2` (Journal Log schema expansion) and `MIGRATION_2_3` (Asset profile refactor).
    - Established automated migration testing using `MigrationTestHelper` and schema versioning.
- **[2026-09-02] Supplies Module Implementation**:
    - Completed Supplies UI with DIY auto-batching and maturity badging.
    - Added category filtering (All, DIY Ferments, Substrates, Amendments).
    - Integrated target maturity date tracking for fermentation-based supplies.
- **[2026-09-02] Logs Calendar & Date Filtering**:
    - Implemented Logs calendar date filtering and event dot indicators.
    - Added state-driven filtering for Month, Week, and Day view modes.
    - Created reusable `CalendarDateStrip` component with event awareness.
- **[2026-09-02] Journal Log Image Attachments**:
    - Expanded `JournalLogEntity` (DB v4) to support multi-photo attachments.
    - Integrated Android Photo Picker and internal storage persistence for media.
    - Implemented image carousels in Log cards and full-screen preview support.
- **[2026-09-02] Analytics & Lab Insights Dashboard**:
    - Added Profile Lab Insights dashboard with tag frequency analysis and DIY ferment summaries.
    - Implemented `LabAnalyticsManager` for cross-entity metric calculation.
    - Integrated visual progress bars for top used experiment tags.
- **[2026-09-02] Calendar Refactor & Time Formatting**:
    - Implemented dynamic layout switching for Month, Week, and Day calendar modes.
    - Added user preference for 12h/24h time formatting across the application.
    - Created `TimeFormatter` utility to centralize temporal display logic.
    - Introduced hourly timeline view for detailed daily log inspection.
- **[2026-09-02] Activity-Centric Logging & Media Refactor**:
    - Upgraded `JournalLogEntity` (DB v5) with `activityType`, `parentLogId`, and `audioFilePath`.
    - Refactored `NewLogEntryScreen` with predefined Activity chips and Unified Attachments Card.
    - Implemented long-press calendar interactions for precise temporal logging.
    - Established Parent-Child log linking for longitudinal experiment tracking.
- **[2026-09-02] Optional Supply Linking**:
    - Refactored `NewLogEntryScreen` to allow optional linking of logs to database Supplies.
    - Implemented `supplyId` and `customInputName` (DB v6) for hybrid inventory tracking.
    - Added smart auto-fill logic to set `activityType` based on supply selection.
    - Implemented dynamic UI expansion for Pest Control activities with dosage, dilution, and method inputs.
    - Added automated title generation for treatment logs to enhance record consistency.
- **[2026-09-02] Activity Visualization & Inline Media**:
    - Implemented nested Activity Threads with visual timeline connectors and time-delta badging.
    - Integrated inline `MediaPlayer` for voice note playback within Log cards.
    - Established lifecycle-aware audio handling using `DisposableEffect`.
- **[2026-09-02] Local Data Export & Import**:
    - Implemented `DataBackupManager` for JSON-based database serialization.
    - Integrated Android Storage Access Framework (SAF) for secure local backup/restore.
    - Updated Profile screen with "Data & Storage" management tools.
- **[2026-09-02] Pest Control A.I. Metadata**:
    - Expanded `DiySupplyEntity` (DB v7) to support Active Ingredient (A.I.) and Concentration tracking.
    - Added contextual A.I. hints in `NewLogEntryScreen` to display chemical specifications during field logging.
    - Implemented automated title appending for A.I. details to ensure experimental traceability.
- **[2026-09-02] Pest Control Field Refactor**:
    - Reordered `Treatment Details` to appear directly under `Activity Type` for improved visibility and reduced scrolling.
    - Fixed `Unit` and `Volume` selector text clipping by enforcing single-line constraints and adjusting field weights/widths.
- **[2026-09-02] Layout Hierarchy & Visual Density Polish**:
    - Reorganized field order to match natural notebook logging flow.
    - Grouped related inputs into compact laboratory cards with MaterialTheme outline variant borders.
    - Consolidated dosage units and ratios into a single dropdown to reduce horizontal clutter.
    - Tightened spatial hierarchy and typography across the screen.
- **[2026-09-03] Asset Categories, Tag Architecture & Hints**:
    - Refactored `AssetCategory` enum with structured domain-specific categories and icons.
    - Enhanced `AddAssetBottomSheet` with single-select chips, explicit location hints, and live ID preview.
    - Upgraded Asset Picker in `NewLogEntryScreen` to a full searchable Bottom Sheet with category filtering.
    - Integrated inline hints for location and tagging to improve data entry consistency.
- **[2026-09-03] Modular Memento-Style Assets & Memory Sheets**:
    - Refactored Asset Creation to a modular, progressive-disclosure UI (Memento style).
    - Implemented 2-tap `ModalBottomSheet` pickers for Locations and Tags with master list persistence.
    - Introduced `AddAssetViewModel` for dynamic field state management and category-based suggestions.
    - Updated Asset Picker in `NewLogEntryScreen` with location filtering and enhanced visual cards.
- **[2026-09-03] Permanent Asset Notes, Physical ID Renaming & Spacing Polish**:
    - Added permanent Asset Notes field to the core creation form.
    - Renamed "Tree Tag #" to "Physical ID / Tree #" for improved clarity.
    - Compressed vertical spacing for dynamic optional fields to 6dp.
    - Refined PlantAssetEntity with a dedicated notes field (DB v9).
- **[2026-09-03] Supplies Module & PHI Countdown System**:
    - Expanded DiySupplyEntity with formulation types, PHI days, and stock tracking (DB v10).
    - Implemented AddSupplyScreen with modular optional fields and formulation code support.
    - Integrated flexible dosage units (free-text) into NewLogEntryScreen treatment ribbon.
    - Added automated PHI expiry calculation and countdown badges to the logs timeline.
- **[2026-09-03] Assets Library, Asset Passport & Timeline Filters**:
    - Refactored Assets Tab with location-grouped sections and search functionality.
    - Built "Asset Passport" (AssetDetailScreen) featuring deep metadata display and linked log history.
    - Integrated global location and active PHI filters into the main Logs Tab timeline.
    - Optimized UI cards with category icons and persistent Physical ID visibility.
- **[2026-09-03] Batch Logging & Bulk Asset Actions**:
    - Implemented multi-select logic in Assets Tab with long-press activation.
    - Added "Select All in Zone" functionality to location headers.
    - Built batch log generation system sharing a common `batchGroupId` (DB v11).
    - Refactored NewLogEntryScreen to support multi-asset chip UI and bulk insertion.
    - Added visual indicators for batch logs in the journal timeline.
- **[2026-09-03] Compact Supply Form & Asset Category Graduation**:
    - Refactored AddSupplyScreen with a horizontal layout for Form/Type and Formulation Code.
    - Reduced vertical spacing in supply creation form to improve information density.
    - Implemented "Asset Graduation" feature in PlantDetailScreen with automated milestone logging.
    - Added support for promoting assets between categories (e.g., Seedling ➔ Tree) with historical tracking.
- **[2026-09-03] Dynamic Activity Forms & Asset-Aware Auto Titles**:
    - Refactored NewLogEntryScreen to dynamically switch input cards based on Activity Type (Harvest, Repotting, Pruning, etc.).
    - Implemented Asset-Aware Auto-Title logic: `[Asset] • [Activity] • [Key Parameter]`.
    - Compacted attachment actions into a horizontal strip and integrated the 2-tap Tag Memory Sheet.
    - Migrated raw Dynamic Parameters into an optional progressive-disclosure memento chip.
- **[2026-09-03] IME Window Insets & Parameter Bottom Sheet**:
    - Implemented `imePadding` on NewLogEntryScreen to prevent keyboard occlusion of the notes field.
    - Refactored dynamic parameter entry to use a ModalBottomSheet (`ParameterBottomSheet.kt`).
    - Rendered active parameters as compact, removable chips using a horizontal FlowRow.
- **[2026-09-03] In-Context Dynamic Parameters & Scroll Headroom**:
    - Refactored dynamic parameter entry to use an inline expandable section instead of a ModalBottomSheet.
    - Integrated `BringIntoViewRequester` to ensure inline input fields are automatically scrolled into the viewport upon expansion or focus.
    - Optimized scroll container by removing fixed large bottom spacers in favor of dynamic `imePadding()` and precise content padding.
    - Ensured "Save Activity Log" button remains the final element with zero artificial dead space.
- **[2026-09-03] Compact Asset Form & Memento Custom Field Engine**:
    - Refactored AddAssetScreen with side-by-side Location and Tag triggers to reduce vertical height.
    - Added a horizontal Media Action Strip to assets for direct Camera/Gallery/Audio attachments.
    - Implemented a category-aware Custom Field Engine (DB v12) allowing user-defined fields (Text, Number, Radio).
    - Introduced CustomFieldDefinitionEntity and CustomFieldValueEntity for flexible asset metadata.
- **[2026-09-03] Category-Aware Field Relevance Matrix**:
    - Implemented dynamic field visibility rules in AddAssetScreen and PlantDetailScreen based on Asset Category.
    - Surface specific core fields by category: Seedlings (Batch/Tray ID, Qty), Cuttings (Mother Plant Link, Propagated Date), Trees (Physical ID, GPS, Rootstock).
    - Refined CustomFieldCreatorDialog to support Global fields and strict category scoping.
    - Enhanced dynamic field palette to hide irrelevant metrics based on the active lifecycle stage.
- **[2026-09-03] High-Speed Tag Sheet & 1-Tap Creation**:
    - Refactored TagPickerSheet in MemorySheets.kt with a unified search-and-create pattern.
    - Integrated inline tag creation directly into the search bar, eliminating secondary dialogs.
    - Implemented real-time selection commit to ensure zero-friction metadata management.
- **[2026-09-03] Universal High-Speed Tag Memory Sheet**:
    - Standardized TagPickerSheet across both AddAssetScreen.kt and NewLogEntryScreen.kt.
    - Removed legacy multi-tap tag selection dialogs from the Asset module.
    - Unified the tag creation workflow to use the single-tap search-and-create pattern project-wide.
- **[2026-09-03] Domain-Aware Supply Module & Custom Fields**:
    - Refactored Supply management to support specialized chemical categories (Insecticides, Fungicides, etc.).
    - Surface prominent safety fields (Active Ingredient, PHI) and stock tracking directly in the core form.
    - Integrated Media Action Strip for capturing product labels and audio application instructions.
    - Implemented category-aware Memento custom field engine for Supplies, enabling user-defined product metadata.
- **[2026-09-03] Row/Zone Partial Harvesting & PHI Isolation**:
    - Implemented sub-asset row/zone partitioning for Crops, Veggies, and Seedlings (DB v13).
    - Added granular PHI safety isolation allowing row-specific treatment lockouts.
    - Built multi-row harvest yield breakdown and aggregation engine.
    - Integrated Yield & Compliance safety dashboard into the Asset Passport.
- **[2026-09-03] Fix Harvest Card Row-Level Rendering**:
    - Refactored HarvestActivityCard to allow granular selection of harvested rows.
    - Replaced blanket per-row inputs with a zone-selector and dynamic individual yield fields.
    - Optimized yield aggregation to only sum selected rows.
- **[2026-09-03] Standardized Option A Metric Chips & Ledger**:
    - Introduced unified `ParameterInputSection.kt` shared component.
    - Replaced raw key-value parameter inputs in `NewLogEntryScreen.kt` with a quick-add chip bar and ledger rows.
    - Refactored `AddAssetScreen.kt` and `AddSupplyScreen.kt` to use the same chip-based palette and compact ledger cards for custom metadata.
    - Unified the UI/UX for all dynamic parameter entry project-wide.
- **[2026-09-04] Slate & Forest Emerald Design System**:
    - Overhauled the global palette to a professional high-density "Slate & Forest Green" look.
    - Implemented a deep obsidian background (`#0B121C`) with cool slate-charcoal cards (`#131E2B`) and subtle structural borders (`#1E2D3D`).
    - Standardized crisp off-white (`#F1F5F9`) for primary text and headers with cool gray (`#94A3B8`) for secondary metadata.
    - Refined vertical spacing to 6dp-8dp and card padding to 10dp for a high-density "laboratory notebook" feel.
    - Restricted primary forest emerald (`#10B981`) accent strictly to interactive states and primary CTAs.
- **[2026-09-04] Fix Default Asset Date Initialization and Tag Parsing**:
    - Resolved issue where propagation and harvest dates were auto-generated on new assets.
    - Updated `AddAssetViewModel` to default date state variables to `null`.
    - Implemented conditional tag generation: date tags are only created if the user selects a specific date.
    - Fixed tag splitting bug by switching to a space-and-comma-free format (`yyyy-MM-dd`) for internal metadata tags.
- **[2026-09-04] Streamlined Formulation and Smart Unit Defaults**:
    - Refactored Product Passport in `SupplyDetailScreen.kt` to show unified "Formulation" field combining code and type.
    - Implemented smart unit auto-defaults in `AddSupplyViewModel.kt` based on physical form and formulation codes.
    - Standardized liquid defaults to "L" and solid/powder defaults to "kg".
- **[2026-09-04] Scoped Metadata Suggestions by Module**:
    - Refactored `MasterLocationEntity` and `MasterTagEntity` to include a `scope` field (ASSET, SUPPLY, GLOBAL).
    - Updated `MasterDao` with scoped queries to filter suggestions based on the active module context.
    - Implemented `MIGRATION_15_16` to update master tables with the new schema and composite primary keys.
    - Scoped location and tag selection in `AddAssetViewModel` and `AddSupplyViewModel` to prevent cross-module UI clutter.
- **[2026-09-04] Fix Location Chip Long-Press Gesture Detection**:
    - Replaced Material 3 Chips with custom `Surface` + `combinedClickable` in `LocationSelectionBottomSheet`, `TagPickerSheet`, and `StringPickerSheet`.
    - Restored responsive long-press rename/delete actions by preventing chip-level pointer consumption.
    - Standardized haptic feedback and visual state across all metadata pickers.
- **[2026-09-04] Remove Mockup Data Seeders for Production Testing**:
    - Stripped out `AppDatabaseCallback` and dummy data seeder logic from `AppDatabase.kt`.
    - Cleaned up `MainActivity.kt` dependency on database coroutine scopes for initialization.
    - Implemented professional empty-state placeholders for Assets and Supplies screens.
    - Verified cross-module empty-state handling to ensure graceful fresh-start usability.
- **[2026-09-04] Universal Long-Press Metadata Management**:
    - Created `StringPickerSheet.kt` reusable component for single-select metadata management.
    - Upgraded `ParameterInputSection.kt` to support long-press rename/delete for metrics (pH, EC, etc.).
    - Unified long-press management across Tags, Locations, Active Ingredients, and Metrics.
    - Implemented cascading database transactions for all manageable metadata strings.
    - Migrated Active Ingredient input to the high-speed managed sheet pattern.
- **[2026-09-04] Importance-Based Field Hierarchy**:
    - Implemented a visual hierarchy for form fields to differentiate between mandatory and optional inputs.
    - Updated `PlanForaSurfaceCard` and `PlanForaFieldGroup` to support `isImportant` parameter.
    - Mandatory fields use `#131E2B` (Fill), `#2A3B50` (Border), and `#F1F5F9` (Labels).
    - Optional fields use `#0E1622` (Fill), `#182332` (Border), and `#94A3B8` (Muted Labels).
    - Consolidated text field styling into a shared `planForaTextFieldColors` helper with `#10B981` (Forest Emerald) focused state.
    - Propagated the hierarchy across `NewLogEntryScreen`, `AddAssetScreen`, and `AddSupplyScreen`.
- **[2026-09-04] Tag Rename & Delete Management**:
    - Upgraded `TagPickerSheet` in `MemorySheets.kt` with long-press management actions.
    - Implemented `updateTagName` and `deleteTagByName` in `MasterDao` and `JournalRepository`.
    - Integrated rename and delete confirmation dialogs into the tag selection workflow.
    - Synchronized tag management across Assets, Supplies, and New Log Entry modules.
- **[2026-09-04] Location Rename & Delete Management**:
    - Built `LocationSelectionBottomSheet.kt` reusable component with long-press management actions.
    - Implemented `updateLocationName` and `deleteLocationByName` in `MasterDao` and `JournalRepository`.
    - Added rename and delete confirmation dialogs to the location picker workflow.
    - Unified location selection across Assets and Supplies modules using the new shared component.
- **[2026-09-04] Live Autocomplete Dropdown for Active Ingredients**:
    - Refactored `activeIngredient` input in `AddSupplyScreen.kt` to use `ExposedDropdownMenuBox`.
    - Implemented `filteredActiveIngredients` in `AddSupplyViewModel` using `combine` for live query-based filtering.
    - Replaced static chips with a dynamic, focus-aware dropdown menu that surfaces historical chemical entries as the user types.
- **[2026-09-04] Active Ingredient Autocomplete & Suggestion Chips**:
    - Implemented a distinct Room query in `DiySupplyDao` to retrieve historical active ingredients.
    - Added `activeIngredientSuggestions` Flow to `AddSupplyViewModel` to expose unique chemical names.
    - Integrated a horizontal `LazyRow` of compact suggestion chips in `AddSupplyScreen.kt` for 1-tap input.
- **[2026-09-04] Standardize Media Attachment Strip Across Assets and Supplies**:
    - Propagated `MediaAttachmentStrip.kt` to `AddAssetScreen.kt` and `AddSupplyScreen.kt`.
    - Expanded `PlantAssetEntity` and `DiySupplyEntity` to support `imageUris` and `audioPath` (DB v14).
    - Updated `AddAssetViewModel` and `AddSupplyViewModel` with state binding and persistence for media.
    - Standardized the vertical layout hierarchy for attachments project-wide.
- **[2026-09-04] Calendar Month Navigation & Date Filtering**:
    - Enhanced `MonthCalendarView` with `HorizontalPager` for smooth swipe navigation between months.
    - Added Month/Year navigation header with arrow controls and explicit month selection.
    - Implemented dynamic log filtering by `selectedDate`, showing only logs for the active calendar day.
    - Added interactive log card clicks and an empty state placeholder for days with no activity.
- **[2026-09-04] Voice Note Card Internal Layout Polish**:
    - Refactored `AudioPreviewCard` into a two-tier layout (Header and Controls rows).
    - Decoupled `InlineAudioPlayer` from its internal `Surface` to allow better integration in complex cards.
    - Resolved timestamp overlapping and text clipping by implementing proper weights and padding.
- **[2026-09-04] FileProvider XML Config & Vertical Attachment Layout Fix**:
    - Fixed `IllegalArgumentException` by adding `files-path` for `Pictures/` in `file_paths.xml`.
    - Refactored `MediaAttachmentStrip.kt` to stack the voice note card vertically below the image thumbnail row.
    - Improved layout hierarchy to prevent visual overlap between different media types.
- **[2026-09-04] Camera URI Fix & Voice Card Layout Polish**:
    - Fixed camera thumbnail rendering by migrating to `ActivityResultContracts.TakePicture()` with `FileProvider`.
    - Implemented `rememberSaveable` for `tempCameraUri` to ensure URI persistence across configuration changes.
    - Refactored `AudioPreviewCard` and `InlineAudioPlayer` to resolve layout overflows and cramped text.
- **[2026-09-04] Amplitude Waveform Visualizer & Voice Redo Flow**:
    - Implemented real-time `maxAmplitude` sampling (100ms) and dynamic waveform rendering using Compose `Canvas`.
    - Added a Redo/Replacement workflow with an `AlertDialog` to prevent accidental overwrites of existing voice notes.
    - Enhanced `AudioRecordingHUD` with normalized amplitude spikes (0dp - 24dp).
- **[2026-09-04] MediaRecorder Crash Fix & Audio Lifecycle Refactor**:
    - Resolved `setAudioSource failed` crash by implementing API-level `MediaRecorder` instantiation (API 31+ context constructor).
    - Integrated runtime `RECORD_AUDIO` permission guards and manifest declaration.
    - Refactored recording lifecycle with `DisposableEffect` cleanup to prevent native leaks.
- **[2026-09-03] Standardized MediaAttachmentStrip & Voice Recording Controls**:
    - Built reusable `MediaAttachmentStrip.kt` with integrated Camera, Gallery, and Voice recording.
    - Implemented active recording HUD with animated pulse, live timer, and Discard/Cancel logic.
    - Unified media attachment UI across Logs, Assets, and Supplies modules.
- **[2026-09-03] Calendar Today Marker & Location Tag Picker**:
    - Added real-world "Today" indicator to Month and Week calendar views.
    - Standardized Supply Storage Location to use the 1-tap Tag Memory Sheet.
    - Integrated Master Location persistence for supply inventory.
- **[2026-09-03] Full CRUD Wiring & Device Testing Polish**:
    - Implemented full Edit and Delete functionality for Assets, Supplies, and Journal Logs.
    - Added overflow menus with CRUD actions to `PlantDetailScreen`, `SupplyDetailScreen`, and timeline log cards.
    - Wired `AddAssetScreen` and `AddSupplyScreen` to support pre-filling data for updates via `loadAsset`/`loadSupply` logic.
    - Integrated safe deletion confirmation dialogs for all destructive actions.
    - Decremented asset log counts upon log deletion to maintain data integrity.

## [v10.10 - Fix Default Asset Date Initialization and Tag Parsing]

### Overview
Addressed a bug where new plant assets were being saved with unwanted default propagation and harvest date tags. Refactored the date handling logic to ensure these fields are truly optional and that their metadata is stored in a robust, parseable format.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt`: Refactored date states to `Long?` and updated `saveAsset` logic.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: Updated `DatePickerField` components to handle nullable values and provide a "Clear" action.
- `DECISION_LOG.md`: Documented the initialization and parsing fixes.

### Bug Fixes & Improvements
- **Optional Date Initialization**: Changed the default state for `plantedDate`, `acquisitionDate`, `propagatedDate`, and `expectedHarvestDate` from the current timestamp to `null`. This prevents the system from assuming today's date for every new asset.
- **Conditional Metadata**: Metadata tags (e.g., `PropDate:`, `ExpHarv:`) are now only appended to the asset's tag string if the user has explicitly interacted with the date picker and selected a value.
- **Robust Tag Formatting**: Fixed a bug where dates like "Sep 06, 2026" were being split into multiple tags (e.g., `#PropDate:Sep 06` and `#2026`) due to the presence of commas and spaces. The internal tag format now uses hyphens (e.g., `PropDate:Sep-06-2026`) to ensure each date is treated as a single atomic token.
- **Enhanced Date Pickers**: Added a "Clear" button to the date picker dialogs, allowing users to remove a previously selected date if they change their mind.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddAssetViewModel.kt` and `AddAssetScreen.kt` to their v10.9 states.
2. **State Management**:
   - No database schema changes. Assets saved with the new tag format will still be readable, but may require the old parsing logic to handle the hyphenated strings if reverted.

## [v10.9 - Streamlined Formulation and Smart Unit Defaults]

### Overview
Improved the Product Passport and supply entry experience by streamlining formulation display and introducing smart defaults for measurement units. This reduces manual entry effort and ensures data consistency across the supply inventory.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/screens/SupplyDetailScreen.kt`: Refactored metadata grid to show unified formulation data.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyViewModel.kt`: Implemented auto-default logic for stock units.
- `DECISION_LOG.md`: Documented formulation and unit default enhancements.

### UI & Architecture Changes
- **Unified Formulation Field**: Replaced the split "Form / Type" display with a single "Formulation" field in the Product Passport. If a technical code (like `WP` or `EC`) exists, it is displayed alongside the physical type (e.g., `WP (Powder)`), providing a more professional and descriptive summary.
- **Smart Unit Auto-Defaults**: Selecting a physical form or formulation code in the supply entry form now automatically updates the default unit. Liquids default to `L`, while powders, granules, and solids default to `kg`. Users can still manually override these defaults if needed.
- **Improved Data Entry Speed**: By linking form types to logical units, the application anticipates the user's needs, reducing the number of taps required to create or update a product profile.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `SupplyDetailScreen.kt` and `AddSupplyViewModel.kt` to their v10.8 states.
2. **State Management**:
   - No database schema changes. Existing units saved will remain unchanged upon rollback.

## [v10.8 - Scoped Metadata Suggestions by Module]

### Overview
Introduced a module-scoped metadata system to differentiate between Assets and Supplies suggestions. This prevents UI clutter by ensuring that locations and tags created in the Asset module do not pollute the Supply module's pickers, and vice versa, while still supporting shared "Global" entries.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/MasterEntities.kt`: Added `scope` field and updated Primary Keys.
- `app/src/main/java/com/mail2dev/planfora/data/local/dao/MasterDao.kt`: Implemented scoped queries and cascading updates.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented version to 16; added `MIGRATION_15_16`.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt` & `AddSupplyViewModel.kt`: Updated to use scoped metadata retrieval.
- `DECISION_LOG.md`: Documented metadata scoping implementation.

### Architecture Changes
- **Database Schema v16**: Migrated `master_locations` and `master_tags` to use a composite primary key `(name, scope)`. This allows the same name to exist in different scopes if necessary.
- **Contextual Retrieval**: Pickers now use `getLocationsByScope(scope)` and `getTagsByScope(scope)` to retrieve relevant suggestions. The "GLOBAL" scope is always included in results for project-wide availability.
- **Scoped Management**: Rename and delete operations are now scope-aware, ensuring that managing an Asset tag doesn't unintentionally modify a Supply tag with the same name.

### Rollback Instructions
1. **Code Reversion**:
   - Revert ViewModels and `MasterDao.kt` to their v10.7 states.
   - Revert `MasterEntities.kt` (remove `scope` field and restore single PK).
2. **Database Migration**:
   - Reverting to v15 will require dropping the new columns and restoring the original table structure. 
   - Note: Data in the `scope` column will be lost upon rollback.

## [v10.7 - Remove Mockup Data Seeders for Production Testing]

### Overview
Prepared the application for authentic user testing by removing all hardcoded mockup and sample data. The application now starts with a completely clean database foundation, providing a "fresh start" experience for growers.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Removed `AppDatabaseCallback` and `populateDatabase` seeder.
- `app/src/main/java/com/mail2dev/planfora/MainActivity.kt`: Simplified database instantiation.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/AssetsScreen.kt`: Added "No assets found" empty state placeholder.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/SuppliesScreen.kt`: Added "No supplies found" empty state placeholder.
- `DECISION_LOG.md`: Documented transition to clean production state.

### UI & UX Improvements
- **Graceful Empty States**: Replaced blank screens with professional, actionable empty-state views. When no data is present, users are greeted with clear icons and instructions on how to begin adding plants or managing inventory.
- **Production Readiness**: Eliminated confusing sample records (like "Ficus Bonsai") that could interfere with real agricultural data entry during field testing.
- **Improved Performance**: Simplified the database initialization pipeline by removing redundant `onCreate` callbacks.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AppDatabase.kt` and `MainActivity.kt` to their v10.6 states to re-enable automatic data seeding.
   - Revert `AssetsScreen.kt` and `SuppliesScreen.kt` if empty state placeholders are no longer desired.
2. **State Management**:
   - Clearing app storage on a device will trigger a fresh initialization. If seeders are restored, the database will be re-populated upon the next clean install.

## [v10.6 - Universal Long-Press Metadata Management]

### Overview
Standardized the management of ALL selectable metadata (Tags, Locations, Active Ingredients, and Metrics) by implementing a unified long-press gesture system. This ensures that renaming or deleting any saved metadata value executes a cascading update across all linked entities in the database, maintaining absolute data integrity.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/MasterEntities.kt`: Added `MasterIngredientEntity` and `MasterParameterEntity`.
- `app/src/main/java/com/mail2dev/planfora/data/local/dao/MasterDao.kt`: Implemented cascading transactions for Ingredients and Parameters.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented to version 17; added `MIGRATION_16_17` with seed data.
- `app/src/main/java/com/mail2dev/planfora/ui/components/StringPickerSheet.kt`: New reusable component for single-select management.
- `app/src/main/java/com/mail2dev/planfora/ui/components/ParameterInputSection.kt`: Upgraded for long-press metric management.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Migrated to the managed Active Ingredient sheet.
- `DECISION_LOG.md`: Documented universal metadata management.

### Key Implementation Details
- **Cascading Transactions**: Renaming a metadata string (e.g., changing "pH" to "pH Level" or "Neem" to "Neem Oil") now updates every single occurrence in `plant_assets`, `journal_logs`, and `diy_supplies` automatically via SQLite triggers/transactions.
- **Unified Interaction Pattern**: Every suggestion chip and picker item now uses `combinedClickable`. Users can tap to select and long-press to manage, provided with haptic feedback confirmation.
- **Metric Hygiene**: Users can now clean up their "Quick-Add" metric bars by deleting obsolete parameters or renaming them for better clarity.
- **Managed Active Ingredients**: Replaced the autocomplete dropdown with the high-speed `StringPickerSheet` pattern, enabling professional management of the chemical inventory database.

### Rollback Instructions
1. **Code Reversion**:
   - Revert ViewModels, Screens, and Daos to their v10.5 states.
   - Delete `StringPickerSheet.kt`.
2. **Database Migration**:
   - Reverting to v16 will ignore the new `master_ingredients` and `master_parameters` tables.
3. **Instructional Note**: Revert `AppDatabase.kt` version to `16` and remove `MIGRATION_16_17`.

## [v10.10 - Fix Default Asset Date Initialization and Tag Parsing]

### Overview
Standardized the management of custom tags by introducing rename and delete capabilities directly within the tag selection bottom sheet. This allows users to keep their experimental metadata organized and correct typos or obsolete tags without leaving the creation forms.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/dao/MasterDao.kt`: Added update and delete queries for tags.
- `app/src/main/java/com/mail2dev/planfora/data/repository/JournalRepository.kt`: Exposed tag management methods.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MemorySheets.kt`: Upgraded `TagPickerSheet` with long-press support and management dialogs.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt`, `AddSupplyViewModel.kt`, & `LogsViewModel.kt`: Implemented tag persistence and state synchronization logic.
- `DECISION_LOG.md`: Documented tag management enhancements.

### UI & UX Improvements
- **Long-Press Activation**: Users can long-press any tag chip in the selection sheet to trigger the management menu.
- **Experimental Hygiene**: Easily correct technical terms or chemical names used in tags across the entire project.
- **Safe Rename**: Renaming a tag updates the master list and automatically refreshes the selection state in the current form.
- **Confirmation Guards**: Added explicit confirmation for deletions to prevent accidental loss of frequently used metadata categories.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `MemorySheets.kt`, ViewModels, and Screens to their v10.2 states.
   - Remove `updateTagName` and `deleteTagByName` from `MasterDao.kt` and `JournalRepository.kt`.
2. **State Management**:
   - No database schema changes. Master tags modified will remain in their updated states in `master_tags`.

## [v10.2 - Location Rename & Delete Management]

### Overview
Enhanced the location management system by introducing rename and delete capabilities directly within the selection workflow. This allows users to maintain a clean master location list without navigating to a separate settings screen.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/dao/MasterDao.kt`: Added update and delete queries for locations.
- `app/src/main/java/com/mail2dev/planfora/data/repository/JournalRepository.kt`: Exposed new location management methods.
- `app/src/main/java/com/mail2dev/planfora/ui/components/LocationSelectionBottomSheet.kt`: New shared component supporting long-press management.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt` & `AddAssetViewModel.kt`: Migrated to the new shared sheet and implemented management logic.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt` & `AddSupplyViewModel.kt`: Migrated to the new shared sheet for storage locations.
- `DECISION_LOG.md`: Documented location management enhancements.

### UI & UX Improvements
- **Long-Press Management**: Users can now long-press any location chip in the bottom sheet to reveal management actions (Rename/Delete).
- **Inline Renaming**: An `AlertDialog` with a text field allows for instant renaming of stored locations, which automatically updates the master list and active selection.
- **Safe Deletion**: Integrated a confirmation dialog for deletions to prevent accidental removal of important zone identifiers.
- **Standardized Component**: Unified the location picker experience across all modules, ensuring consistent behavior for both plant assets and supply storage.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddAssetScreen.kt`, `AddAssetViewModel.kt`, `AddSupplyScreen.kt`, `AddSupplyViewModel.kt`, `JournalRepository.kt`, and `MasterDao.kt` to their v10.1 states.
   - Delete `LocationSelectionBottomSheet.kt`.
2. **State Management**:
   - No database schema changes.

## [v10.1 - Live Autocomplete Dropdown for Active Ingredients]

### Overview
Upgraded the active ingredient input experience by replacing static chips with a live-filtering autocomplete dropdown. This provides a more professional and integrated typing experience, allowing users to quickly select historical chemical data without leaving the text input workflow.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyViewModel.kt`: Added filtered suggestions logic using query-state combination.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Integrated `ExposedDropdownMenuBox` and `DropdownMenuItem` for the autocomplete UI.
- `DECISION_LOG.md`: Documented the transition to dynamic autocomplete.

### UI & UX Improvements
- **Live Filtering**: Suggestions are now filtered in real-time as the user types, matching characters anywhere in the historical chemical name (case-insensitive).
- **Exposed Dropdown Menu**: Utilized standard Material 3 `ExposedDropdownMenuBox` to ensure the autocomplete menu respects screen boundaries and focus states.
- **Context-Aware Suggestions**: The dropdown automatically collapses when a selection is made or when the input matches an existing suggestion exactly, reducing visual noise.
- **One-Tap Selection**: Tapping a dropdown entry instantly populates the active ingredient field and dismisses the menu.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddSupplyScreen.kt` and `AddSupplyViewModel.kt` to their v10.0 states.
2. **State Management**:
   - No database schema changes.

## [v10.0 - Active Ingredient Autocomplete & Suggestion Chips]

### Overview
Enhanced the supply entry workflow with a persistent suggestion system for chemical active ingredients. This eliminates redundant typing by recalling previously saved ingredients and displaying them as interactive chips directly below the input field.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/dao/DiySupplyDao.kt`: Added `getDistinctActiveIngredients` query.
- `app/src/main/java/com/mail2dev/planfora/data/repository/SupplyRepository.kt`: Exposed the suggestion query.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyViewModel.kt`: Wired historical suggestions to the UI state.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Implemented the suggestion chip row.
- `DECISION_LOG.md`: Documented the autocomplete implementation.

### UI & Architecture Changes
- **Historical Recall**: The `activeIngredient` field now queries the database for all unique values previously entered across all supplies.
- **Interactive Suggestions**: A horizontal row of compact `AssistChips` appears below the Active Ingredient field. Tapping a chip (e.g., `[ Neem Oil ]`) instantly populates the text field.
- **Real-Time Enrichment**: As users save new supplies with unique ingredients, the suggestion pool is automatically updated and becomes available for subsequent entries.
- **Responsive Layout**: The suggestion row only appears when historical data is available, maintaining a clean interface for new databases.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddSupplyScreen.kt`, `AddSupplyViewModel.kt`, `SupplyRepository.kt`, and `DiySupplyDao.kt` to their v9.9 states.
2. **State Management**:
   - No database schema changes. The `getDistinctActiveIngredients` query can be safely removed without affecting existing data.

## [v9.9 - Standardize Media Attachment Strip Across Assets and Supplies]

### Overview
Propagated the high-fidelity `MediaAttachmentStrip.kt` component across the entire application, ensuring a consistent media capture and playback experience for Assets and Supplies. This update included a database schema expansion to persist photos and voice notes for plant profiles and product inventory.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/PlantAssetEntity.kt`: Added `imageUris` and `audioPath` fields.
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/DiySupplyEntity.kt`: Added `imageUris` and `audioPath` fields.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented to version 14; added `MIGRATION_13_14`.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt`: Implemented media persistence logic.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyViewModel.kt`: Implemented media persistence logic.
- `DECISION_LOG.md`: Documented cross-module media standardization.

### UI & Architecture Changes
- **Universal Attachment Workflow**: Users can now attach multiple photos and a single voice note to any Asset or Supply, matching the rich logging capabilities of the journal module.
- **Database Schema v14**: Successfully migrated `plant_assets` and `diy_supplies` tables to include the new media columns, ensuring data parity across all master entities.
- **ViewModel Synchronization**: Wired `AddAssetViewModel` and `AddSupplyViewModel` to handle real-time URI state updates from the shared strip component, ensuring zero-latency preview and reliable save/load cycles.
- **Layout Consistency**: Enforced the vertical stack alignment (images on top, audio card below) across all entry forms to maintain design system integrity.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddAssetViewModel.kt` and `AddSupplyViewModel.kt` to their v9.8 states.
   - Revert `PlantAssetEntity.kt` and `DiySupplyEntity.kt` (remove new media fields).
2. **Database Migration**:
   - Reverting to v13 will ignore the `imageUris` and `audioPath` columns in both tables.
   - Note: Data stored in these columns will remain in SQLite but will be inaccessible to the application.
3. **Instructional Note**: Revert `AppDatabase.kt` version to `13` and remove `MIGRATION_13_14` if full rollback is required.

## [v9.8 - Calendar Month Navigation & Date Filtering]

### Overview
Significantly improved the usability of the journal calendar by introducing intuitive navigation gestures and granular date filtering. Users can now easily browse historical logs by swiping through months or jumping to specific dates.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/logs/CalendarComponents.kt`: Refactored `MonthCalendarView` to use `HorizontalPager` and added navigation header.
- `app/src/main/java/com/mail2dev/planfora/ui/logs/LogComponents.kt`: Added `onClick` support to all log card variants.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/LogsScreen.kt`: Integrated dynamic date filtering and empty state handling.
- `DECISION_LOG.md`: Documented calendar enhancements.

### UI & UX Improvements
- **Month Swipe & Arrows**: Replaced the static month view with a high-performance `HorizontalPager`. Users can swipe left/right to change months or use the new `<` and `>` arrow buttons in the calendar header.
- **Contextual Filtering**: The log list now automatically filters to show only entries from the day selected on the calendar. This prevents timeline clutter and allows for precise historical lookup.
- **Interactive Feed**: Log entries are now fully clickable, navigating directly to the edit screen for quick updates.
- **Empty State**: Introduced a professional "No logs recorded for this day" placeholder to provide clear feedback when no activity is found.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `CalendarComponents.kt` to its v9.7 state (removing `HorizontalPager` and header logic).
   - Revert `LogComponents.kt` and `LogsScreen.kt` to their v9.7 states.
2. **State Management**:
   - No database schema changes.

## [v9.8 - Voice Note Card Internal Layout Polish]

### Overview
Improved the internal spatial organization of the voice note playback card. By transitioning to a structured two-tier layout, we eliminated layout overflows and ensured that all controls and metadata are clearly visible across different device widths.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/MediaAttachmentStrip.kt`: Refactored `AudioPreviewCard` with separate header and control rows.
- `app/src/main/java/com/mail2dev/planfora/ui/components/InlineAudioPlayer.kt`: Removed redundant `Surface` to facilitate modular card integration.
- `DECISION_LOG.md`: Documented the layout refactor.

### UI Improvements
- **Two-Tier Architecture**: The voice card now splits content into a top metadata row (label + delete button) and a bottom playback control row. This prevents the "squashed" appearance seen in previous builds where all elements competed for the same horizontal line.
- **Timestamp Clarity**: Refactored the duration labels and progress bar to use flexible weights. This ensures the timer (e.g., `00:01 / 00:12`) never overlaps with the playback slider or clips against the card boundaries.
- **Enhanced Visual Hierarchy**: Increased the primary label font size and standardized the delete icon placement for better ergonomics.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `MediaAttachmentStrip.kt` and `InlineAudioPlayer.kt` to their v9.7 states.
2. **State Management**:
   - No database schema changes.

## [v9.7 - FileProvider XML Config & Vertical Attachment Layout Fix]

### Overview
Resolved a critical crash in the camera capture workflow and improved the visual hierarchy of media attachments. The voice note interface now occupies its own dedicated space, preventing layout collisions with image thumbnails.

### Modified Files
- `app/src/main/res/xml/file_paths.xml`: Added `files-path` entry for internal `Pictures/` directory.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MediaAttachmentStrip.kt`: Refactored attachment container into a vertical stack.
- `DECISION_LOG.md`: Updated with `FileProvider` fix and layout hierarchy changes.

### Bug Fixes & UI Refinements
- **FileProvider Path Resolution**: Fixed the `IllegalArgumentException` thrown during camera capture by explicitly exposing the internal `Pictures/` directory in the `FileProvider` XML configuration. This ensures the camera app has the necessary permissions to write to the app's internal storage.
- **Vertical Attachment Stacking**: Migrated from a single `LazyRow` to a vertical `Column` layout for attachments. Image thumbnails now scroll horizontally in the top section, while the voice note card sits cleanly underneath. This prevents "cramping" and ensures the audio player controls are always accessible.
- **Improved Spacing**: Enforced consistent 10dp vertical spacing between the image row and the audio card to maintain a professional, organized notebook aesthetic.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `MediaAttachmentStrip.kt` to its v9.6 state.
   - Remove the `internal_pictures` entry from `file_paths.xml`.
2. **State Management**:
   - No database schema changes.

## [v9.6 - Camera URI Fix & Voice Card Layout Polish]

### Overview
Resolved critical UI and functional bugs in the media attachment system. Fixed the camera-to-thumbnail rendering pipeline and optimized the voice note playback interface for better spatial efficiency.

### Modified Files
- `app/src/main/AndroidManifest.xml`: Registered `FileProvider` for secure camera URI sharing.
- `app/src/main/res/xml/file_paths.xml`: Defined accessible paths for the `FileProvider`.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MediaAttachmentStrip.kt`: Migrated to `TakePicture` and increased audio card width.
- `app/src/main/java/com/mail2dev/planfora/ui/components/InlineAudioPlayer.kt`: Refined progress bar weights and time label formatting.
- `DECISION_LOG.md`: Updated with camera URI and layout polish documentation.

### Bug Fixes
- **Camera Thumbnail Rendering**: Resolved the issue where camera-captured images failed to appear in the tray. By using `FileProvider.getUriForFile()` and `ActivityResultContracts.TakePicture()`, the app now correctly persists the image file and its URI, allowing `AsyncImage` to render the thumbnail immediately.
- **Voice Card Overflow**: Fixed layout collisions in the `AudioPreviewCard`. The progress bar now uses proper weights, and the playback timestamps have been compacted to prevent wrapping on smaller screens.
- **State Persistence**: Utilized `rememberSaveable` for `tempCameraUri` to prevent loss of the camera destination during device rotation or activity recreation.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `MediaAttachmentStrip.kt` and `InlineAudioPlayer.kt` to their v9.5 states.
   - Remove `FileProvider` from `AndroidManifest.xml` and delete `res/xml/file_paths.xml`.
2. **State Management**:
   - No database schema changes.

## [v9.5 - Amplitude Waveform Visualizer & Voice Redo Flow]

### Overview
Upgraded the voice recording experience with real-time visual feedback and a robust replacement workflow. The new system provides live amplitude visualization during capture and protects users from accidentally losing recorded data.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/MediaAttachmentStrip.kt`: Refactored to include waveform Canvas and replacement logic.
- `DECISION_LOG.md`: Updated with visualization and redo workflow documentation.

### New Features
- **Live Amplitude Waveform**: The recording HUD now features a dynamic visualizer that samples the microphone's amplitude 10 times per second. These samples are mapped to normalized vertical spikes, creating a scrolling waveform effect.
- **Redo Confirmation Workflow**: To prevent accidental data loss, the application now detects if a voice note is already attached when the user clicks the record button. If so, a confirmation dialog appears: "Replace existing voice note?".
- **Refined Recording HUD**: Standardized the discard/save actions. The "Done" button now uses a checkmark icon, and the recording timer has been updated to `MM:SS` format.
- **Improved Preview Card**: The audio attachment card now explicitly uses a `Delete` trash icon for clarity.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `MediaAttachmentStrip.kt` to its v9.4 state.
2. **State Management**:
   - No database schema changes.

## [v9.4 - MediaRecorder Crash Fix & Audio Lifecycle Refactor]

### Overview
Fixed a critical runtime crash in the voice recording system and hardened the audio capture lifecycle. Introduced runtime permission handling and API-compliant `MediaRecorder` initialization to ensure stability across different Android versions.

### Modified Files
- `app/src/main/AndroidManifest.xml`: Added `RECORD_AUDIO` permission declaration.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MediaAttachmentStrip.kt`: Major refactor of the recording engine with permission guards and safe initialization.
- `DECISION_LOG.md`: Updated with crash resolution details and architectural safeguards.

### Bug Fixes & Improvements
- **Crash Resolution**: Fixed `java.lang.RuntimeException: setAudioSource failed` by ensuring the `RECORD_AUDIO` permission is granted before initialization and using the required `MediaRecorder(context)` constructor for Android 12 (API 31) and above.
- **Permission UX**: Integrated a seamless runtime permission request flow. Tapping the microphone now prompts for access if not already granted, with toast feedback for the user.
- **Lifecycle Safety**: Added `DisposableEffect` to `MediaAttachmentStrip` to ensure native `MediaRecorder` resources are explicitly released when the component leaves the composition, preventing memory leaks and state-related crashes.
- **API Compatibility**: Implemented branching logic to support both legacy and modern `MediaRecorder` instantiation patterns.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `MediaAttachmentStrip.kt` to its v9.3 state.
   - Remove `<uses-permission android:name="android.permission.RECORD_AUDIO" />` from `AndroidManifest.xml`.
2. **State Management**:
   - No database schema changes. Audio files in the cache remain unaffected but recording functionality will revert to the unstable v9.3 state.

## [v9.3 - Standardized MediaAttachmentStrip & Voice Recording Controls]

### Overview
Unified the media attachment experience by creating a shared `MediaAttachmentStrip.kt` component. This standardized the photo and voice logging workflow across the entire application, introducing professional-grade recording controls with live feedback and discard capabilities.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/MediaAttachmentStrip.kt`: New shared component for all media interactions.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Migrated to unified strip and removed legacy launchers.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: Integrated unified strip for plant profiles.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Standardized media actions for supply label tracking.

### UI Structure Changes
- **Unified Action Strip**: Replaced disparate icon rows with a consistent horizontal bar for Camera, Gallery, and Audio.
- **Voice Recording HUD**: Tapping "Audio" now transforms the strip into a recording interface with a red pulsing indicator, live duration counter, and explicit "Stop & Save" or "Cancel" actions.
- **Enhanced Preview Tray**: Capture media appears in a horizontal scrollable list. Photos feature standard delete badges, and Voice Notes are rendered as compact audio players with play/pause and duration display.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `NewLogEntryScreen.kt`, `AddAssetScreen.kt`, and `AddSupplyScreen.kt` to their v9.3 (Today Marker) states.
   - Delete `MediaAttachmentStrip.kt`.
2. **State Management**:
   - Audio files captured via the new HUD are stored in the application cache. Reverting will not delete these files but they will no longer be linked to new records through the unified UI.

## [v9.3 - Calendar Today Marker & Location Tag Picker]

### Overview
Enhanced the logging calendar with a real-time "Today" indicator and standardized the storage location selection across the application. Migrated the supply storage field to the high-speed unified Tag Memory Sheet for zero-friction inventory management.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/logs/CalendarComponents.kt`: Added `isToday` check and circular border styling for the current date.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MemorySheets.kt`: Refactored `TagPickerSheet` to support custom titles and cleaner label formatting.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Replaced the legacy location dialog with the unified 1-tap `TagPickerSheet`.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyViewModel.kt`: Integrated `masterLocations` persistence for reusable storage labels.

### UI Structure Changes
- **Today Indicator**: The current date on the calendar now features a subtle `1.5dp` circular green border. If today is selected, the border wraps around the filled selected state, providing clear temporal context.
- **Unified Location Picker**: Tapping "Storage Location" in the supply form now opens a chip-based grid. Users can select existing locations with 1-tap or create new ones via the inline search bar.
- **Persistence**: New locations created during supply entry are automatically added to the master location list for future use in assets or other supplies.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `CalendarComponents.kt` (remove `isToday` logic and border modifiers).
   - Revert `TagPickerSheet` in `MemorySheets.kt` to the version without the `title` parameter.
   - Restore the legacy `AlertDialog` for location entry in `AddSupplyScreen.kt`.
   - Remove `masterLocations` flow and `addMasterLocation` from `AddSupplyViewModel.kt`.
2. **State Management**:
   - No database schema changes. Master locations created will remain in the `master_locations` table but will not be surfaced by the old UI.

## [v9.3 - Full CRUD Wiring & Device Testing Polish]

### Overview
Completed the full lifecycle of data management by wiring the View, Edit, and Delete workflows across all modules. This ensures the application is fully functional for live device testing and demoing, allowing users to correct mistakes and manage their database effectively.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/screens/PlantDetailScreen.kt`: Added Edit/Delete menu and confirmation logic.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/SupplyDetailScreen.kt`: Implemented action bar with Edit/Delete support.
- `app/src/main/java/com/mail2dev/planfora/ui/logs/LogComponents.kt`: Added overflow menus to log cards for granular editing/deletion.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt` & `AddSupplyViewModel.kt`: Added pre-loading logic for editing existing records.
- `app/src/main/java/com/mail2dev/planfora/data/repository/JournalRepository.kt`: Added decrement logic for log counts during deletion.

### UI Structure Changes
- **Action Menus**: Introduced consistent three-dot overflow menus in detail headers and log cards.
- **Confirmation Safety**: Implemented stylized `AlertDialogs` for all delete actions to prevent accidental data loss.
- **Adaptive Sheets**: The Asset and Supply creation sheets now dynamically switch titles and button text (e.g., "Create" vs "Update") based on the editing state.

### Rollback Instructions
1. **Code Reversion**:
   - Revert all modified files to their v9.2 states.
   - Delete `loadAsset`, `loadSupply`, and `updateLog` methods in respective ViewModels if restoring old repository logic.
2. **State Management**:
   - No database schema changes since v13.
   - Data updated via the new CRUD flows will remain persisted in SQLite.

## [v9.2 - Standardized Option A Metric Chips & Ledger]

### Overview
Unified the user interface for all dynamic parameter and custom metadata entry across the application. Replaced debug-style raw inputs with a professional "Quick-Add" chip bar (Option A) and a clean inline ledger for active metrics.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/ParameterInputSection.kt`: New shared component for metric entry.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Migrated to the new chip-based parameter system.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: Refactored optional and custom fields to match the standardized ledger style.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Applied the unified chip bar and ledger cards to supply metadata.

### UI Structure Changes
- **Quick-Add Metric Bar**: A horizontal scrollable row of chips (e.g., `+ pH`, `+ Temp`) allows 1-tap addition of common metrics.
- **Unified Ledger**: Active metrics render as compact, bordered cards with bold labels and inline numeric/text inputs.
- **Swift Custom Creator**: The "+ Custom" chip reveals an inline input field for ad-hoc metrics, keeping the interface focused.
- **Consistent Toggling**: The chip bar now serves as the single source of truth for visibility, with an inline delete icon in the ledger for quick removal.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `NewLogEntryScreen.kt`, `AddAssetScreen.kt`, and `AddSupplyScreen.kt` to their v9.1 states.
   - Delete `ParameterInputSection.kt`.
2. **State Management**:
   - No database schema changes.

## [v9.1 - Fix Harvest Card Row-Level Rendering]

### Overview
Refactored the harvest logging experience to support selective row-by-row yield entry. This allows growers to log output only for specific zones harvested during a session, rather than forcing inputs for every defined zone in an asset.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/HarvestActivityCard.kt`: Added Zone Selector chips and dynamic row rendering.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Updated to handle selective zone harvesting state.

### UI Structure Changes
- **Zone Selector**: Introduced a chip-based selector at the top of the Harvest card to toggle which rows were picked.
- **Dynamic Yield Inputs**: Individual yield fields (with unit suffixes) now only appear for selected rows, reducing form clutter.
- **Selective Auto-Sum**: The total harvest summary now dynamically calculates the sum of yields from selected rows only.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `HarvestActivityCard.kt` to the initial v9.1 version (which rendered all zones by default).
2. **State Management**:
   - Revert `NewLogEntryScreen.kt` changes to ignore `selectedZones` during harvest yield filtering.

## [v9.1 - Row/Zone Partial Harvesting & PHI Isolation Engine]

### Overview
Implemented a granular sub-asset partitioning system allowing growers to define and track specific rows or zones within a single plant asset. This enables precise harvest recording and targeted chemical safety (PHI) lockouts, ensuring that untreated zones remain harvestable while others are under lockdown.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/PlantAssetEntity.kt`: Added `zones` field (DB v13).
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/JournalLogEntity.kt`: Added `targetZones` field.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented to v13; added `MIGRATION_12_13`.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt` & `AddAssetViewModel.kt`: Added support for defining logical zones/rows.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Integrated zone selection for treatments and refactored harvest inputs.
- `app/src/main/java/com/mail2dev/planfora/ui/components/HarvestActivityCard.kt`: New granular yield breakdown component.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/PlantDetailScreen.kt`: Added Yield & Compliance dashboard with row-level PHI tracking.

### Architecture Changes
- **Sub-Asset Zoning**: Assets can now store a comma-separated list of logical units (e.g., "Row 1, Row 2").
- **Granular PHI Isolation**: Chemical application logs now target specific zones. The safety engine calculates "Locked" vs "Safe" status independently for each row based on these targets.
- **Multi-Row Yield Breakdown**: Harvest logs store an aggregated total yield and a detailed breakdown per zone in the `parameters` field (`yield_breakdown:Row 1=10;Row 2=15`).
- **Safety Dashboard**: The Asset Passport now features a `YieldComplianceCard` that visualizes cumulative row yields alongside active PHI countdowns.

### Rollback Instructions
1. **Code Reversion**:
   - Revert all modified files to their v9.0 states.
   - Delete `app/src/main/java/com/mail2dev/planfora/ui/components/HarvestActivityCard.kt`.
2. **Database Migration**:
   - Reverting to v12 will ignore the `zones` and `targetZones` columns. 
   - Note: Data stored in these columns will persist in SQLite but will be inaccessible to the application.
3. **Instructional Note**: Ensure the `AppDatabase.kt` version is reverted to `12` to match the previous schema state.

## [v9.0 - Domain-Aware Supply Module & Custom Fields]

### Overview
Upgraded the Supplies module to a professional-grade inventory system with deep support for chemical safety and custom metadata. Introduced specialized categories for pesticides and automated preset suggestions.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/DiySupplyEntity.kt`: Expanded SupplyCategory enum.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Major UI overhaul for safety fields, media, and custom fields.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyViewModel.kt`: Updated to handle expanded categories and Memento fields.

### UI Structure Changes
- **Safety First**: Active Ingredient and PHI (Pre-Harvest Interval) are now top-level, mandatory fields for all pesticide categories.
- **Compact Intelligence**: Location and Tags now share a single row. Form and Formulation fields are side-by-side to minimize scrolling.
- **Label Scanner**: Integrated a Media Strip to allow attaching high-res label photos and voice notes directly to product profiles.
- **Dynamic Presets**: Added 1-tap chips for common chemical metrics like REI (Restricted Entry Interval) and NPK ratios.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddSupplyScreen.kt`, `AddSupplyViewModel.kt`, and `DiySupplyEntity.kt` to v8.9 states.
2. **State Management**:
   - No database schema changes since v12 (custom fields).
   - Data stored in new pesticide categories (e.g., "Insecticides") will revert to "Other" or be unmatchable if the old enum is restored.

## [v8.9 - Universal High-Speed Tag Memory Sheet]

### Overview
Standardized the tag selection experience across the entire application by migrating the Asset creation flow to the shared high-speed TagPickerSheet. This eliminates UI fragmentation and ensures consistent data entry patterns for both logs and assets.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: Replaced local legacy sheet with shared component.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MemorySheets.kt`: Shared component used (no changes required this task).

### UI Structure Changes
- **Standardized Search**: The asset creation tag field now triggers the unified search bar with inline `[ ➕ Create ]` capability.
- **Instant Sync**: Tag selection and creation are now instantly persisted and synced with the Master Tag list, matching the high-speed journal logging pattern.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddAssetScreen.kt` to the v8.8 state, restoring the local `TagPickerSheet` implementation.
2. **State Management**:
   - No database schema changes.

## [v8.9 - High-Speed Tag Sheet & 1-Tap Creation]

### Overview
Refactored the tag selection bottom sheet to prioritize speed and reduce UI friction. The new unified search-and-create pattern allows users to find or define technical tags in a single workflow.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/MemorySheets.kt`: UI overhaul for unified search/create.

### UI Structure Changes
- **Unified Search Bar**: Replaced the "New Tag" button with a primary search field at the top of the sheet.
- **1-Tap Creation**: If a search query doesn't match an existing tag, a dynamic `[ ➕ Create ]` action chip appears. Tapping it persists, selects, and clears search in one action.
- **Zero-Friction Commit**: Removed the "Done" button. Selection state is updated in real-time, and dismissing the sheet (tap outside or drag down) commits all changes instantly.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `TagPickerSheet` in `MemorySheets.kt` to the v8.8 implementation (using `isAddingNew` toggle and "Done" button).
2. **State Management**:
   - No database schema changes.

## [v8.8 - Category-Aware Field Relevance Matrix]

### Overview
Introduced an intelligent field visibility engine that tailors the data entry experience and asset passports based on the specific category. This reduces cognitive load by surfacing only relevant metrics for seedlings, cuttings, trees, or crops.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: Implemented visibility matrix and category-scoped custom fields.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt`: Updated state management and field definitions.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/PlantDetailScreen.kt`: Updated metadata grid for category relevance.

### UI Structure Changes
- **Category Context**: The form now "adapts" when a category chip is selected. For example, selecting "Tree" automatically surfaces the Physical ID field as a core input.
- **Scoping**: User-defined custom fields are now filtered to only show for their intended category, or marked as "Global" to appear everywhere.
- **Passport Precision**: The Asset Detail view (Passport) now uses a context-aware layout to display category-specific milestones and measurements.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddAssetScreen.kt`, `AddAssetViewModel.kt`, and `PlantDetailScreen.kt` to v8.7 states.
2. **State Management**:
   - No database schema changes (v12 preserved).
   - Data stored in category-specific tags (e.g., `Mother:`, `PropDate:`) will remain but may not render correctly in the old UI.

## [v8.7 - Compact Asset Form & Memento Custom Field Engine]

### Overview
Significantly enhanced the Asset creation workflow with improved spatial efficiency and a powerful custom metadata engine. Users can now define their own tracking metrics per category.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/CustomFieldEntities.kt`: New entities for field definitions and values.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented to v12; added `MIGRATION_11_12`.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: Major UI overhaul for compactness, media, and dynamic fields.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt`: State management for custom fields and media.

### UI Structure Changes
- **Compact Layout**: Location and Tags now share a single row.
- **Media Strip**: Integrated attachment controls directly into the asset profile creation.
- **Dynamic Field Engine**: Custom fields (e.g., "Trunk Diameter", "Nursery Source") can be created on-the-fly and are automatically suggested for all future assets in that category.
- **Field Types**: Supports OutlinedTextFields for text/numbers and FilterChips for radio selections.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddAssetScreen.kt`, `AddAssetViewModel.kt`, `JournalRepository.kt`, and `AppDatabase.kt` to v8.6 states.
2. **Database Migration**:
   - Reverting to v11 will ignore the `custom_field_definitions` and `custom_field_values` tables. Data will remain in SQLite but will be inaccessible.

## [v8.6 - Tightened Form Scroll Boundaries]

## [v8.6 - Tightened Form Scroll Boundaries]

### Overview
Refined the log entry form by eliminating excessive bottom over-scroll and optimizing keyboard-aware focus visibility.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Removed 250dp fixed spacer and adjusted bottom padding.

### UI Structure Changes
- **Scroll Clean-up**: Replaced the arbitrary 250dp fixed spacer with standard `imePadding()` and a modest `24dp` bottom margin on the primary action button. This prevents the "blank void" at the end of the form while keeping the Save button reachable.
- **Focus Integrity**: Maintained `BringIntoViewRequester` for inline parameters to ensure focused fields stay above the soft keyboard without requiring artificial scroll length.

### Rollback Instructions
1. **Code Reversion**:
   - Re-add `Spacer(modifier = Modifier.height(250.dp))` to the end of the `Column` in `NewLogEntryScreen.kt`.
2. **State Management**:
   - No database schema changes.

## [v8.5 - IME Window Insets & Parameter Bottom Sheet]

### Overview
Improved the data entry experience by resolving keyboard occlusion issues and streamlining the addition of technical metadata.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Applied IME padding and integrated Parameter sheet.
- `app/src/main/java/com/mail2dev/planfora/ui/components/ParameterBottomSheet.kt`: New reusable sheet for metadata entry.

### UI Structure Changes
- **Keyboard Awareness**: The root scroll container now automatically adjusts its viewport when the soft keyboard is visible, ensuring the active text field (especially multi-line notes) is always accessible.
- **Parameter Chips**: Technical metadata (e.g., pH, EC) no longer clutters the main form with empty text boxes. They are now added via a dedicated sheet and displayed as clean chips: `[ Key: Value ✕ ]`.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `NewLogEntryScreen.kt` to v8.4 state (restore inline param inputs and remove `imePadding`).
   - Delete `ParameterBottomSheet.kt`.
2. **State Management**:
   - No database schema changes (v11 preserved).

## [v8.4 - Dynamic Activity Forms & Asset-Aware Auto Titles]

### Overview
Refactored the activity logging interface into a high-fidelity, context-aware engine. The system now adapts the form layout in real-time based on the selected activity and manages consistent title naming conventions automatically.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Major UI refactor with dynamic card switching and auto-title logic.
- `app/src/main/java/com/mail2dev/planfora/ui/logs/LogsViewModel.kt`: Added `addMasterTag` support.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MemorySheets.kt`: New reusable 2-tap selection sheets.

### UI Structure Changes
- **Dynamic Cards**: Specialized inputs for `Harvest` (yield/grade), `Repotting` (substrate/pot), and `Pruning` (type) appear only when relevant.
- **Auto-Title**: The log title now defaults to a professional summary of the action but remains fully editable.
- **Progressive Disclosure**: Raw Key/Value parameters are hidden behind a "🧩 Add Dynamic Parameter" chip to keep the interface clean for 90% of logging scenarios.
- **Memory Tags**: Replaced manual tag text input with a high-speed selection sheet.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `NewLogEntryScreen.kt` and `LogsViewModel.kt` to v8.3 states.
   - Delete `MemorySheets.kt`.
2. **State Management**:
   - No database schema changes (v11 preserved).
   - Specialized activity data stored in `parameters` (e.g., `yield:X|unit:Y`) will still be readable by the timeline components but the editor will lose the specialized input cards.

## [v8.3 - Compact Supply Form & Asset Category Graduation]

### Overview
Optimized the Supply creation workflow for better screen usage and introduced the "Asset Graduation" lifecycle management feature. Assets can now be promoted across categories with automated system-generated milestones in their history.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Row-based layout for dropdowns and tightened spacing.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/PlantDetailScreen.kt`: Added Graduation menu and target category selection dialog.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AssetsViewModel.kt`: Added `promoteAssetCategory` logic.
- `app/src/main/java/com/mail2dev/planfora/data/repository/JournalRepository.kt`: Added `updateAsset`.

### UI Structure Changes
- **Supply Form**: Form/Type and Formulation Code now sit side-by-side. Vertical spacing between fields reduced to 10dp.
- **Asset Passport**: Added a `MoreVert` menu with "🎓 Promote Category" option.
- **Graduation Dialog**: Quick-select list of target categories with auto-milestone creation upon update.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AddSupplyScreen.kt`, `PlantDetailScreen.kt`, `AssetsViewModel.kt`, and `JournalRepository.kt` to v8.2 states.
2. **State Management**:
   - No database schema changes (v11 preserved).
   - Graduation logs (activityType: "MILESTONE") will remain in history even if the promotion code is reverted.

## [v8.2 - Batch Logging & Bulk Asset Actions]

### Overview
Introduced high-speed batch logging workflows to significantly reduce repetitive manual entry. Users can now select dozens of assets across zones and apply a single treatment log to all of them simultaneously.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/JournalLogEntity.kt`: Added `batchGroupId`.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented to v11; added `MIGRATION_10_11`.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AssetsViewModel.kt`: Added multi-select and selection logic.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/AssetsScreen.kt`: Implemented selection UI, header tools, and bottom batch bar.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Added multi-asset chip row and batch saving logic.
- `app/src/main/java/com/mail2dev/planfora/ui/logs/LogComponents.kt`: Added batch log indicators.

### UI Structure Changes
- **Long-Press Selection**: Activating multi-select mode transforms the library into a checklist.
- **Select Zone**: Quickly select all plants in a specific orchard or greenhouse row via the header.
- **Batch Chips**: In the log creator, target assets are displayed as removable chips rather than a single field.
- **Timeline Indicator**: Batch logs are marked with a `👥 Batch` badge to distinguish group treatments from individual observations.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AssetsViewModel.kt`, `AssetsScreen.kt`, `NewLogEntryScreen.kt`, `LogsViewModel.kt`, and `LogComponents.kt`.
2. **Database Migration**:
   - Reverting to v10 will cause the `batchGroupId` column to be ignored by Room entities, effectively decoupling batch associations in the UI while data remains in SQLite.

## [v8.1 - Assets Library, Asset Passport & Timeline Filters]

### Overview
Transformed the Assets management into a high-fidelity directory with detailed "Asset Passports". Enhanced the journal timeline with professional-grade filtering for locations and safety-critical PHI statuses.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AssetsViewModel.kt`: Added search and grouping logic.
- `app/src/main/java/com/mail2dev/planfora/ui/logs/LogsViewModel.kt`: Added global location and PHI filter state.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/AssetsScreen.kt`: Major refactor for location-grouped directory and search.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/PlantDetailScreen.kt`: Refactored into the "Asset Passport" view.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/LogsScreen.kt`: Added filter strip and toggle controls.

### UI Structure Changes
- **Grouped Directory**: Assets are now automatically categorized under Location headers (e.g., "📍 Orchard A").
- **Asset Passport**: Single-asset view showing permanent notes, Physical ID, and a dedicated historical thread of all linked logs.
- **Global Filters**: The main journal now supports one-tap filtering for specific zones and an "Active PHI" toggle to quickly identify plants under treatment.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AssetsViewModel.kt`, `LogsViewModel.kt`, `AssetsScreen.kt`, `PlantDetailScreen.kt`, and `LogsScreen.kt` to v8.0 states.
2. **State Management**:
   - No database schema changes (v10 preserved).
   - Reverting code will remove search, location grouping, and filter strip functionality.

### Overview
Significant upgrade to the inventory and treatment tracking system. Introduced professional formulation types for supplies and a safety-critical PHI (Pre-Harvest Interval) countdown system.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/DiySupplyEntity.kt`: Added formulation, PHI, and stock fields.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented to v10; added `MIGRATION_9_10`.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: New modular creation screen for supplies.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Added flexible dosage units and PHI calculation logic.
- `app/src/main/java/com/mail2dev/planfora/ui/logs/LogComponents.kt`: Implemented PHI countdown badges.

### UI Structure Changes
- **Supply Formulation**: Supplies now track `Form Type` (Liquid, Powder, etc.) and `Formulation Code` (SL, EC, WP, etc.).
- **Flexible Dosage**: The treatment ribbon in the log editor now supports free-text units (e.g., "3 capfuls").
- **PHI Badges**: Log entries associated with PHI-regulated supplies display a real-time countdown (e.g., "⚠️ PHI: 3 Days Left") that transitions to a green checkmark upon expiry.

### Rollback Instructions
1. **Code Reversion**:
   - Restore `AppDatabase.kt` (v9), `DiySupplyEntity.kt`, `NewLogEntryScreen.kt`, and `LogComponents.kt`.
   - Delete `AddSupplyScreen.kt` and `AddSupplyViewModel.kt`.
   - Revert `SuppliesScreen.kt` and `MainActivity.kt` navigation changes.
2. **Database Migration**:
   - Reverting to v9 will make new supply columns inaccessible but data remains in SQLite.

## [v7.7 - Permanent Asset Notes, Physical ID Renaming & Spacing Polish]

### Overview
Refined the Asset Creation UI by introducing a permanent notes field and improving terminology for physical identifiers. Optimized the vertical density of the modular form.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/PlantAssetEntity.kt`: Added `notes` field.
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Incremented to v9; added `MIGRATION_8_9`.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: UI updates for permanent notes and compact spacing.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt`: State management updates for notes and renamed physical ID.

### UI Structure Changes
- **Permanent Notes**: Added a multi-line "Asset Notes" field to the default core field set.
- **Physical ID**: Rebranded "Tree Tag #" as "Physical ID / Tree #" with updated hints for metal tags and bed markers.
- **Spacing Polish**: Reduced inter-field spacing for injected optional fields from 16dp/8dp to 6dp for a tighter, more professional "form-factor" look.

### Rollback Instructions
1. **Code Reversion**:
   - Restore `AppDatabase.kt` (v8), `PlantAssetEntity.kt`, `AddAssetScreen.kt`, and `AddAssetViewModel.kt` to v7.6 states.
2. **Database Migration**:
   - Reverting to v8 will cause the `notes` column to be inaccessible to the Room entity, though the column will persist in the database file.

## [v7.6 - Modular Memento-Style Assets & Memory Sheets]

### Overview
Refactored the Asset Management module to use a "Memento-style" progressive disclosure UI. This update reduces cognitive load by hiding non-essential fields and using high-speed 2-tap sheets for selecting master locations and tags.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/AppDatabase.kt`: Upgraded to v8; added `MasterLocationEntity` and `MasterTagEntity`.
- `app/src/main/java/com/mail2dev/planfora/data/local/entity/MasterEntities.kt`: New entities for master data storage.
- `app/src/main/java/com/mail2dev/planfora/data/local/dao/MasterDao.kt`: New DAO for master list management.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetScreen.kt`: Replaced `AddAssetBottomSheet` with modular creation screen.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetViewModel.kt`: Centralized state for complex asset creation.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Enhanced Asset Picker with location filtering and category icons.

### UI Structure Changes
- **Lean Asset Creation**: Default state shows only Name, Category, Location, and Tags.
- **Optional Field Palette**: Expandable chip row for injecting specific fields like `GPS`, `Cost`, or `Batch ID`.
- **2-Tap sheets**: Locations and Tags now use dedicated grid sheets with haptic feedback and inline creation.
- **Asset Cards**: Updated cards in the picker to show location badges and category icons (`🌳`, `🌽`, etc.).

### Rollback Instructions
1. **Code Reversion**:
   - Restore `AppDatabase.kt` (v7), `JournalRepository.kt`, and `MainActivity.kt`.
   - Revert `NewLogEntryScreen.kt` to v7.5 state.
   - Delete `AddAssetScreen.kt`, `AddAssetViewModel.kt`, and `MasterEntities.kt`.
   - Restore `AddAssetBottomSheet.kt` from v7.5 backup.
2. **Database Migration**:
   - If rolling back, note that `master_locations` and `master_tags` tables will remain in the SQLite file but will be ignored by the Room v7 schema.

### Overview
Standardized asset categorization and improved the data entry experience for new plant profiles and log associations. Introduced a hint-rich architecture to guide users toward more structured metadata.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AssetsViewModel.kt`: Updated `AssetCategory` enum with `Tree`, `Crop/Veggie`, `Seedling`, and `Cutting`.
- `app/src/main/java/com/mail2dev/planfora/ui/assets/AddAssetBottomSheet.kt`: UI overhaul for asset creation including chip-based category selection and live preview.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Replaced Asset Dropdown with `AssetPickerBottomSheet` featuring category and location filtering.

### UI Structure Changes
- **Asset Creation**: Replaced generic dropdowns with FilterChips. Added specific hints for Location (`"e.g., Orchard A, Row 3"`) and Tags.
- **Asset Picker**: Now a searchable Bottom Sheet with icons (`🌳`, `🌽`, `🌱`, `✂️`) and multi-parameter filtering (Category + Location).
- **Live Preview**: Added a dynamic preview card in the creation sheet to show how the asset will appear in the system.

### Rollback Instructions
1. **Code Reversion**:
   - Revert `AssetCategory` enum in `AssetsViewModel.kt` to original values (`TREES`, `PLANTS`, `SEED_TRAYS`).
   - Restore `AddAssetBottomSheet.kt` and `NewLogEntryScreen.kt` from v7.4 backups.
2. **Database Note**: If data was already entered using new category strings (e.g., "Crop/Veggie"), they will no longer match the old enum values and may require manual database cleanup if strict enum matching is enforced in UI filters.
3. **Icons**: Remove icon-related logic from `AssetCategory` and picker rows if reverting to a text-only UI.

---


### Overview
Significant refactor of `NewLogEntryScreen.kt` to improve the logical flow and visual clarity of the logging interface. Grouped related fields into cards and optimized spatial hierarchy.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Complete layout restructure, introduced Surface-based grouping, and consolidated dosage ratio.

### UI Structure Changes
- **New Field Order**: Associated Plant -> Activity Type -> Treatment Card (Conditional) -> Title/Notes -> Attachments/Tags.
- **Grouped Cards**: Used `Surface` containers with 0.5dp borders for clear section separation.
- **Dosage Consolidation**: Combined `dosageUnit` and `perVolume` into a single `dosageRatio` dropdown (e.g., "mL / Liter").
- **Spacing Polish**: Tightened vertical arrangement (10dp) and adjusted typography (`labelLarge` for headers).

### Rollback Instructions
1. **Code Reversion**:
   - Restore `NewLogEntryScreen.kt` to v7.2 state (split dosage unit/volume and previous field order).
2. **State Management**:
   - Revert `dosageRatio` state back to separate `dosageUnit` and `perVolume` variables.
   - Update `LaunchedEffect` dependencies for title auto-generation.
3. **Typography/Styling**:
   - Revert `Surface` containers back to standard `Column`/`Card` structures if the tighter aesthetic is not desired.

## [v7.2 - Product-First Treatment & Contextual Dosage Memory]

### Overview
Implementation of a high-end, product-first workflow for Pest Control and Treatment logging. This update introduces contextual memory for dosages based on application methods and a searchable bottom sheet for supply selection.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/data/local/dao/JournalLogDao.kt`: Added `getLogsBySupply` to facilitate dosage memory lookup.
- `app/src/main/java/com/mail2dev/planfora/ui/logs/LogsViewModel.kt`: Added `getLatestLogForSupply` helper.
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Major UI refactor for "Product-First" workflow, implementation of Searchable Bottom Sheet, and Dosage Memory logic.

### Database & Schema
- No schema changes (v7 preserved).
- Contextual memory is derived from existing `JournalLogEntity` parameters (`dosage:X|unit:Y|volume:Z|method:W`).

### UI Structure Changes
- Replaced Supply Dropdown with a `SearchableSupplyBottomSheet`.
- Consolidated Treatment Details into a "Dosage Ribbon" and compact "Method Selector".
- Integrated A.I. hints and category badges into the primary treatment card.

### Rollback Instructions
1. **Code Reversion**:
   - Restore `NewLogEntryScreen.kt` to the state prior to v7.2 (remove BottomSheet implementation).
   - Restore `LogsViewModel.kt` (remove `getLatestLogForSupply`).
   - Restore `JournalLogDao.kt` (remove `getLogsBySupply`).
2. **State Cleanup**:
   - No database migration cleanup required as schema was not altered.
3. **Instructional Note**: If the searchable bottom sheet fails to render, check `Modifier.imePadding()` constraints in the ModalBottomSheet content.

---

## [v10.12 - Fix Location Chip Long-Press Gesture Detection]

### Overview
Resolved an issue where long-pressing on location and tag chips in selection bottom sheets was unresponsive. This was caused by the Material 3 Chip internal click handling consuming pointer input before the custom `combinedClickable` modifier could detect the long-press event.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/LocationSelectionBottomSheet.kt`: Replaced `AssistChip` with a custom `Surface` + `combinedClickable` implementation.
- `app/src/main/java/com/mail2dev/planfora/ui/components/MemorySheets.kt`: Replaced `FilterChip` with a custom `Surface` + `combinedClickable` implementation.
- `app/src/main/java/com/mail2dev/planfora/ui/components/StringPickerSheet.kt`: Replaced `AssistChip` with a custom `Surface` + `combinedClickable` implementation.

### Bug Fixes & Improvements
- **Unresponsive Long-Press Fix**: By using a custom `Surface` wrapper with `Modifier.combinedClickable`, the long-press gesture is now reliably detected. The previous implementation using M3 Chips was failing because the internal `onClick` handler of the chip components was consuming the touch events.
- **Haptic Feedback**: Correctly wired `LocalHapticFeedback` to trigger `HapticFeedbackType.LongPress` upon successful long-press detection.
- **Visual Consistency**: The custom `Surface` implementation mimics the Material 3 Chip design (8dp rounding, specific padding, and border styles) to maintain UI consistency.

---

## [v10.13 - Supply Dosage Helper Text in Log Entry]

### Overview
Improved the logging experience by surfacing product-specific dosage instructions and notes directly within the activity creation flow. This eliminates the need for users to navigate back to the supply inventory to recall application ratios or specific handling notes.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/screens/NewLogEntryScreen.kt`: Integrated `AnimatedVisibility` dosage tip in the `TreatmentDetailsCard`.

### UI & UX Improvements
- **Contextual Intelligence**: Selecting a supply (e.g., a specific fertilizer or pesticide) now triggers a subtle, animated helper text beneath the selection field if that supply has notes/instructions defined.
- **Subtle Visual Cues**: Used a small info icon and muted typography to ensure the tip is helpful without cluttering the primary treatment form.
- **Dynamic Feedback**: The tip automatically updates or collapses when switching between different products or custom inputs.

---

## [v10.14 - Standardized Tonal Surface Components]

### Overview
Introduced a standardized UI component library to unify the visual language of data entry forms across the application. This refactor replaces hardcoded surface blocks and disparate field layouts with a cohesive tonal system based on the Material 3 `surfaceVariant` specification.

### Modified Files
- `app/src/main/java/com/mail2dev/planfora/ui/components/SharedComponents.kt`: Created new shared library for `PlanForaSurfaceCard` and `PlanForaFieldGroup`.
- `app/src/main/java/com/mail2dev/planfora/ui/supplies/AddSupplyScreen.kt`: Refactored to use the new standardized components.
- `DECISION_LOG.md`: Documented the component standardization.

### UI & Architecture Changes
- **PlanForaSurfaceCard**: A reusable container using `surfaceVariant` (alpha 0.6f), `16.dp` corner rounding, and `2.dp` tonal elevation. This provides a consistent "laboratory card" aesthetic.
- **PlanForaFieldGroup**: A standardized row container for grouping horizontal field pairs (e.g., Stock & Unit) with consistent `8.dp` spacing.
- **Consolidated Layouts**: Refactored `AddSupplyScreen.kt` to group related inputs into logical sections: "Identification & Storage", "Stock & Safety", and "Product Formulation".
- **Keyboard Handling**: Enforced `imePadding()` at the root scroll container project-wide to ensure input visibility when the soft keyboard is active.
- **AddAssetScreen Refactor**: Applied the standardized tonal surface design system to the asset creation form. Grouped inputs into "Asset Identity", "Location & Taxonomy", "Lifecycle Details", and "Attachments & Notes" cards. Fixed keyboard overlap by adding `imePadding()` to the root layout.

---

## 5. Navigation & App Routing

Managed via `navigation-compose` with `Screen` sealed class contracts:

- **Logs Flow**: `LogsScreen` (FAB) -> `NewLogEntryScreen` (Save) -> `LogsScreen`.
- **Assets Flow**: 
    - `AssetsScreen` (FAB) -> `AddAssetScreen` (Modular Sheet).
    - `AssetsScreen` (Card Tap) -> `PlantDetailScreen(plantId)`.
- **Plant Detail Flow**: `PlantDetailScreen` retrieves `plantId` from backstack arguments and reactively filters the `allLogs` Flow from `LogsViewModel`.

---

## 6. Recreation & AI Bootstrapping Instructions

To recreate this project or add features, adhere to these technical constraints:

1.  **Dependency Stack**: Android 15 (API 35+), Compose BOM 2024+, Room 2.8.4, KSP, Navigation Compose.
2.  **Schema Changes**: Always bump the `version` in `AppDatabase.kt` and use `.fallbackToDestructiveMigration()` during active UI development to avoid `Room cannot verify data integrity` crashes.
3.  **State Management**: Use `StateFlow` and `collectAsStateWithLifecycle`. ViewModels must use `SharedFlow` for one-time events (Navigation/Snackbars).
4.  **UI Layouts**:
    - **Cards**: Surface color `#1E2120`, Border 0.5dp `Color.Gray`, Elevation 2dp.
    - **Text**: Title (White, Bold), Body (LightGray), Meta (SageGreen).
5.  **Data Persistence**: Ensure all DAO methods use `suspend` for C.R.U.D operations. Queries returning `Flow` should not be suspend.
6.  **Seeding**: The `AppDatabaseCallback` handles the `onCreate` seeding. If mock data is needed, clear app storage to re-trigger.
