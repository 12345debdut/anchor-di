package com.debdut.simpletemplate.product.presentation

import androidx.lifecycle.ViewModel
import com.debdut.anchordi.Inject
import com.debdut.anchordi.compose.AnchorViewModel
import com.debdut.simpletemplate.product.domain.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.Lazy

/**
 * ViewModel for the product details screen.
 * Injects [ProductRepository]; scoped to the navigation destination so it persists
 * while the details screen is in the back stack. Use [viewModelAnchor] inside
 * composable("product/{id}") { } so the ViewModel is tied to that NavBackStackEntry.
 */
@AnchorViewModel
class ProductDetailsViewModel
    @Inject
    constructor(
        private val repository: Lazy<ProductRepository>,
    ) : ViewModel() {
        private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        private val _uiState = MutableStateFlow(ProductDetailsUiState())
        val uiState = _uiState.asStateFlow()

        fun loadProduct(id: String) {
            if (id.isBlank()) return
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                runCatching { repository.value.getProduct(id) }
                    .onSuccess { product ->
                        _uiState.update {
                            it.copy(product = product, isLoading = false, error = null)
                        }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(isLoading = false, error = e.message ?: "Unknown error")
                        }
                    }
            }
        }
    }
