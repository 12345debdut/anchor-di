package com.debdut.simpletemplate.product.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debdut.anchordi.Inject
import com.debdut.anchordi.compose.AnchorViewModel
import com.debdut.simpletemplate.product.domain.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.Lazy

/**
 * ViewModel for the product list screen.
 * Injects [ProductRepository]; scoped to the navigation destination so it persists
 * when navigating to details and back (until the destination is removed from the stack).
 * Use [viewModelAnchor] inside composable(route) { } so the ViewModel is tied to the NavBackStackEntry.
 */
@AnchorViewModel
class ProductListViewModel @Inject constructor(
    private val repository: Lazy<ProductRepository>
) : ViewModel() {


    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.value.getProducts() }
                .onSuccess { products ->
                    _uiState.update {
                        it.copy(products = products, isLoading = false, error = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
        }
    }

    fun refresh() = loadProducts()
}
