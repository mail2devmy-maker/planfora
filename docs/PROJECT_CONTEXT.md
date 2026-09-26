# PlanFora - App Overview & Core Context

## 1. Product Description
**PlanFora** (`com.mail2dev.planfora`) is an offline-first Android management and logging application designed for tracking assets, plant logs, supplies, measurements, and custom field activities.

## 2. Target Users & Key Features
* **Asset & Plant Tracking:** Manage plant locations, sub-locations, zones, and blocks.
* **Activity & Journal Logs:** Record activities (e.g., weeding, harvesting, watering) with custom fields, audio notes, and media attachments.
* **Inventory & Supplies:** Track DIY supplies, materials, and usage logs.
* **Offline-First Storage:** Local Room database with JSON export/import via Storage Access Framework (SAF).

## 3. Environment & Local Setup
* **AI Sandbox Directory:** `C:\DockerShared\Project\PlanFora\`
* **Master Source Backup:** `C:\109Backup\Project\PlanFora\`
* **Local Homelab Server:** `192.168.0.10` (Syncthing / FileBrowser / SMB)
* **Testing Stack:** Physical Android device via Wireless ADB / USB Debugging + Android Studio.

## 4. Documentation Mapping
* **`docs/SYSTEM_PROMPT.md`**: AI master rules and active guardrails.
* **`docs/PROJECT_CONTEXT.md`**: High-level app goals and feature summary.
* **`docs/ARCHITECTURE.md`**: Tech stack, Room DB schema versioning, SDK levels, and directory map.
* **`docs/CURRENT_TASK.md`**: Active sprint goals, immediate TODOs, and current progress.
* **`docs/CHANGELOG.md`**: Historical record of completed milestones and verified changes.