package com.debdut.simpletemplate.product.presentation

import anchor_di.composeapp.generated.resources.Res
import anchor_di.composeapp.generated.resources.arrow_left_thin
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.debdut.anchordi.compose.viewModelAnchor
import com.debdut.anchordi.navigation.navViewModelAnchor
import com.debdut.simpletemplate.product.data.Product
import org.jetbrains.compose.resources.painterResource

/** Corner radius for hero image and section cards. */
private val DetailCardShape = RoundedCornerShape(16.dp)

/** Hero image placeholder height. */
private val HeroHeight = 220.dp

/**
 * Product details screen. Uses [viewModelAnchor] so the ViewModel is scoped to the
 * current navigation destination; it persists while the details screen is in the stack.
 *
 * @param productId Product id from route (e.g. "product/{id}").
 * @param onBack Called when user taps back; host should pop the back stack.
 */
@Composable
fun ProductDetailsScreen(
    productId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = navViewModelAnchor<ProductDetailsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        // Top app bar with back button
        DetailsAppBar(onBack = onBack)

        when {
            uiState.isLoading && uiState.product == null -> {
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
            uiState.error != null && uiState.product == null -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("← Back")
                    }
                }
            }
            uiState.product != null -> {
                ProductContent(
                    product = uiState.product!!,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

/** Standard app bar height (matches Material top app bar). */
private val AppBarHeight = 64.dp

/** Standard icon size for app bar actions (Material 24.dp). */
private val AppBarIconSize = 24.dp

@Composable
private fun DetailsAppBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(AppBarHeight)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp),
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) {
            Image(
                painter = painterResource(Res.drawable.arrow_left_thin),
                contentDescription = "Back",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            )
        }
        Text(
            text = "Product details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun ProductContent(
    product: Product,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Hero image placeholder
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(HeroHeight)
                    .clip(DetailCardShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🖼",
                style = MaterialTheme.typography.displayLarge,
            )
        }

        // Title
        Text(
            text = product.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Price and rating row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (product.rating > 0) {
                Text(
                    text = "★ ${product.rating}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
        )

        // Category and brand
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailChip(label = "Category", value = product.category)
            product.brand.takeIf { it.isNotBlank() }?.let { brand ->
                DetailChip(label = "Brand", value = brand)
            }
        }

        // Description card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = DetailCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Availability
        product.availabilityStatus?.let { status ->
            Text(
                text = "Availability: $status",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Reviews section
        product.reviews.takeIf { it.isNotEmpty() }?.let { reviews ->
            Text(
                text = "Reviews (${reviews.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            reviews.take(5).forEach { review ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = DetailCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = review.reviewerName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${review.rating}/5",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = review.comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
