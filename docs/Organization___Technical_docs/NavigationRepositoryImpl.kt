package edu.wku.toppernav.data.repository

import android.util.Log
import edu.wku.toppernav.data.local.dao.RoomDao
import edu.wku.toppernav.data.local.entity.RoomEntity
import edu.wku.toppernav.data.model.Building

/**
 * Concrete implementation backed by Room database.
 *
 * UML: Component Diagram -> Data Layer (Repository) mediates UI/Domain and Room DB.
 * Use Case & Sequence: invoked by SearchRoomsUseCase for search queries.
 * Security: only exposes sanitized strings; DB handles persistence.
 */
class NavigationRepositoryImpl(
    private val roomDao: RoomDao
) : NavigationRepository {

    override suspend fun getBuildings(): List<Building> {
        val all: List<RoomEntity> = roomDao.searchRooms("%%")
        val distinct = all.map { it.building }.toSet()
        return distinct.map { code ->
            Building(
                id = code,
                name = code, // placeholder name mapping
                floors = 0,
                latitude = null,
                longitude = null
            )
        }
    }

    override suspend fun searchRooms(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        val trimmed = query.trim()
        val pattern = "%${trimmed}%"
        val matches: List<RoomEntity> = roomDao.searchRooms(pattern)
        if (matches.isEmpty()) {
            Log.d("SEARCH", "No matches for '$trimmed'")
            return emptyList()
        }
        // Prioritize exact building match at start if user typed a building
        val upper = trimmed.uppercase()
        val ordered = matches.sortedWith(compareBy({ if (upper == it.building) 0 else 1 }, { it.building }, { it.room }))
        return ordered.map { it.building + " " + it.room }
    }
}
