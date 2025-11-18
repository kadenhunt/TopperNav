# TopperNav

Campus room finder for WKU using a local CSV → Room database and a simple navigation engine (straight‑line distance, bearing, ETA, and floor advice). No step‑by‑step routing.

## What works
- First run imports `toppernav_export.csv` into a local Room database (no network).
- Search: type building (e.g., `Snell`), room (e.g., `B104`), or full (`Snell Hall B104`). Results show as `BUILDING ROOM`.
- Navigate: destination lookup → permission → compute distance/bearing/ETA; show north‑up arrow, status text, and floor advice near target.
- History: in‑session list (most recent at top).
- Settings: greeting name (cosmetic).

## What is not implemented
- Step‑by‑step routing (turn list, live steps, voice guidance).
- Persisted favorites and search history.
- iOS version.

## Notes on behavior
- Arrow is north‑up (not tied to phone orientation). Rotates when bearing changes due to movement or different destination.
- ETA uses walking speed from `AppConfig.walkingSpeedMps`.
- Debug panel on Navigate shows permission and coordinates; quick links open device Location/App settings.
- Added `NAV Recomputed d=… b=… eta=…` log in the ViewModel to verify ETA calculation path.

## Structure (quick)
See `docs/ARCHITECTURE.md` for diagrams/mapping and `docs/REQUIREMENTS_STATUS.md` for detailed coverage.

```
app/
  src/main/assets/toppernav_export.csv
  src/main/java/edu/wku/toppernav/
    core/AppConfig.kt            # Singleton: feature flags + constants (incl. mock)
    viewmodel/NavigationViewModel.kt
    viewmodel/SearchViewModel.kt
    domain/usecase/SearchRoomsUseCase.kt
    data/repository/NavigationRepositoryImpl.kt
    data/local/db/TopperNavDatabase.java
    data/local/dao/RoomDao.java
    data/local/entity/RoomEntity.java
    data/importcsv/CsvRoomImporter.java
    ui/screens/*.kt
    util/GeoUtils.java
```

## Requirements snapshot (final)
- Functional: search (implemented), straight‑line route metrics (partial), ETA (implemented), UI display (partial), voice/favorites (not implemented).
- Non‑functional: Android only; Room DB local; security as non‑functional (app‑private DB + validation + minimal logs); performance logs present; portrait prioritized.

## Run
- Build/install from Android Studio or Gradle.
- On first launch, CSV is imported automatically.
- Search for a room and select it; grant Location permission.
- If indoors without a fix, enable mock in `AppConfig` for demo.
