package com.debdut.simpletemplate.product.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.debdut.anchordi.compose.anchorInject
import com.debdut.anchordi.compose.viewModelAnchor
import com.debdut.anchordi.navigation.navViewModelAnchor
import com.debdut.simpletemplate.Platform
import com.debdut.simpletemplate.di.SessionViewModel
import com.debdut.simpletemplate.product.data.Product

/** Vertical spacing between list items. */
private val ListItemSpacing = 14.dp

/** Corner radius for list cards and image placeholder. */
private val CardShape = RoundedCornerShape(16.dp)

/** Corner radius for image placeholder only. */
private val ImageShape = RoundedCornerShape(12.dp)

/**
 * Product list screen. Uses [navViewModelAnchor] for list ViewModel and [sessionViewModel]
 * for session state and [SessionViewModel.logout]. Session-scoped objects live until
 * [SessionViewModel.logout] is called (new component is created). See docs/SESSION_AND_LOGOUT.md.
 *
 * @param sessionViewModel Passed from root; use for session state and logout (no CompositionLocal).
 * @param onProductClick Called when a product is tapped; pass product id to navigate to details.
 */
@Composable
fun ProductListScreen(
    sessionViewModel: SessionViewModel,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = navViewModelAnchor<ProductListViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionState = sessionViewModel.getSessionState()
    val platform = anchorInject<Platform>()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // App bar
        ProductListAppBar()
        // Demo: session (ViewModel + logout) + root inject (Platform)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Session: ${sessionState.sessionId} · ${platform.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { sessionViewModel.logout() }) {
                Text("Logout")
            }
        }

        when {
            uiState.isLoading && uiState.products.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            uiState.error != null && uiState.products.isEmpty() -> {
                ErrorContent(
                    message = uiState.error!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                )
            }
            uiState.products.isEmpty() -> {
                EmptyContent(modifier = Modifier.fillMaxSize())
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
                ) {
                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        }
                    }
                    items(
                        items = uiState.products,
                        key = { it.id },
                    ) { product ->
                        ProductListItem(
                            product = product,
                            onClick = { onProductClick(product.id.toString()) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductListAppBar(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = "Products",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Browse our catalog",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun ProductListItem(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail placeholder (replace with image loader when available)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(ImageShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "🖼",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "📦",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No products yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Products will appear here when loaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
