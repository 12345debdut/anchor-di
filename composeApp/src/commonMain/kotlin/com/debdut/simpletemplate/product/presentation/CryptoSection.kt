package com.debdut.simpletemplate.product.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class representing a cryptocurrency item.
 */
data class CryptoItem(
    val id: String,
    val name: String,
    val symbol: String,
    val rank: Int,
    val iconColorStart: Color,
    val iconColorEnd: Color,
)

/**
 * Object containing cryptocurrency-related composable functions.
 */
object CryptoDetails {
    /**
     * Displays a list of cryptocurrency items in a vertical scrollable layout.
     * Each item shows an icon, name, rank badge, and symbol.
     *
     * @param cryptos List of cryptocurrency items to display
     * @param modifier Modifier for the section container
     */
    @Composable
    fun CryptoSection(
        cryptos: List<CryptoItem>,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        ) {
            items(
                items = cryptos,
                key = { it.id },
            ) { crypto ->
                CryptoItemRow(crypto = crypto)
            }
        }
    }
}

/**
 * Individual cryptocurrency row item.
 * Displays icon, name, rank badge, and symbol in a horizontal layout.
 */
@Composable
private fun CryptoItemRow(
    crypto: CryptoItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Crypto Icon with gradient background
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(crypto.iconColorStart, crypto.iconColorEnd),
                        ),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            // Icon symbol (e.g., "B" for Bitcoin)
            Text(
                text = crypto.symbol.first().toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Name and Symbol Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            // Cryptocurrency Name with Rank Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = Color.White,
                )

                // Ranking Badge
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3A3B43))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "#${crypto.rank}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                        ),
                        color = Color.White,
                    )
                }
            }

            // Cryptocurrency Abbreviation
            Text(
                text = crypto.symbol,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                ),
                color = Color(0xFFA0A0A0),
            )
        }
    }
}
