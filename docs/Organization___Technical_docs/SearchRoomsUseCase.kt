package edu.wku.toppernav.domain.usecase

import edu.wku.toppernav.data.repository.NavigationRepository

/**
 * Encapsulates search business rules (e.g. logging, analytics, validation).
 * UML: Use Case 'Search Room' -> this class orchestrates repository access.
 * Sequence Diagram mapping: UI(SearchScreen) -> SearchViewModel -> SearchRoomsUseCase -> NavigationRepository -> RoomDao.
 * Keeps query validation and future logging isolated.
 */
class SearchRoomsUseCase(private val repo: NavigationRepository) {
    suspend operator fun invoke(query: String): List<String> {
        if (query.length > 40) return emptyList() // simple validation
        return repo.searchRooms(query)
    }
}
