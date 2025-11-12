# TopperNav (Android)

Western Kentucky University indoor navigation application (UI-first milestone). This repository contains the Android client with user interface screens implemented in Kotlin/Jetpack Compose. Data access, database, and networking are intentionally not implemented yet.

## Contents
- Overview
- Project structure
- Build and run
- Development guidelines
- Data model and CSV handoff
- Next steps (for backend integration)

## Overview
TopperNav provides a basic user interface for searching destinations, viewing simple step placeholders, browsing recent searches, and adjusting a display name used by the greeting. The current code avoids any hardcoded sample data and is ready for a backend team to connect a database and services.

## Project structure
```
TopperNavApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/edu/wku/toppernav/
│   │   │   │   ├── MainActivity.kt         ← App entry and navigation host
│   │   │   │   ├── ui/                     ← UI only (Jetpack Compose)
│   │   │   │   │   ├── screens/            ← Composables per screen (Search, Navigate, History, Settings)
│   │   │   │   │   └── theme/              ← Colors, typography, shapes
│   │   │   │   ├── viewmodel/              ← ViewModels (state holders) — minimal for now
│   │   │   │   ├── domain/                 ← Use cases (interfaces for business rules)
│   │   │   │   │   └── usecase/
│   │   │   │   ├── data/                   ← Models and repository interfaces
│   │   │   │   │   ├── model/              ← Data classes (e.g., Building)
│   │   │   │   │   └── repository/         ← NavigationRepository API and a placeholder impl
│   │   │   │   └── util/                   ← Small helpers (if any)
│   │   │   └── AndroidManifest.xml         ← App configuration
│   └── build.gradle                         ← Module build file
├── build.gradle                             ← Project build file
├── settings.gradle                          ← Gradle settings
└── toppernav_export.csv                     ← Campus data export (see below)
```
Notes:
- No sample/mock data ships with the UI. Screens render, accept input, and show placeholders only.
- `NavigationRepository` defines the data access contract. `FakeNavigationRepository` returns empty results intentionally to avoid shipping stand‑in data.

## Build and run
Prerequisites:
- Android Studio (latest stable)
- Android SDK/Platform tools installed via Android Studio
- JDK 11+ (bundled with recent Android Studio)

Open in Android Studio:
1. File → Open → select the `TopperNavApp` folder.
2. Let Gradle sync complete.
3. Use Run to install on an emulator or device.

Command line (Windows, from repository root):
```
gradlew.bat assembleDebug
```
The APK will be under `app/build/outputs/apk/debug/`.

## Development guidelines
- Keep UI logic in `ui/` composables. Avoid data access in composables.
- Use ViewModels (`viewmodel/`) to hold UI state once the data layer is available.
- Keep `domain/usecase` free of Android framework types to enable unit testing.
- `data/repository` should provide interfaces; implementations can combine local (Room) and remote (Retrofit/Ktor) sources later.
- Kotlin is preferred; Java interop is supported if needed.

## Data model and CSV handoff
The repository includes `toppernav_export.csv` at the project root. Columns:
- building
- room
- floor
- lat, lng
- alt_m (altitude meters)
- accuracy_m (GPS accuracy estimate)
- notes
- created_at (epoch ms)

Current app behavior does not read this file. Backend contributors may:
- Create a simple import utility (Gradle task or runtime one‑shot) to load CSV rows into a local Room database.
- Define Room entities (e.g., BuildingEntity, RoomEntity) and mappers to domain models.
- Implement `NavigationRepository` methods using Room DAOs and, later, a remote source if applicable.

Suggested import steps (outline):
1. Add a Room database with DAOs for buildings and rooms.
2. Write a small importer that parses `toppernav_export.csv` and populates the database on first run (or via a developer‑only debug menu).
3. Implement `NavigationRepository.getBuildings()` and `searchRooms(query)` using DAOs.

## Next steps (for backend integration)
- Data layer
  - Define entities and DAOs.
  - Implement `FakeNavigationRepository` replacement using Room; return real results.
  - Optional: add Retrofit/Ktor client once a service exists.
- UI wiring
  - Introduce ViewModels per screen (e.g., `SearchViewModel`) and connect to use cases.
  - Replace placeholder texts with state collected from ViewModels.
- Testing
  - Unit tests for use cases and repository functions.
  - Instrumentation test to navigate across tabs.

## Status
- UI compiles and runs without bundled sample data.
- Repository APIs exist with no production data sources yet.
- Project is ready for backend work (database and import of `toppernav_export.csv`).
