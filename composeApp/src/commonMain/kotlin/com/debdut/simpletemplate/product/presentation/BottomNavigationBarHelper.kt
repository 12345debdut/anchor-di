package com.debdut.simpletemplate.product.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Helper functions for properly handling bottom navigation bar padding
 * when using edge-to-edge layouts (enableEdgeToEdge()).
 *
 * The issue: With edge-to-edge enabled, the NavigationBar can be overlapped
 * by the system navigation bar, making items unclickable.
 *
 * Solution / RECOMMENDED: Add navigationBarsPadding() modifier directly to NavigationBar.
 * This ensures the navigation bar items are accessible above the system navigation bar.
 *
 * Usage:
 * ```
 * NavigationBar(
 *     modifier = Modifier.navigationBarsPadding()
 * ) {
 *     NavigationBarItem(...)
 *     NavigationBarItem(...)
 * }
 * ```
 */
@Composable
fun BottomNavigationBarWithPadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    NavigationBar(
        modifier =
            modifier
                // CRITICAL: Add this modifier to ensure items are clickable
                .navigationBarsPadding(),
        content = content,
    )
}

/**
 * Alternative: Use windowInsetsPadding for more control.
 * This gives you explicit control over which insets to apply.
 */
@Composable
fun BottomNavigationBarWithWindowInsets(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    NavigationBar(
        modifier =
            modifier
                .windowInsetsPadding(WindowInsets.navigationBars),
        content = content,
    )
}

/**
 * Alternative: Wrap NavigationBar in a Box with padding.
 * Use this if modifiers don't work directly on NavigationBar.
 */
@Composable
fun BottomNavigationBarWrapped(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .navigationBarsPadding(),
    ) {
        NavigationBar(
            // Disable default insets since we handle it manually
            windowInsets = WindowInsets(0),
            content = content,
        )
    }
}
