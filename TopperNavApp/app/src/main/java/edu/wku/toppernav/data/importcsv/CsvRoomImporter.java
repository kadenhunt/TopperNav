package edu.wku.toppernav.data.importcsv;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.wku.toppernav.data.local.db.TopperNavDatabase;
import edu.wku.toppernav.data.local.entity.RoomEntity;

public class CsvRoomImporter {

    private final Context context;
    private final TopperNavDatabase db;

    public CsvRoomImporter(Context context, TopperNavDatabase db) {
        this.context = context.getApplicationContext();
        this.db = db;
    }

    public void importIfEmpty() {
        if (db.roomDao().count() > 0) return;

        List<RoomEntity> rooms = new ArrayList<>();
        BufferedReader reader = null;

        try {
            InputStream is = context.getAssets().open("toppernav_export.csv");
            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue; // skip header line
                }
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < 5) continue;

                String building = cols[0].trim();
                String room = cols[1].trim();
                Integer floor = parseIntSafe(cols[2]);
                Double lat = parseDoubleSafe(cols[3]);
                Double lng = parseDoubleSafe(cols[4]);
                Double altM = cols.length > 5 ? parseDoubleSafe(cols[5]) : null;
                Double accM = cols.length > 6 ? parseDoubleSafe(cols[6]) : null;
                String notes = cols.length > 7 ? cols[7].trim() : null;
                Long createdAt = (cols.length > 8) ? parseLongSafe(cols[8]) : null;

                if (building.isEmpty() || room.isEmpty() || lat == null || lng == null) {
                    continue;
                }

                RoomEntity e = new RoomEntity();
                e.setBuilding(building.toUpperCase());
                e.setRoom(room);
                e.setFloor(floor);
                e.setLat(lat);
                e.setLng(lng);
                e.setAltM(altM);
                e.setAccuracyM(accM);
                e.setNotes(notes);
                e.setCreatedAt(createdAt);

                rooms.add(e);
            }

            db.roomDao().insertAll(rooms);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) {}
            }
        }
    }

    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDoubleSafe(String s) {
        try {
            String t = s.trim();
            if (t.isEmpty()) return null;
            return Double.parseDouble(t);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongSafe(String s) {
        try {
            String t = s.trim();
            if (t.isEmpty()) return null;
            return Long.parseLong(t);
        } catch (Exception e) {
            return null;
        }
    }
}
