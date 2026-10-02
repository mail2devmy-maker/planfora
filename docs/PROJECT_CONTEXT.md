# PlanFora — Product Overview & Local Context

## 1. Product Description
**PlanFora** (`com.mail2dev.planfora`) is an offline-first Android management and logging app designed for farmers and growers to track assets, plant growth, supplies, field measurements, and custom journal activities.

## 2. Core Feature Pillars
* **Asset & Plant Tracking:** Track plant locations, sub-locations, zones, and blocks.
* **Activity & Journal Logging:** Log activities (e.g., weeding, harvesting, watering) with custom fields, audio notes, and media attachments.
* **Inventory & Supplies:** Track DIY supplies, fertilizers, equipment, and material usage logs.
* **Offline-First Storage:** Local Room database with SAF-based JSON export/import for easy manual backups.

## 3. Local Environment & Sandbox
* **Active Working Directory:** `C:\DockerShared\Project\PlanFora\`
* **Master Source Backup:** `C:\109Backup\Project\PlanFora\`
* **Local Homelab Server:** `192.168.0.10` (Gitea / Syncthing / FileBrowser / SMB)
* **Execution Stack:** Android Studio AI Agent / Gemini CLI / Antigravity CLI (`agy`) + Wireless ADB debugging on a physical Android device.

## 4. Documentation Mapping
* `docs/SYSTEM_PROMPT.md`: Core AI execution rules and handoff instructions.
* `docs/PROJECT_CONTEXT.md`: High-level app purpose and local setup.
* `docs/ARCHITECTURE.md`: Tech stack specs, package map, and coding standards.
* `docs/CURRENT_TASK.md`: Active task sprint details and immediate goals.
* `docs/CHANGELOG.md`: Historical record of completed milestones.