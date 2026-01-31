package com.debdut.simpletemplate.product.presentation

import com.debdut.simpletemplate.product.data.Product

/**
 * UI state for the product list screen.
 * Used by [ProductListViewModel]; collected in [ProductListScreen].
 */
data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
