package com.debdut.androidapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.debdut.anchordi.runtime.Anchor
import com.debdut.anchordi.runtime.AnchorContainer
import com.debdut.anchordi.runtime.Binding
import com.debdut.anchordi.runtime.BindingRegistry
import com.debdut.anchordi.runtime.ComponentBindingContributor
import com.debdut.anchordi.runtime.Factory
import com.debdut.anchordi.runtime.Key
import com.debdut.simpletemplate.App
import com.debdut.simpletemplate.di.getAnchorContributors
import com.debdut.simpletemplate.product.data.Product
import com.debdut.simpletemplate.product.domain.ProductRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductAppInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun productListShowsItems() {
        val products = listOf(sampleProduct(id = 1, title = "Coffee Beans"), sampleProduct(id = 2, title = "Tea"))
        val repository = FakeProductRepository(products)

        setContentWithRepository(repository)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Coffee Beans").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Products").assertIsDisplayed()
        composeRule.onNodeWithText("Browse our catalog").assertIsDisplayed()
        composeRule.onNodeWithText("Coffee Beans").assertIsDisplayed()
        composeRule.onNodeWithText("Tea").assertIsDisplayed()
    }

    @Test
    fun productListErrorShowsFriendlyMessage() {
        val repository = ErrorProductRepository("Unable to resolve host")

        setContentWithRepository(repository)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Something went wrong").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule
            .onNodeWithText("Can't reach the server. Check your internet connection and try again.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun productDetailsShownAfterClick() {
        val products = listOf(sampleProduct(id = 7, title = "Camera"))
        val repository = FakeProductRepository(products)

        setContentWithRepository(repository)

        composeRule.onNode(hasText("Camera") and hasClickAction()).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Product details").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Camera").assertIsDisplayed()
    }

    private fun setContentWithRepository(repository: ProductRepository) {
        if (Anchor.isInitialized()) {
            Anchor.reset()
        }
        Anchor.init(*getAnchorContributors(), TestBindings(repository))

        composeRule.setContent {
            App()
        }
    }

    private class TestBindings(
        private val repository: ProductRepository,
    ) : ComponentBindingContributor {
        override fun contribute(registry: BindingRegistry) {
            val key = Key(ProductRepository::class.qualifiedName!!)
            registry.register(
                key,
                Binding.Singleton(
                    object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = repository
                    },
                ),
            )
        }
    }

    private class FakeProductRepository(
        private val products: List<Product>,
    ) : ProductRepository {
        private val productById = products.associateBy { it.id.toString() }

        override suspend fun getProducts(): List<Product> = products

        override suspend fun getProduct(id: String): Product = productById[id] ?: error("Missing product $id")
    }

    private class ErrorProductRepository(
        private val message: String,
    ) : ProductRepository {
        override suspend fun getProducts(): List<Product> {
            throw IllegalStateException(message)
        }

        override suspend fun getProduct(id: String): Product {
            throw IllegalStateException(message)
        }
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
