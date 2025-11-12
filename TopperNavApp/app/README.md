# App Module (Android)

This module contains the Android app code for TopperNav.

## What goes where

- `src/main/java/edu/wku/toppernav/`
  - `MainActivity.kt` – App entry point and navigation host
  - `ui/` – UI only
    - `screens/` – Compose screens (Search, Navigate, History, Settings)
    - `theme/` – Colors, typography, shape system
  - `viewmodel/` – ViewModels (state + calling use cases)
  - `domain/` – Business rules
    - `usecase/` – Use cases (small, focused)
  - `data/` – Data acquisition & storage
    - `model/` – Data classes
    - `repository/` – Repository interfaces + implementations
  - `util/` – Small helpers

- `src/main/res/` – Android resources (icons, strings, etc.)
- `src/test/` – Unit tests (JVM)
- `src/androidTest/` – Instrumentation/UI tests (device/emulator)

## Kotlin vs Java
- Preferred: Kotlin. The project uses Jetpack Compose and coroutines.
- You can add Java files anywhere under `java/edu/wku/toppernav/...` and interop is supported.

## Work lanes
- Frontend: `ui/` + previews. Keep networking and data out of composables.
- Backend: `data/` + `domain/` + `viewmodel/`.

## Feature flow
Composable -> ViewModel -> UseCase -> Repository -> (local/remote) -> Models

## Run & test
- Build debug: use Android Studio Run, or terminal:
```
./gradlew assembleDebug
```
- Unit tests:
```
./gradlew testDebugUnitTest
```

## Next steps
- Introduce Room + Retrofit when APIs/entities are ready.
- Add DI (Hilt/Koin) to wire repositories & use cases into ViewModels.

