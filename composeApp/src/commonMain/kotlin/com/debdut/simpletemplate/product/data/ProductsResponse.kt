package com.debdut.simpletemplate.product.data

import kotlinx.serialization.Serializable

/**
 * Wrapper for DummyJSON products list API: https://dummyjson.com/products
 */
@Serializable
data class ProductsResponse(
    val products: List<Product>,
)
