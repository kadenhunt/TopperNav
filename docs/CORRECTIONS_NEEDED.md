# TopperNav Project - Final Documentation Corrections

**Critical Corrections Needed Based on Actual Code Review**

## Actual Data from CSV (7 rooms total):
1. Snell Hall B105
2. Snell Hall B104
3. Snell Hall 4115 (floor 4)
4. Snell Hall 1101 (floor 1)
5. KTH 2003 (floor 2)
6. EST 213 (floor 1)
7. EST 250 (floor 1)

**Search for "Snell" returns 4 results** (not 2 as I incorrectly stated)

## ETA vs Travel Time:
- **ETA** = Estimated Time of Arrival (clock time like "3:52 PM")
- **Travel Time** = Duration to destination (like "8 min")
- Code shows: `etaText` shows arrival time, `travelTimeText` shows duration
- Both derived from `state.etaMinutes`

## Cardinal Directions:
- **YES, the app DOES show N/NE/E/SE/S/SW/W/NW**
- Implementation in `GeoUtils.toCardinal()` returns these 8-direction strings
- Status line shows format: "45m • NE" (distance + cardinal direction)

## Arrow Updates:
- Arrow updates after user moves **5 meters** (not arbitrarily)
- Configured in `AppConfig.navRecalcMoveThresholdMeters = 5.0`
- Code: `if (moved >= AppConfig.navRecalcMoveThresholdMeters) return true`

## Recenter Button:
- **YES, there IS a recenter button** in NavigationScreen
- Calls `navVm.forceRefresh()` to get immediate GPS fix
- Purpose: Get new GPS coordinates when arrow seems stuck

## Actual Unit Tests (3 test files, not all passing):
1. **GeoUtilsTest.kt** - 3 tests (can run without Android)
2. **SearchRoomsUseCaseTest.kt** - 4 tests (can run without Android)
3. **NavigationViewModelTest.kt** - 2 tests (marked @Ignore, require emulator/Robolectric)

## Buildings Actually in Data:
- ✅ Snell Hall (4 rooms)
- ✅ KTH (1 room)
- ✅ EST (2 rooms)
- ❌ NO Cherry Hall in the data

---

## Source Code Files to Copy for LaTeX Documentation

For your LaTeX Technical Documentation appendix, you need to copy these key source files into the `docs/Organization___Technical_docs/` folder:

### Core Application Files:
1. `app/src/main/java/edu/wku/toppernav/MainActivity.kt`
2. `app/src/main/java/edu/wku/toppernav/core/AppConfig.kt`

### ViewModels:
3. `app/src/main/java/edu/wku/toppernav/viewmodel/NavigationViewModel.kt`
4. `app/src/main/java/edu/wku/toppernav/viewmodel/SearchViewModel.kt`

### Domain Layer:
5. `app/src/main/java/edu/wku/toppernav/domain/usecase/SearchRoomsUseCase.kt`

### Data Layer:
6. `app/src/main/java/edu/wku/toppernav/data/repository/NavigationRepository.kt`
7. `app/src/main/java/edu/wku/toppernav/data/repository/NavigationRepositoryImpl.kt`
8. `app/src/main/java/edu/wku/toppernav/data/local/db/TopperNavDatabase.java`
9. `app/src/main/java/edu/wku/toppernav/data/local/dao/RoomDao.java`
10. `app/src/main/java/edu/wku/toppernav/data/local/entity/RoomEntity.java`
11. `app/src/main/java/edu/wku/toppernav/data/importcsv/CsvRoomImporter.java`

### UI Screens:
12. `app/src/main/java/edu/wku/toppernav/ui/screens/SearchScreen.kt`
13. `app/src/main/java/edu/wku/toppernav/ui/screens/NavigationScreen.kt`
14. `app/src/main/java/edu/wku/toppernav/ui/screens/HistoryScreen.kt`
15. `app/src/main/java/edu/wku/toppernav/ui/screens/SettingsScreen.kt`

### Utilities:
16. `app/src/main/java/edu/wku/toppernav/util/GeoUtils.java`

### Test Files:
17. `app/src/test/java/edu/wku/toppernav/util/GeoUtilsTest.kt`
18. `app/src/test/java/edu/wku/toppernav/domain/usecase/SearchRoomsUseCaseTest.kt`
19. `app/src/test/java/edu/wku/toppernav/viewmodel/NavigationViewModelTest.kt`

---

## How to Reference in LaTeX

In your Technical Documentation.tex, you can reference these files in the appendix like:

```latex
\section{Appendix: Source Code}

\subsection{MainActivity.kt}
\lstinputlisting[language=Kotlin]{./MainActivity.kt}

\subsection{NavigationViewModel.kt}
\lstinputlisting[language=Kotlin]{./NavigationViewModel.kt}

% etc...
```

Make sure the files are in the same directory as your .tex file or adjust paths accordingly.

---

## README.md Status

The README should be mostly accurate now, but needs these corrections:
- Emphasis that ETA shows arrival time AND travel time
- Mention of recenter button feature
- Accurate CSV data (7 rooms: 4 Snell, 1 KTH, 2 EST)
