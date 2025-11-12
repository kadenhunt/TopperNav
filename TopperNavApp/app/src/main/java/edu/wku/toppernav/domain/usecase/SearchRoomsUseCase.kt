package edu.wku.toppernav.domain.usecase

import edu.wku.toppernav.data.repository.NavigationRepository

/**
 * Encapsulates search business rules (e.g. logging, analytics, validation).
 */
class SearchRoomsUseCase(private val repo: NavigationRepository) {
    suspend operator fun invoke(query: String): List<String> {
        if (query.length > 40) return emptyList() // simple validation
        return repo.searchRooms(query)
    }
}

