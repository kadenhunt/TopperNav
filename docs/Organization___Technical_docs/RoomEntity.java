package edu.wku.toppernav.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "rooms",
        indices = {
                @Index(value = {"building", "room"}, unique = true)
        }
)
public class RoomEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String building;   // e.g. "SH"

    @NonNull
    private String room;       // e.g. "210"

    private Integer floor;
    private Double lat;
    private Double lng;
    private Double altM;
    private Double accuracyM;
    private String notes;
    private Long createdAt;

    // --- getters / setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getBuilding() { return building; }
    public void setBuilding(@NonNull String building) { this.building = building; }

    @NonNull
    public String getRoom() { return room; }
    public void setRoom(@NonNull String room) { this.room = room; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Double getAltM() { return altM; }
    public void setAltM(Double altM) { this.altM = altM; }

    public Double getAccuracyM() { return accuracyM; }
    public void setAccuracyM(Double accuracyM) { this.accuracyM = accuracyM; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
