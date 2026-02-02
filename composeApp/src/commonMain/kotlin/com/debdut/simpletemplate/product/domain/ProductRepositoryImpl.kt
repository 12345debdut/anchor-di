package com.debdut.simpletemplate.product.domain

import com.debdut.anchordi.Inject
import com.debdut.simpletemplate.product.data.Product
import com.debdut.simpletemplate.product.data.ProductApi
import kotlin.Lazy

/**
 * Implementation of [ProductRepository] using [ProductApi].
 * Singleton so one instance is shared; ViewModels inject this for list and details.
 */
class ProductRepositoryImpl
    @Inject
    constructor(
        private val api: Lazy<ProductApi>,
    ) : ProductRepository {
        override suspend fun getProducts(): List<Product> = api.value.getProducts().products

        override suspend fun getProduct(id: String): Product = api.value.getProduct(id)
    }
