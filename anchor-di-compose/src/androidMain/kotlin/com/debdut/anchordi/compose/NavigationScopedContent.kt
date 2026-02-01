package com.debdut.anchordi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import com.debdut.anchordi.navigation.LocalNavigationScope
import com.debdut.anchordi.navigation.LocalNavViewModelScope
import com.debdut.anchordi.navigation.NavigationScopeRegistry

/**
 * Provides [NavigationComponent][com.debdut.anchordi.NavigationComponent] and
 * [ViewModelComponent][com.debdut.anchordi.ViewModelComponent] scope for the given navigation destination.
 *
 * Wrap the content of each NavHost destination with this so that [navigationScopedInject]
 * can resolve [NavigationScoped][com.debdut.anchordi.NavigationScoped] and
 * [InstallIn][com.debdut.anchordi.InstallIn](NavigationComponent::class) bindings, and
 * [navViewModelAnchor][com.debdut.anchordi.navigation.navViewModelAnchor] can resolve ViewModels.
 *
 * One scope (and one set of scoped instances) is created per [NavBackStackEntry]; when the
 * destination is popped and the composable leaves composition, the scope is disposed and
 * instances are released.
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
 * @param content Composable content that can use [navigationScopedInject] and
 *   [navViewModelAnchor][com.debdut.anchordi.navigation.navViewModelAnchor].
 */
@Composable
fun NavigationScopedContent(
    navBackStackEntry: NavBackStackEntry,
    content: @Composable () -> Unit
) {
    val scopeKey = navBackStackEntry.id
    val scopeEntry = remember(scopeKey) {
        NavigationScopeRegistry.getOrCreate(scopeKey)
    }
    
    DisposableEffect(scopeKey) {
        onDispose {
            NavigationScopeRegistry.dispose(scopeKey)
        }
    }
    
    CompositionLocalProvider(
        LocalNavigationScope provides scopeEntry.navContainer,
        LocalNavViewModelScope provides scopeEntry.viewModelContainer,
        content = content
    )
}
