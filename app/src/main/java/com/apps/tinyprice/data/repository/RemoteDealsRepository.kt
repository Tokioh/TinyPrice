package com.apps.tinyprice.data.repository

import com.apps.tinyprice.BuildConfig
import com.apps.tinyprice.data.model.ProductOffer
import com.apps.tinyprice.data.remote.ApiErrorResponse
import com.apps.tinyprice.data.remote.SearchProductsRequest
import com.apps.tinyprice.data.remote.TinyPriceApi
import com.apps.tinyprice.data.remote.TinyPriceApiFactory
import com.apps.tinyprice.data.remote.toProductOffer
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class RemoteDealsRepository(
    private val api: TinyPriceApi = TinyPriceApiFactory.create(),
    private val gson: Gson = Gson(),
) : DealsRepository {
    override val isPreviewData = false

    override suspend fun search(query: String): List<ProductOffer> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.searchProducts(SearchProductsRequest(query))
                if (!response.isSuccessful) {
                    throw RemoteSearchException(response.errorMessage())
                }
                response.body()?.offers.orEmpty().map { it.toProductOffer() }
            } catch (error: RemoteSearchException) {
                throw error
            } catch (_: Exception) {
                throw RemoteSearchException(
                    "No se pudo conectar con la búsqueda en vivo. Revisa tu conexión e inténtalo de nuevo.",
                )
            }
        }

    private fun Response<*>.errorMessage(): String = runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, ApiErrorResponse::class.java)?.error
        }
    }.getOrNull().orEmpty().ifBlank {
        "No fue posible hacer la búsqueda en vivo. Inténtalo de nuevo."
    }
}

class RemoteSearchException(message: String) : Exception(message)

object DealsRepositoryProvider {
    fun create(): DealsRepository =
        if (BuildConfig.USE_REMOTE_DEALS) RemoteDealsRepository()
        else PreviewDealsRepository
}
