package edu.wku.toppernav.data.repository

import edu.wku.toppernav.data.model.Building

/**
 * Abstraction for data access. Implementation can combine local cache + remote.
 */
interface NavigationRepository {
    suspend fun getBuildings(): List<Building>
    suspend fun searchRooms(query: String): List<String>
}

/**
 * Placeholder in-memory implementation for UI-only milestone.
 * Backend will replace this with real Room/Retrofit implementations.
 */
class FakeNavigationRepository : NavigationRepository {

    override suspend fun getBuildings(): List<Building> {
        // TODO(back-end): Load buildings from DB or service.
        return emptyList()
    }

    override suspend fun searchRooms(query: String): List<String> {
        // TODO(back-end): Query rooms by building/room number.
        if (query.isBlank()) return emptyList()
        return emptyList()
    }
}
