# Acceptance Test Plan (Final Sprint)

Summary
- This project delivers search + local DB + straight‑line navigation metrics (distance, bearing, ETA) and floor advice.
- GPS‑based metrics render when a device provides a location fix; on our BLU S5 indoors, fixes were unreliable. We added `NAV Recomputed…` logs to verify the computation path.

| ID | Test case | Steps | Expected | Result |
|----|-----------|-------|----------|--------|
| AT-01 | App builds | gradlew assembleDebug | Build succeeds without errors | P |
| AT-02 | First-run CSV import | Fresh install + launch | No crash; search after 2+ chars returns entries (CSV count=7) | P |
| AT-03 | Search by building | Type "Snell" | Shows Snell Hall rooms | P |
| AT-04 | Search by room | Type "B104" | Shows "SNELL HALL B104" | P |
| AT-05 | Search by building + room | Type "Snell Hall B104" | Exact/near‑top match | P |
| AT-06 | History (session) | Select a result; open History | Selected item appears at top | P |
| AT-07 | Settings greeting | Set a name; return | Top app bar greets by name | P |
| AT-08 | Navigate screen | Select a result | Destination, arrow circle, debug panel visible | P |
| AT-09 | Permission prompt | Go to Navigate w/o permission | System prompt; granting sets perm=true | P |
| AT-10 | Distance + bearing status | After fix or mock | Status line shows "<m> m • <cardinal>" | F (no fix on BLU S5 indoors) |
| AT-11 | ETA text | After fix or mock | ETA shows integer minutes (≥1) | F (depends on AT-10) |
| AT-12 | Floor advice | Near destination | Floor/upstairs/downstairs when applicable | F (needs distance) |
| AT-13 | Mock fallback | Enable mock in AppConfig | Debug shows user=(mockLat,mockLng) | P |
| AT-14 | Bearing arrow rotation | With user & dest known | Arrow rotates to bearing; north‑up | Pending |

Work to do (post‑submission)
- Integrate fused location provider for faster indoor fixes and better power behavior.
- Add device‑heading sensor to rotate arrow relative to the way the phone faces.
- Persist favorites and history via Room.
- Optional: step‑by‑step routing or maps layer.
