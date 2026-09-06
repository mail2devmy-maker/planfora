# PlanFora Architecture & Development Rules

## Tech Stack
- Language: Kotlin
- UI Framework: Jetpack Compose (Material 3)
- Database: Room DB (Local-First)
- Architecture Pattern: MVVM (Model-View-ViewModel) + Clean Architecture (Data -> Repository -> ViewModel -> UI)

## Coding Standards
1. UI Components: Keep composables small and modular. Store reusable UI elements under `ui/components/`.
2. State Management: UIs must collect state as `StateFlow` from ViewModels.
3. Dark Theme First: Follow `DESIGN.md` explicitly. Deep charcoal/black backgrounds (`#121413`), forest/sage green accents (`#22C55E`, `#86EFAC`), and 28px card corner radii.
4. Local First: All data writes must target Room DB first before triggering cloud backup.