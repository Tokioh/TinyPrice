package com.apps.tinyprice.presentation.deals

import com.apps.tinyprice.data.model.ProductOffer
import com.apps.tinyprice.data.repository.DealsRepository
import com.apps.tinyprice.data.repository.RemoteSearchException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DealsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun blankQueryShowsValidationWithoutCallingRepository() = runTest {
        val repository = FakeDealsRepository()
        val viewModel = DealsViewModel(repository)

        viewModel.onEvent(DealsEvent.SubmitSearch)

        assertTrue(viewModel.state.value.hasSearched)
        assertEquals(0, repository.calls)
        assertEquals("Escribe un producto para iniciar la búsqueda.", viewModel.state.value.errorMessage)
    }

    @Test fun validQueryPublishesResults() = runTest {
        val offer = sampleOffer()
        val viewModel = DealsViewModel(FakeDealsRepository(result = listOf(offer)))

        viewModel.onEvent(DealsEvent.QueryChanged("Switch OLED"))
        viewModel.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf(offer), viewModel.state.value.offers)
    }

    @Test fun remoteFailurePublishesReadableError() = runTest {
        val viewModel = DealsViewModel(FakeDealsRepository(error = RemoteSearchException("Sin servicio")))

        viewModel.onEvent(DealsEvent.QueryChanged("Laptop"))
        viewModel.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()

        assertEquals("Sin servicio", viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.offers.isEmpty())
    }
}

private class FakeDealsRepository(
    private val result: List<ProductOffer> = emptyList(),
    private val error: RemoteSearchException? = null,
) : DealsRepository {
    override val isPreviewData = false
    var calls = 0

    override suspend fun search(query: String): List<ProductOffer> {
        calls += 1
        error?.let { throw it }
        return result
    }
}

private fun sampleOffer() = ProductOffer(
    id = "1",
    store = "Fuente web",
    productName = "Nintendo Switch OLED",
    price = "Ver en fuente",
    shipping = "Consulta en tienda",
    delivery = "Disponibilidad por confirmar",
    rating = "—",
    sourceUrl = "https://example.com/product",
    summary = "Resultado web",
    tags = listOf("Resultado web"),
)
