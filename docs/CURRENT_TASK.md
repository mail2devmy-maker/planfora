# Current Task Status

## Status: Idle
**Sprint Milestone:** Logs UI Polish — Full-Height Detail Sheet, 3-Zone Card with Media Badges & Feeding Method Rename

---

### Key Deliverables:
- [x] Configure `ModalBottomSheet` with `skipPartiallyExpanded = true` in `LogsScreen.kt` and `AddLogBottomSheet.kt`.
- [x] Update `LogComponents.kt` (`CompactLogItem` & `ExpandedLogCard`) to render Plant/Asset Name, Location Pill, and Notification-style Media Badges (`📷 count`, `🎙️ count`).
- [x] Update feeding application methods in `NewLogEntryScreen.kt` from `SPOT` to `SPREAD`.

---

### Execution Scope & Rules:
* Modify ONLY:
  * `ui/screens/LogsScreen.kt`
  * `ui/logs/AddLogBottomSheet.kt`
  * `ui/logs/LogComponents.kt`
  * `ui/screens/NewLogEntryScreen.kt`
* Do NOT alter database schema or entity definitions.

### Instructions for AI Agent:
If Status is **Idle**, reply with:
"PlanFora context loaded. `CURRENT_TASK.md` is currently idle. What is our next feature or fix?"
