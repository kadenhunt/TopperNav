package edu.wku.toppernav.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import edu.wku.toppernav.data.local.entity.RoomEntity;

@Dao
public interface RoomDao {

    @Query("SELECT * FROM rooms WHERE building = :building AND room = :room LIMIT 1")
    RoomEntity findByBuildingAndRoom(String building, String room);

    // for existing rooms
    @Query("SELECT * FROM rooms " +
           "WHERE (building || ' ' || room) LIKE :pattern " +
           "OR building LIKE :pattern " +
           "OR room LIKE :pattern " +
           "ORDER BY building, room")
    List<RoomEntity> searchRooms(String pattern);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RoomEntity> rooms);

    @Query("SELECT COUNT(*) FROM rooms")
    int count();
}
