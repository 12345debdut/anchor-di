package com.debdut.simpletemplate.product.domain

import com.debdut.simpletemplate.product.data.Product

/**
 * Repository contract for product data.
 * Bound to [ProductRepositoryImpl] in [ProductRepositoryModule]; used by list and details ViewModels.
 */
interface ProductRepository {
    suspend fun getProducts(): List<Product>

    suspend fun getProduct(id: String): Product
}
