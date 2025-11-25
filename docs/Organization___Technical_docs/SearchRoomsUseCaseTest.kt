package edu.wku.toppernav.domain.usecase

import edu.wku.toppernav.data.repository.NavigationRepository
import edu.wku.toppernav.data.repository.FakeNavigationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchRoomsUseCaseTest {

    class FakeRepo(private val list: List<String>) : NavigationRepository {
        override suspend fun getBuildings(): List<edu.wku.toppernav.data.model.Building> = emptyList()
        override suspend fun searchRooms(query: String): List<String> = list
    }

    @Test
    fun `non-blank query returns empty until data layer implemented`() = runBlocking {
        val repo = FakeNavigationRepository()
        val useCase = SearchRoomsUseCase(repo)

        val results = useCase("ABC")

        assertTrue(results.isEmpty())
    }

    @Test
    fun `very long query returns empty`() = runBlocking {
        val repo = FakeNavigationRepository()
        val useCase = SearchRoomsUseCase(repo)

        val results = useCase("x".repeat(100))

        assertTrue(results.isEmpty())
    }

    @Test
    fun returns_repository_results() = runBlocking {
        val repo = FakeRepo(listOf("Snell B104", "Cherry 101"))
        val uc = SearchRoomsUseCase(repo)
        val res = uc("Snell")
        assertEquals(2, res.size)
    }

    @Test
    fun validation_rejects_long_query() = runBlocking {
        val repo = FakeRepo(listOf("X"))
        val uc = SearchRoomsUseCase(repo)
        val longQuery = "a".repeat(100)
        val res = uc(longQuery)
        assertEquals(0, res.size)
    }
}
