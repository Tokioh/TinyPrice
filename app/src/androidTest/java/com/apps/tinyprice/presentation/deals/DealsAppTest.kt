package com.apps.tinyprice.presentation.deals

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.apps.tinyprice.data.model.ProductOffer
import com.apps.tinyprice.ui.theme.TinyPriceTheme
import org.junit.Rule
import org.junit.Test

class DealsAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun initialScreenShowsDiscoveryContent() {
        composeRule.setContent {
            TinyPriceTheme {
                DealsApp(state = DealsUiState(), onEvent = {})
            }
        }

        composeRule.onNodeWithText("TinyPrice").assertIsDisplayed()
        composeRule.onNodeWithText("Prueba una búsqueda").assertIsDisplayed()
        composeRule.onNodeWithText("Nintendo Switch OLED").assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsPoliteIndicator() {
        composeRule.setContent {
            TinyPriceTheme {
                DealsApp(state = DealsUiState(isLoading = true, hasSearched = true), onEvent = {})
            }
        }

        composeRule.onNodeWithTag("loading").assertIsDisplayed()
        composeRule.onNodeWithText("Buscando ofertas…").assertIsDisplayed()
    }

    @Test
    fun clickingOfferEmitsOpenOfferEvent() {
        var clicked = false
        val offer = ProductOffer(
            id = "test-1",
            store = "Tienda Test",
            productName = "Nintendo Switch OLED",
            price = "$329",
            shipping = "Envío gratis",
            delivery = "Disponible hoy",
            rating = "4.8",
            sourceUrl = "https://example.com",
            summary = "Consola en oferta",
            tags = listOf("Mejor precio"),
        )

        composeRule.setContent {
            TinyPriceTheme {
                DealsApp(
                    state = DealsUiState(
                        hasSearched = true,
                        offers = listOf(offer),
                    ),
                    onEvent = { event ->
                        if (event is DealsEvent.OpenOffer) clicked = true
                    },
                )
            }
        }

        composeRule.onNodeWithText("Tienda Test").assertIsDisplayed()
        composeRule.onNodeWithText("Nintendo Switch OLED").performClick()
        assert(clicked)
    }
}
