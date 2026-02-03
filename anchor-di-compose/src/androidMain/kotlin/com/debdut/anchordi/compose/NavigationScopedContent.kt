package com.debdut.anchordi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import com.debdut.anchordi.navigation.LocalNavViewModelScope
import com.debdut.anchordi.navigation.LocalNavigationScope
import com.debdut.anchordi.navigation.NavigationScopeRegistry

/**
 * Provides [NavigationComponent][com.debdut.anchordi.NavigationComponent] and
 * [ViewModelComponent][com.debdut.anchordi.ViewModelComponent] scope for the given navigation destination.
 *
 * Wrap the content of each NavHost destination with this so that [navigationScopedInject]
 * can resolve [NavigationScoped][com.debdut.anchordi.NavigationScoped] and
 * modules with [InstallIn][com.debdut.anchordi.InstallIn](NavigationComponent::class), and
 * [navViewModelAnchor][com.debdut.anchordi.navigation.navViewModelAnchor] can resolve ViewModels.
 *
 * One scope (and one set of scoped instances) is created per [NavBackStackEntry]; when the
 * entry is destroyed (popped from the back stack), the scope is disposed and instances are released.
 *
 * **Important:** The scope persists even when the composable temporarily leaves composition
 * (e.g., when navigating forward to another screen). It is only disposed when the
 * [NavBackStackEntry] lifecycle reaches DESTROYED state.
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
    content: @Composable () -> Unit,
) {
    val scopeKey = navBackStackEntry.id
    val scopeEntry =
        remember(scopeKey) {
            NavigationScopeRegistry.getOrCreate(scopeKey)
        }

    // Tie scope disposal to NavBackStackEntry lifecycle, NOT composition lifecycle.
    // This ensures scope persists when navigating forward (composable leaves composition
    // but entry is still in back stack), and disposes only when entry is truly destroyed.
    DisposableEffect(navBackStackEntry) {
        val lifecycle = navBackStackEntry.lifecycle

        // Handle edge case where lifecycle is already destroyed when we start observing
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            NavigationScopeRegistry.dispose(scopeKey)
        }

        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    NavigationScopeRegistry.dispose(scopeKey)
                }
            }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            // Observer handles disposal on ON_DESTROY; no redundant disposal needed here.
            // The scope persists when composable leaves composition but entry is still alive.
        }
    }

    CompositionLocalProvider(
        LocalNavigationScope provides scopeEntry.navContainer,
        LocalNavViewModelScope provides scopeEntry.viewModelContainer,
        content = content,
    )
}
