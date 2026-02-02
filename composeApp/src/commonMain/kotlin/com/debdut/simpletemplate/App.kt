package com.debdut.simpletemplate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.debdut.anchordi.compose.viewModelAnchor
import com.debdut.simpletemplate.di.SessionHolder
import com.debdut.simpletemplate.di.SessionViewModel
import com.debdut.simpletemplate.product.navigation.ProductAppRoot
import com.debdut.simpletemplate.theme.ProductAppTheme

/**
 * Root Compose UI: wraps [ProductAppRoot] in [ProductAppTheme].
 * Session is owned by [SessionViewModel] (get via [viewModelAnchor]); call [SessionViewModel.logout]
 * to dispose the session scope. See docs/SESSION_AND_LOGOUT.md.
 */
@Composable
@Preview
fun App() {
    remember { SessionHolder.init() }
    val sessionViewModel = viewModelAnchor<SessionViewModel>()
    ProductAppTheme {
        ProductAppRoot(sessionViewModel = sessionViewModel)
    }
}
