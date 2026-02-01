package com.debdut.simpletemplate.product.data

import com.debdut.anchordi.Inject
import com.debdut.anchordi.ViewModelScoped
import io.ktor.client.HttpClient
import kotlin.Lazy
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Ktor-based implementation of [ProductApi].
 * Injects [HttpClient] provided by [ProductApiModule]; calls DummyJSON endpoints.
 * Unscoped: used only via [ProductApiBindsModule] which binds ProductApi as Singleton.
 */
class ProductApiImpl @Inject constructor(
    private val httpClient: Lazy<HttpClient>
) : ProductApi {

    override suspend fun getProducts(): ProductsResponse =
        httpClient.value.get(PRODUCTS_URL).body()

    override suspend fun getProduct(id: String): Product =
        httpClient.value.get("$PRODUCTS_URL/$id").body()

    private companion object {
        const val PRODUCTS_URL = "https://dummyjson.com/products"
    }
}
