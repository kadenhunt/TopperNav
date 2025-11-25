package edu.wku.toppernav.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import edu.wku.toppernav.data.local.dao.RoomDao;
import edu.wku.toppernav.data.local.entity.RoomEntity;

@Database(
        entities = {RoomEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class TopperNavDatabase extends RoomDatabase {

    public abstract RoomDao roomDao();

    private static volatile TopperNavDatabase INSTANCE;

    public static TopperNavDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TopperNavDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TopperNavDatabase.class,
                            "toppernav.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
