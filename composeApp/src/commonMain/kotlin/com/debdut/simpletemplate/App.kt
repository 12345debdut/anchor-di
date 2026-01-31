package com.debdut.simpletemplate

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.debdut.simpletemplate.product.navigation.ProductAppRoot
import com.debdut.simpletemplate.theme.ProductAppTheme

/**
 * Root Compose UI: wraps [ProductAppRoot] in [ProductAppTheme].
 * [ProductAppRoot] uses Navigation 3 (NavDisplay, user-owned back stack) on all platforms:
 * Android, iOS, desktop, and web. ViewModels persist per back-stack entry until the destination is removed.
 */
@Composable
@Preview
fun App() {
    ProductAppTheme {
        ProductAppRoot()
    }
}