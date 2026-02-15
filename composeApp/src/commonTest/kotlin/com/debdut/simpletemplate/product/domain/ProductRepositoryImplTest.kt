package com.debdut.simpletemplate.product.domain

import com.debdut.simpletemplate.product.data.Product
import com.debdut.simpletemplate.product.data.ProductApi
import com.debdut.simpletemplate.product.data.ProductsResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductRepositoryImplTest {
    @Test
    fun getProductsReturnsListFromApi() = runTest {
        val products = listOf(sampleProduct(id = 1), sampleProduct(id = 2, title = "Second"))
        val repository = ProductRepositoryImpl(lazyOf(FakeProductApi(products = products)))

        val result = repository.getProducts()

        assertEquals(products, result)
    }

    @Test
    fun getProductReturnsSingleItem() = runTest {
        val expected = sampleProduct(id = 42, title = "Answer")
        val repository = ProductRepositoryImpl(lazyOf(FakeProductApi(product = expected)))

        val result = repository.getProduct("42")

        assertEquals(expected, result)
    }

    private class FakeProductApi(
        private val products: List<Product> = emptyList(),
        private val product: Product = sampleProduct(),
    ) : ProductApi {
        override suspend fun getProducts(): ProductsResponse = ProductsResponse(products)

        override suspend fun getProduct(id: String): Product = product.copy(id = id.toInt())
    }
}

private fun sampleProduct(
    id: Int = 1,
    title: String = "Sample",
    description: String = "Description",
    category: String = "Category",
    price: Double = 9.99,
): Product =
    Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
    )
