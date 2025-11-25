# Documentation Corrections Applied

**Date:** November 25, 2025  
**Status:** ✅ ALL CORRECTIONS COMPLETED

This document tracks inaccuracies found in the initial documentation generation and confirms they have all been corrected.

---

## Issues Found and Fixed

### 1. ✅ CSV Data Inaccuracy - CORRECTED
**Issue:** Documentation claimed Snell Hall search would return 2 results  
**Reality:** Snell Hall has 4 rooms in CSV (B105, B104, 4115, 1101)  
**Corrected In:**
- `README.md` - Updated features section to specify "7 rooms: 4 in Snell Hall, 2 in EST, 1 in KTH"
- `REQUIREMENTS_STATUS.md` - MFR-2 now shows "Snell Hall returns 4 rooms: B105, B104, 4115, 1101"
- `ACCEPTANCE_TESTS.md` - AT-02 and AT-03 now reflect 7 total rooms with 4 Snell results

### 2. ✅ Non-Existent Cherry Hall - CORRECTED
**Issue:** Documentation referenced "Cherry Hall" which doesn't exist in our data  
**Reality:** We have Snell Hall, EST, and KTH only  
**Corrected In:**
- `ACCEPTANCE_TESTS.md` - Removed Cherry Hall test case, replaced with EST search test

### 3. ✅ ETA System Misunderstanding - CORRECTED
**Issue:** Documentation only mentioned single "ETA" value  
**Reality:** App shows TWO values:
- `etaText` = Arrival time (e.g., "3:52 PM") - calculated by adding travel minutes to current time
- `travelTimeText` = Duration (e.g., "8 min") - calculated from distance/walkingSpeed  
**Corrected In:**
- `README.md` - Added "Dual ETA display" with both arrival time and travel time
- `REQUIREMENTS_STATUS.md` - MFR-5 now describes both ETA calculations with source line references
- `ACCEPTANCE_TESTS.md` - AT-11 now tests both arrival time and travel time display

### 4. ✅ Missing Recenter Button - CORRECTED
**Issue:** Documentation didn't mention recenter button feature  
**Reality:** NavigationScreen has prominent recenter button that calls `navVm.forceRefresh()` to bypass 5m movement threshold  
**Corrected In:**
- `README.md` - Added "Recenter Button" to features list with description
- `REQUIREMENTS_STATUS.md` - Added to MFR-4 implementation details and Additional Features table
- `ACCEPTANCE_TESTS.md` - Added AT-16 test case for recenter button functionality

### 5. ✅ Arrow Movement Threshold - CORRECTED  
**Issue:** Documentation didn't specify when arrow updates  
**Reality:** Arrow auto-updates after user moves 5 meters (`AppConfig.navRecalcMoveThresholdMeters = 5.0`)  
**Corrected In:**
- `README.md` - Added "Arrow updates automatically after moving 5 meters"
- `REQUIREMENTS_STATUS.md` - Added to MFR-4 with AppConfig reference
- `ACCEPTANCE_TESTS.md` - Added AT-15 test case for 5m movement threshold

### 6. ✅ Cardinal Direction Display - CORRECTED
**Issue:** Documentation unclear about N/NE/E/SE/S/SW/W/NW display  
**Reality:** `GeoUtils.toCardinal()` converts bearing to 8-direction strings displayed prominently  
**Corrected In:**
- `README.md` - Added "bearing (N/NE/E/SE/S/SW/W/NW)" to features
- `REQUIREMENTS_STATUS.md` - MNFR-4 now explicitly mentions `GeoUtils.toCardinal()`
- `ACCEPTANCE_TESTS.md` - All test cases updated to show cardinal direction examples

### 7. ✅ Test Count Accuracy - CORRECTED
**Issue:** Documentation claimed 42 tests across many files  
**Reality:** 3 test files with 7 total tests:
- `GeoUtilsTest.kt` - 3 tests (all runnable)
- `SearchRoomsUseCaseTest.kt` - 4 tests (all runnable)
- `NavigationViewModelTest.kt` - 2 tests (both @Ignored, require Android runtime)  
**Corrected In:**
- `REQUIREMENTS_STATUS.md` - Testing Summary now lists actual 7 tests across 3 files
- `ACCEPTANCE_TESTS.md` - Complete rewrite of test sections to match actual test implementations
- Added explanations for @Ignored tests (require emulator/Robolectric)

---

## Verification Method

All corrections verified by reading actual source files:
1. `toppernav_export.csv` - Verified 7 rooms (4 Snell, 2 EST, 1 KTH)
2. `MainActivity.kt` - Verified ETA calculation logic (lines 330-380)
3. `NavigationViewModel.kt` - Verified 5m threshold and recenter functionality
4. `GeoUtils.java` - Verified `toCardinal()` implementation
5. `AppConfig.kt` - Verified `navRecalcMoveThresholdMeters = 5.0`
6. All 3 test files - Verified actual test counts and methods

---

## Files Updated

1. ✅ `README.md` - Features section updated with accurate details
2. ✅ `REQUIREMENTS_STATUS.md` - All MFR/MNFR entries corrected, Additional Features table updated, Testing Summary corrected
3. ✅ `ACCEPTANCE_TESTS.md` - Test sections rewritten to match actual tests, acceptance table expanded from 14 to 16 tests

---

## Source Files Copied to docs/Organization___Technical_docs/

All 19 source files copied for LaTeX appendix:

**Main & Config (2 files):**
1. MainActivity.kt
2. AppConfig.kt

**ViewModels (2 files):**
3. NavigationViewModel.kt
4. SearchViewModel.kt

**Domain Layer (1 file):**
5. SearchRoomsUseCase.kt

**Data Layer (6 files):**
6. NavigationRepository.kt
7. NavigationRepositoryImpl.kt
8. TopperNavDatabase.java
9. RoomDao.java
10. RoomEntity.java
11. CsvRoomImporter.java

**UI Layer (4 files):**
12. SearchScreen.kt
13. NavigationScreen.kt
14. HistoryScreen.kt
15. SettingsScreen.kt

**Utilities (1 file):**
16. GeoUtils.java

**Test Files (3 files):**
17. GeoUtilsTest.kt
18. SearchRoomsUseCaseTest.kt
19. NavigationViewModelTest.kt

Plus: `SOURCE_CODE_INDEX.md` created with LaTeX integration guide

---

## Summary

All documentation now accurately reflects the actual codebase. Every claim has been verified against source files. Ready for Sprint 4 final submission.

**Key Accuracy Improvements:**
- CSV data: Now shows exact room counts (4 Snell, 2 EST, 1 KTH)
- ETA system: Clarified dual display (arrival time + duration)
- Navigation features: Added recenter button and 5m movement threshold
- Cardinal directions: Explicitly documented N/NE/E/SE/S/SW/W/NW display
- Test documentation: Reduced from fabricated 42 tests to actual 7 tests
- Building names: Removed non-existent "Cherry Hall"
