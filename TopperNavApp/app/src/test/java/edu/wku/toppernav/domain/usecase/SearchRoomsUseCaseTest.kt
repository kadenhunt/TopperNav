package edu.wku.toppernav.domain.usecase

import edu.wku.toppernav.data.repository.FakeNavigationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchRoomsUseCaseTest {
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
}
