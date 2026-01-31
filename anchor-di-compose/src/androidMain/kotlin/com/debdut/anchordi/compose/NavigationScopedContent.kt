package com.debdut.anchordi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import com.debdut.anchordi.NavigationComponent
import com.debdut.anchordi.runtime.Anchor

/**
 * Provides [NavigationComponent] scope for the given navigation destination.
 *
 * Wrap the content of each NavHost destination with this so that [navigationScopedInject]
 * can resolve [NavigationScoped] and [InstallIn][com.debdut.anchordi.InstallIn]
 * (NavigationComponent::class) bindings. One scope (and one set of scoped instances) is
 * created per [NavBackStackEntry]; when the destination is popped, the scope is released.
 *
 * Example:
 * ```
 * NavHost(navController, startDestination = "home") {
 *     composable("home") {
 *         NavigationScopedContent(requireNotNull(it)) {
 *             HomeScreen()  // can use navigationScopedInject<ScreenState>()
 *         }
 *     }
 * }
 * ```
 *
 * @param navBackStackEntry The back stack entry for this destination (e.g. from the
 *   composable lambda in NavHost).
 * @param content Composable content that can use [navigationScopedInject].
 */
@Composable
fun NavigationScopedContent(
    navBackStackEntry: NavBackStackEntry,
    content: @Composable () -> Unit
) {
    val scope = remember(navBackStackEntry.id) {
        Anchor.scopedContainer(NavigationComponent.SCOPE_ID)
    }
    CompositionLocalProvider(LocalNavigationScope provides scope, content = content)
}
