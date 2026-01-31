package com.debdut.anchordi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry

/**
 * Android overload: provides navigation scope using [NavBackStackEntry] as the scope key.
 *
 * Use inside Jetpack Navigation Compose's NavHost when you have a [NavBackStackEntry]
 * from the composable lambda. Delegates to [NavigationScopedContent](scopeKey) with
 * [NavBackStackEntry.id] so that [navigationScopedInject] and [navViewModelAnchor] work inside the content.
 *
 * @param navBackStackEntry The back stack entry for this destination (e.g. from the composable lambda in NavHost).
 * @param content Composable content that can use [navigationScopedInject] and [navViewModelAnchor].
 */
@Composable
fun NavigationScopedContent(
    navBackStackEntry: NavBackStackEntry,
    content: @Composable () -> Unit,
) {
    NavigationScopedContent(scopeKey = navBackStackEntry.id, content = content)
}
