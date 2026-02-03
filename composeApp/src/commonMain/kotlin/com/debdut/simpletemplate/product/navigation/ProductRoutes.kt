package com.debdut.simpletemplate.product.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation 3 routes (NavKey) for the product app.
 * Used with [rememberNavBackStack] and [NavDisplay]; supports Android, iOS, desktop, and web.
 *
 * - [ProductListRoute]: list screen (start destination)
 * - [ProductDetailsRoute]: details screen with product [id]
 */
@Serializable
data object ProductListRoute : NavKey

/** Details screen; [id] is the product id from the API. */
@Serializable
data class ProductDetailsRoute(val id: String) : NavKey
