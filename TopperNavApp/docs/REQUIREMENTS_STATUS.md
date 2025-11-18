# Requirements Coverage Snapshot (Final Sprint)

Scope: UI + local data (CSV → Room) + straight‑line navigation metrics (distance, bearing, ETA) and floor advice. No step‑by‑step routing.

## Functional requirements
- Use GPS to determine current location — Partial
  - Implementation: `NavigationViewModel` requests GPS/Network fixes; updates NavState.
  - Reality: Works on emulator/good hardware; on BLU S5 indoors we didn’t receive live updates. A mock fallback is available for demo.
  - Debug: Added `NAV Recomputed d=… b=… eta=…` log to confirm ETA pipeline.
- Search for building + room — Implemented
  - Room DB seeded from CSV; LIKE search via repository/DAO.
- Generate step‑by‑step navigation route — Not Implemented
  - Out of scope; we provide straight‑line bearing/ETA instead.
- Interactive display to navigate — Partial
  - Destination header, bearing arrow (north‑up), status line, floor advice near target; includes debug panel.
- Estimated travel time — Implemented
  - Based on walking speed heuristic in `AppConfig`.
- Voice guidance — Not Implemented
- Favorites/bookmarks — Not Implemented
- Recent searches — Partial
  - In‑session only (not persisted).
- Floor awareness near destination — Implemented
  - Advises floor/upstairs/downstairs when within threshold.

## Non‑functional requirements (incl. security)
- Location accuracy ±5 m — Partial / Device‑dependent
  - Accurate outdoors on capable devices; indoors on BLU S5 is unreliable.
- Route generation ≤ 2 s — Met for straight‑line ETA
  - Computation is O(1) once a fix is available.
- Platform compatibility — Partial
  - Android only; iOS out of scope.
- Visual + text guidance — Partial
  - Text status + arrow; no turn‑by‑turn list or map.
- Orientation support — Partial
  - Optimized for portrait; landscape not validated.
- Database usage — Implemented
  - Room DB seeded from asset CSV.
- Performance metrics — Partial
  - Search start logs (Perf); NAV recompute logs added.
- Security (non‑functional) — Partial
  - App‑private DB; basic input validation; minimal logs; no network.
- UI metrics — Partial
  - Manual observation with logs; no formal dashboard.

## Notes for submission
- Straight‑line guidance (distance, bearing, ETA) is considered “route generation” for this project.
- Lack of indoor GPS fixes on the BLU S5 is the primary reason some acceptance tests are marked as Fail; the computation and UI paths are implemented and logged.
