# Source Code Index

This directory contains all source code files for inclusion in the Technical Documentation LaTeX appendix.

## Source Files (19 files total)

### Main Application (1 file)
1. `MainActivity.kt` - Main entry point, navigation setup, location permission handling

### Core Configuration (1 file)
2. `AppConfig.kt` - Application-wide configuration (walking speed, movement threshold, etc.)

### ViewModels (2 files)
3. `NavigationViewModel.kt` - Navigation state management, GPS updates, ETA calculation
4. `SearchViewModel.kt` - Search state management, history tracking

### Domain Layer - Use Cases (1 file)
5. `SearchRoomsUseCase.kt` - Search business logic, input validation

### Data Layer - Repositories (2 files)
6. `NavigationRepository.kt` - Repository interface
7. `NavigationRepositoryImpl.kt` - Repository implementation (Kotlin version)

### Data Layer - Database (4 files)
8. `TopperNavDatabase.java` - Room database setup
9. `RoomDao.java` - Data access object for room queries
10. `RoomEntity.java` - Room entity model
11. `CsvRoomImporter.java` - CSV data import utility

### UI Layer - Screens (4 files)
12. `SearchScreen.kt` - Search interface with results list
13. `NavigationScreen.kt` - Navigation interface with arrow, ETA, recenter button
14. `HistoryScreen.kt` - Search history display
15. `SettingsScreen.kt` - User settings (greeting name)

### Utilities (1 file)
16. `GeoUtils.java` - Geographic calculations (Haversine distance, bearing, cardinal directions)

### Test Files (3 files)
17. `GeoUtilsTest.kt` - Unit tests for GeoUtils (3 tests)
18. `SearchRoomsUseCaseTest.kt` - Unit tests for SearchRoomsUseCase (4 tests)
19. `NavigationViewModelTest.kt` - Unit tests for NavigationViewModel (2 tests, both @Ignored)

---

## File Organization for LaTeX

### Suggested LaTeX Structure

```latex
\appendix
\chapter{Source Code}

\section{Main Application}
\lstinputlisting[language=Kotlin, caption=MainActivity.kt]{docs/Organization___Technical_docs/MainActivity.kt}

\section{Core Configuration}
\lstinputlisting[language=Kotlin, caption=AppConfig.kt]{docs/Organization___Technical_docs/AppConfig.kt}

\section{ViewModels}
\lstinputlisting[language=Kotlin, caption=NavigationViewModel.kt]{docs/Organization___Technical_docs/NavigationViewModel.kt}
\lstinputlisting[language=Kotlin, caption=SearchViewModel.kt]{docs/Organization___Technical_docs/SearchViewModel.kt}

\section{Domain Layer - Use Cases}
\lstinputlisting[language=Kotlin, caption=SearchRoomsUseCase.kt]{docs/Organization___Technical_docs/SearchRoomsUseCase.kt}

\section{Data Layer - Repositories}
\lstinputlisting[language=Kotlin, caption=NavigationRepository.kt]{docs/Organization___Technical_docs/NavigationRepository.kt}
\lstinputlisting[language=Kotlin, caption=NavigationRepositoryImpl.kt]{docs/Organization___Technical_docs/NavigationRepositoryImpl.kt}

\section{Data Layer - Database}
\lstinputlisting[language=Java, caption=TopperNavDatabase.java]{docs/Organization___Technical_docs/TopperNavDatabase.java}
\lstinputlisting[language=Java, caption=RoomDao.java]{docs/Organization___Technical_docs/RoomDao.java}
\lstinputlisting[language=Java, caption=RoomEntity.java]{docs/Organization___Technical_docs/RoomEntity.java}
\lstinputlisting[language=Java, caption=CsvRoomImporter.java]{docs/Organization___Technical_docs/CsvRoomImporter.java}

\section{UI Layer - Screens}
\lstinputlisting[language=Kotlin, caption=SearchScreen.kt]{docs/Organization___Technical_docs/SearchScreen.kt}
\lstinputlisting[language=Kotlin, caption=NavigationScreen.kt]{docs/Organization___Technical_docs/NavigationScreen.kt}
\lstinputlisting[language=Kotlin, caption=HistoryScreen.kt]{docs/Organization___Technical_docs/HistoryScreen.kt}
\lstinputlisting[language=Kotlin, caption=SettingsScreen.kt]{docs/Organization___Technical_docs/SettingsScreen.kt}

\section{Utilities}
\lstinputlisting[language=Java, caption=GeoUtils.java]{docs/Organization___Technical_docs/GeoUtils.java}

\section{Test Files}
\lstinputlisting[language=Kotlin, caption=GeoUtilsTest.kt]{docs/Organization___Technical_docs/GeoUtilsTest.kt}
\lstinputlisting[language=Kotlin, caption=SearchRoomsUseCaseTest.kt]{docs/Organization___Technical_docs/SearchRoomsUseCaseTest.kt}
\lstinputlisting[language=Kotlin, caption=NavigationViewModelTest.kt]{docs/Organization___Technical_docs/NavigationViewModelTest.kt}
```

---

## LaTeX Package Setup

Add to your LaTeX preamble:

```latex
\usepackage{listings}
\usepackage{xcolor}

% Kotlin syntax highlighting
\lstdefinelanguage{Kotlin}{
  keywords={package, import, class, interface, fun, val, var, if, else, when, for, while, return, override, private, public, protected, internal, data, object, companion, sealed, enum, annotation, suspend, inline, infix, operator, tailrec, lateinit, by, in, is, as, throw, try, catch, finally, true, false, null},
  keywordstyle=\color{blue}\bfseries,
  ndkeywords={@Override, @JvmStatic, @Composable},
  ndkeywordstyle=\color{orange}\bfseries,
  sensitive=true,
  comment=[l]{//},
  morecomment=[s]{/*}{*/},
  commentstyle=\color{gray}\ttfamily,
  stringstyle=\color{red}\ttfamily,
  morestring=[b]",
  morestring=[b]'
}

% Java syntax highlighting (built-in, but can customize)
\lstset{
  language=Java,
  basicstyle=\small\ttfamily,
  keywordstyle=\color{blue}\bfseries,
  commentstyle=\color{gray}\ttfamily,
  stringstyle=\color{red}\ttfamily,
  numbers=left,
  numberstyle=\tiny\color{gray},
  stepnumber=1,
  numbersep=10pt,
  tabsize=2,
  showspaces=false,
  showstringspaces=false,
  breaklines=true,
  frame=single,
  captionpos=b
}
```

---

## Statistics

- **Total Lines of Code:** ~3500 lines (estimated across all files)
- **Kotlin Files:** 15
- **Java Files:** 4
- **Test Files:** 3 (7 total tests, 5 runnable)
- **Languages:** Kotlin (primary), Java (database layer + utilities)
