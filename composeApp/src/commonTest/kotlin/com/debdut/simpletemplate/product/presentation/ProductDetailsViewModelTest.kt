package com.debdut.simpletemplate.product.presentation

import com.debdut.simpletemplate.product.data.Product
import com.debdut.simpletemplate.product.domain.ProductRepository
import com.debdut.simpletemplate.testing.runMainTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductDetailsViewModelTest {
    @Test
    fun blankIdDoesNothing() = runMainTest {
        val repository = FakeProductRepository(Result.success(sampleProduct(id = 10)))
        val viewModel = ProductDetailsViewModel(lazyOf(repository))

        viewModel.loadProduct("")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.product)
        assertNull(state.error)
        assertEquals(0, repository.getProductCalls)
    }

    @Test
    fun loadProductSuccessUpdatesState() = runMainTest {
        val product = sampleProduct(id = 5, title = "Coffee")
        val repository = FakeProductRepository(Result.success(product))

        val viewModel = ProductDetailsViewModel(lazyOf(repository))
        viewModel.loadProduct("5")
        assertTrue(viewModel.uiState.value.isLoading)

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(product, state.product)
        assertNull(state.error)
        assertEquals(1, repository.getProductCalls)
    }

    @Test
    fun loadProductFailureShowsError() = runMainTest {
        val repository = FakeProductRepository(Result.failure(IllegalStateException("Not found")))

        val viewModel = ProductDetailsViewModel(lazyOf(repository))
        viewModel.loadProduct("404")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Not found", state.error)
        assertNull(state.product)
    }

    private class FakeProductRepository(
        private val productResult: Result<Product>,
    ) : ProductRepository {
        var getProductCalls: Int = 0
            private set

        override suspend fun getProducts(): List<Product> = emptyList()

        override suspend fun getProduct(id: String): Product {
            getProductCalls += 1
            return productResult.getOrThrow().copy(id = id.toInt())
        }
    }

    private fun sampleProduct(
        id: Int,
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
}
