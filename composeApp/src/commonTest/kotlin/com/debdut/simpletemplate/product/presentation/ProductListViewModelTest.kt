package com.debdut.simpletemplate.product.presentation

import com.debdut.simpletemplate.product.data.Product
import com.debdut.simpletemplate.product.domain.ProductRepository
import com.debdut.simpletemplate.testing.runMainTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductListViewModelTest {
    @Test
    fun loadProductsSuccessUpdatesState() =
        runMainTest {
            val products = listOf(sampleProduct(id = 1), sampleProduct(id = 2))
            val repository = FakeProductRepository(productsResult = Result.success(products))

            val viewModel = ProductListViewModel(lazyOf(repository))

            assertTrue(viewModel.uiState.value.isLoading)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals(products, state.products)
            assertEquals(1, repository.getProductsCalls)
        }

    @Test
    fun loadProductsNetworkErrorShowsFriendlyMessage() =
        runMainTest {
            val repository =
                FakeProductRepository(
                    productsResult = Result.failure(IllegalStateException("Unable to resolve host")),
                )

            val viewModel = ProductListViewModel(lazyOf(repository))
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(
                "Can't reach the server. Check your internet connection and try again.",
                state.error,
            )
        }

    @Test
    fun loadProductsUnknownErrorUsesMessage() =
        runMainTest {
            val repository =
                FakeProductRepository(
                    productsResult = Result.failure(IllegalArgumentException("Boom")),
                )

            val viewModel = ProductListViewModel(lazyOf(repository))
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Boom", state.error)
        }

    @Test
    fun refreshTriggersReload() =
        runMainTest {
            val products = listOf(sampleProduct(id = 3))
            val repository = FakeProductRepository(productsResult = Result.success(products))
            val viewModel = ProductListViewModel(lazyOf(repository))

            testScheduler.advanceUntilIdle()
            viewModel.refresh()
            testScheduler.advanceUntilIdle()

            assertEquals(2, repository.getProductsCalls)
        }

    private class FakeProductRepository(
        private val productsResult: Result<List<Product>>,
    ) : ProductRepository {
        var getProductsCalls: Int = 0
            private set

        override suspend fun getProducts(): List<Product> {
            getProductsCalls += 1
            return productsResult.getOrThrow()
        }

        override suspend fun getProduct(id: String): Product {
            error("Not used in ProductListViewModel tests")
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
