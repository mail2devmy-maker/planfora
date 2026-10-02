# Current Task Status

## Status: Idle
Asset Manager UI Redesign — View Toggle (Hierarchy List vs Visual Grid) & Filter Bottom Sheet

---

### Key Deliverables:
- [x] Add `AssetViewMode` enum (`HIERARCHY_LIST`, `VISUAL_GRID`) and StateFlow in `AssetsViewModel.kt`.
- [x] Replace static filter bars and header in `AssetsScreen.kt` with a Material 3 TopAppBar containing Search, Filter Bottom Sheet trigger, and View Toggle actions.
- [x] Implement `FilterBottomSheet` in `AssetsScreen.kt` for Category and Location filters.
- [x] Flatten `"UNASSIGNED ZONE"` grouping in Hierarchy List mode to render direct assets without nested wrappers.
- [x] Create `ListAssetCard` (56dp square thumbnail, compact row) and `GridAssetCard` (2-column 1:1 image grid card) in `AssetsScreen.kt`.

---

### Execution Scope & Rules:
* Modify ONLY:
  * `ui/screens/AssetsScreen.kt`
  * `ui/assets/AssetsViewModel.kt`
* Do NOT change database schema or entity relationship

### Instructions for AI Agent:
If Status is **Idle**, reply with:
"PlanFora context loaded. `CURRENT_TASK.md` is currently idle. What is our next feature or fix?"
