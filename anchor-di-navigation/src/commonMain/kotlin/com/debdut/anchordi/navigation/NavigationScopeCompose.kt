package com.debdut.anchordi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.debdut.anchordi.runtime.AnchorContainer

/**
 * CompositionLocal that holds the current [AnchorContainer] for the navigation destination scope.
 *
 * Set by [NavigationScopedContent] when content is composed inside a navigation destination.
 * When non-null, [navigationScopedInject] resolves [NavigationComponent][com.debdut.anchordi.NavigationComponent]-scoped bindings
 * from this container.
 */
val LocalNavigationScope = compositionLocalOf<AnchorContainer?> { null }

/**
 * CompositionLocal that holds the [ViewModelComponent][com.debdut.anchordi.ViewModelComponent] container for the current navigation entry.
 *
 * Set by [NavigationScopedContent] so that [navViewModelAnchor] can resolve ViewModelComponent-scoped
 * ViewModels per navigation destination.
 */
val LocalNavViewModelScope = compositionLocalOf<AnchorContainer?> { null }

/**
 * Provides navigation scope for the given navigation destination using [NavigationScopeRegistry].
 *
 * Use in commonMain with a stable [scopeKey] per destination (e.g. route + id).
 * Wrap destination content so that [navigationScopedInject] and [navViewModelAnchor] work.
 * When the composable leaves composition, the scope is disposed via [NavigationScopeRegistry.dispose].
 *
 * On Android with Jetpack Navigation, use the overload that takes [androidx.navigation.NavBackStackEntry]
 * (in androidMain).
 *
 * @param scopeKey Stable key that uniquely identifies this navigation entry (e.g. route id).
 * @param content Composable content that can use [navigationScopedInject] and [navViewModelAnchor].
 */
@Composable
fun NavigationScopedContent(
    scopeKey: Any,
    content: @Composable () -> Unit,
) {
    val entry = remember(scopeKey) {
        NavigationScopeRegistry.getOrCreate(scopeKey)
    }
    DisposableEffect(scopeKey) {
        onDispose {
            NavigationScopeRegistry.dispose(scopeKey)
        }
    }
    CompositionLocalProvider(
        LocalNavigationScope provides entry.navContainer,
        LocalNavViewModelScope provides entry.viewModelContainer,
        content = content,
    )
}

/**
 * Resolves a [NavigationComponent][com.debdut.anchordi.NavigationComponent]-scoped (or unscoped/singleton) dependency from the current
 * navigation scope.
 *
 * Must be called from a composable that is inside [NavigationScopedContent]; otherwise throws.
 */
@Composable
inline fun <reified T : Any> navigationScopedInject(): T {
    val container = LocalNavigationScope.current
        ?: error(
            "Navigation scope is not available. Wrap destination content in NavigationScopedContent " +
                "(e.g. NavigationScopedContent(scopeKey) { ... }) so that " +
                "navigationScopedInject() can resolve NavigationComponent-scoped bindings."
        )
    return remember(container) { container.get<T>() }
}

/**
 * Returns a ViewModel scoped to the current navigation route (same lifetime as [NavigationScopedContent]).
 *
 * Must be called from a composable inside [NavigationScopedContent]. Resolves the ViewModel
 * from the ViewModelComponent scope tied to the current navigation entry.
 *
 * Works with ViewModelComponent-scoped ViewModels (e.g. @AnchorViewModel from anchor-di-compose).
 */
@Composable
inline fun <reified T : ViewModel> navViewModelAnchor(): T {
    val container = LocalNavViewModelScope.current
        ?: error(
            "Navigation ViewModel scope is not available. Wrap destination content in NavigationScopedContent " +
                "(e.g. NavigationScopedContent(scopeKey) { ... }) so that navViewModelAnchor() can resolve ViewModels."
        )
    return remember(container) { container.get<T>() }
}
