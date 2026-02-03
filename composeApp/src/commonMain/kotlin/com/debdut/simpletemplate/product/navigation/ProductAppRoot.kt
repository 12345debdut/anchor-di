package com.debdut.simpletemplate.product.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.debdut.anchordi.navigation.NavScopeContainer
import com.debdut.simpletemplate.di.SessionViewModel
import com.debdut.simpletemplate.product.presentation.ProductDetailsScreen
import com.debdut.simpletemplate.product.presentation.ProductListScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Polymorphic serialization for Navigation 3 destination keys (required for non-JVM: iOS, web).
 * See: https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html#polymorphic-serialization-for-destination-keys
 */
private val productNavConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(ProductListRoute::class, ProductListRoute.serializer())
                    subclass(ProductDetailsRoute::class, ProductDetailsRoute.serializer())
                }
            }
    }

/**
 * Root composable for the product app using Navigation 3.
 * [sessionViewModel] is passed from [App]; use it for session state and [SessionViewModel.logout].
 */
@Composable
fun ProductAppRoot(sessionViewModel: SessionViewModel) {
    val backStack = rememberNavBackStack(productNavConfig, ProductListRoute)

    NavScopeContainer(
        backStack = backStack,
        scopeKeyForEntry = { entry ->
            when (entry) {
                is ProductDetailsRoute -> entry.id
                is ProductListRoute -> ProductListRoute
                else -> entry
            }
        },
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider =
                entryProvider {
                    entry<ProductListRoute> { key ->
                        NavigationScopedContent(key) {
                            ProductListScreen(
                                sessionViewModel = sessionViewModel,
                                onProductClick = { id -> backStack.add(ProductDetailsRoute(id)) },
                            )
                        }
                    }
                    entry<ProductDetailsRoute> { key ->
                        NavigationScopedContent(key) {
                            ProductDetailsScreen(
                                productId = key.id,
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                    }
                },
        )
    }
}
