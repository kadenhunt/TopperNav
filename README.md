# TopperNav

**GPS-Based Campus Room Finder for Western Kentucky University**

TopperNav is an Android application designed to help students, faculty, and visitors navigate WKU's campus. The app provides real-time GPS-based navigation to specific rooms within campus buildings, using a local database of building and room locations.

**Final Sprint 4 Delivery - Fall 2025**

## Features

### Implemented
- **Room Search**: Search by building name (e.g., `Snell Hall`), room number (e.g., `B104`), or combined (`Snell Hall B104`)
  - Database contains 7 rooms: 4 in Snell Hall, 2 in EST, 1 in KTH
- **GPS Navigation**: Real-time distance, bearing calculation, and dual ETA display
  - **Arrival Time ETA**: Shows clock time of arrival (e.g., "3:52 PM")
  - **Travel Time**: Shows duration to destination (e.g., "8 min")
  - Cardinal directions (N/NE/E/SE/S/SW/W/NW) calculated via `GeoUtils.toCardinal()`
- **Visual Guidance**: North-up arrow indicator showing direction to destination
  - Arrow updates automatically after moving 5 meters
  - **Recenter Button**: Tap to force immediate GPS refresh and update arrow direction
- **Floor Advice**: Automatic floor detection and guidance when near the target building
- **Search History**: In-session history of recent searches (most recent first)
- **Settings**: Customizable greeting name
- **Local Database**: First-run CSV import into Room database for offline functionality
- **Location Permissions**: Robust permission handling with debug panel and settings shortcuts

### Architecture Highlights
- **Kotlin/Jetpack Compose** for modern, reactive UI
- **MVVM Architecture** with ViewModels and use cases
- **Room Database** for local data persistence
- **Clean Architecture** separation (UI → ViewModel → UseCase → Repository → Data)
- **CSV Import System** for campus data initialization

## Project Structure
```
TopperNavApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/toppernav_export.csv
│   │   │   ├── java/edu/wku/toppernav/
│   │   │   │   ├── MainActivity.kt                    ← App entry point
│   │   │   │   ├── core/AppConfig.kt                  ← Feature flags & constants
│   │   │   │   ├── ui/screens/                        ← Compose UI screens
│   │   │   │   │   ├── SearchScreen.kt
│   │   │   │   │   ├── NavigationScreen.kt
│   │   │   │   │   ├── HistoryScreen.kt
│   │   │   │   │   └── SettingsScreen.kt
│   │   │   │   ├── viewmodel/                         ← State management
│   │   │   │   ├── domain/usecase/                    ← Business logic
│   │   │   │   ├── data/                              ← Data layer
│   │   │   │   │   ├── repository/                    ← Repository implementation
│   │   │   │   │   ├── local/db/                      ← Room database
│   │   │   │   │   ├── local/dao/                     ← Data access objects
│   │   │   │   │   └── importcsv/                     ← CSV import logic
│   │   │   │   └── util/GeoUtils.java                 ← Distance/bearing calculations
│   │   │   └── AndroidManifest.xml
│   └── build.gradle
├── docs/                                               ← Project documentation
│   ├── ARCHITECTURE.md
│   ├── REQUIREMENTS_STATUS.md
│   ├── ACCEPTANCE_TESTS.md
│   ├── PROJECT_OVERVIEW.md
│   └── RECENTER_BUTTON.md
└── settings.gradle
```

## Getting Started

### Prerequisites
- **Android Studio** (latest stable version)
- **Android SDK** (API 24+ / Android 7.0 or higher)
- **JDK 11+** (bundled with Android Studio)

### Build and Run

**Using Android Studio:**
1. Open Android Studio
2. File → Open → select the `TopperNavApp` folder
3. Wait for Gradle sync to complete
4. Click Run (or Shift+F10) to install on emulator or physical device

**Command Line (Windows):**
```powershell
cd TopperNavApp
.\gradlew.bat assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/`

**First Launch:**
- The app automatically imports campus data from the bundled CSV on first run
- Grant location permissions when prompted for full navigation functionality

## How It Works

### Navigation Algorithm
- **Distance Calculation**: Haversine formula for accurate GPS distance
- **Bearing Calculation**: Initial bearing from current location to destination
- **ETA Calculation**: Based on walking speed constant (1.4 m/s configurable in `AppConfig`)
- **Floor Detection**: Automatic floor guidance when within proximity of target building

### Data Flow
```
User Search → SearchViewModel → SearchRoomsUseCase → NavigationRepository 
→ RoomDao → Room Database → Results displayed
```

```
Navigation Start → LocationManager → NavigationViewModel → Geo Calculations 
→ UI Updates (Arrow, Distance, ETA)
```

## Known Limitations
- **No step-by-step routing**: Provides straight-line distance and bearing only
- **No voice guidance**: Visual navigation only
- **No persistent favorites**: History clears on app restart
- **Android only**: iOS version not implemented
- **Portrait optimized**: Portrait only

## Documentation
Comprehensive documentation is available in the `docs/` folder:
- **ARCHITECTURE.md** - System design, diagrams, and component mapping
- **REQUIREMENTS_STATUS.md** - Detailed requirement coverage and implementation status
- **ACCEPTANCE_TESTS.md** - Test scenarios and validation criteria

## Technical Stack
- **Language**: Kotlin (primary), Java (utilities)
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture principles
- **Database**: Room (SQLite)
- **Location Services**: Android LocationManager with FusedLocationProvider
- **Build System**: Gradle with Kotlin DSL
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)

## Team
- **Aaron Downing** - aaron.downing652@topper.wku.edu
- **Ryerson Brower** - ryerson.brower178@topper.wku.edu
- **Kaden Hunt** - kaden.hunt144@topper.wku.edu

**Client**: Michael Galloway  
**Course**: CS 360 - Software Engineering  
**Term**: Fall 2025  
**Institution**: Western Kentucky University

## License
Academic project for CS 360. All rights reserved by the development team and Western Kentucky University.

---

**Submission Date**: November 25, 2025  
**Sprint**: 4 (Final)  
**Status**: Complete and functional
