package com.debdut.simpletemplate.product.domain

import com.debdut.anchordi.Inject
import com.debdut.simpletemplate.product.data.Product
import com.debdut.simpletemplate.product.data.ProductApi

/**
 * Implementation of [ProductRepository] using [ProductApi].
 * Singleton so one instance is shared; ViewModels inject this for list and details.
 */
class ProductRepositoryImpl @Inject constructor(
    private val api: ProductApi
) : ProductRepository {

    override suspend fun getProducts(): List<Product> =
        api.getProducts().products

    override suspend fun getProduct(id: String): Product =
        api.getProduct(id)
}
