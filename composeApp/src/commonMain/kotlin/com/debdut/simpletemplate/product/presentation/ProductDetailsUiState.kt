package com.debdut.simpletemplate.product.presentation

import com.debdut.simpletemplate.product.data.Product

/**
 * UI state for the product details screen.
 * Used by [ProductDetailsViewModel]; collected in [ProductDetailsScreen].
 */
data class ProductDetailsUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
