package com.debdut.simpletemplate.product.data

/**
 * API contract for product data from DummyJSON.
 * Implemented by [ProductApiImpl]; provided via [ProductApiModule].
 */
interface ProductApi {
    /** Fetches all products. GET https://dummyjson.com/products */
    suspend fun getProducts(): ProductsResponse

    /** Fetches a single product by id. GET https://dummyjson.com/products/:id */
    suspend fun getProduct(id: String): Product
}
