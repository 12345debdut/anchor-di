package com.debdut.anchordi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * Wraps navigation content and disposes scopes when entries are removed from the back stack.
 *
 * Use as the root of your nav UI: pass [backStack] and [scopeKeyForEntry], and put [NavDisplay]
 * (and [NavigationScopedContent] per destination) inside the [content] lambda. The lambda has
 * [NavScope] receiver so you can call [NavigationScopedContent][NavScope.NavigationScopedContent]
 * inside. When the back stack shrinks (e.g. user pops), scopes for entries no longer in the stack
 * are disposed via [NavigationScopeRegistry.dispose].
 *
 * Example (Navigation 3 with typed [NavKey][androidx.navigation3.runtime.NavKey]):
 * ```
 * val backStack = rememberNavBackStack(config, ProductListRoute)
 * NavScopeContainer<NavKey>(backStack, scopeKeyForEntry = { entry ->
 *   when (entry) {
 *     is ProductListRoute -> ProductListRoute
 *     is ProductDetailsRoute -> entry.id
 *     else -> entry
 *   }
 * }) {
 *   NavDisplay(backStack = backStack, onBack = { backStack.removeLastOrNull() }, entryProvider {
 *     entry<ProductListRoute> { NavigationScopedContent(ProductListRoute) { ProductListScreen(...) } }
 *     entry<ProductDetailsRoute> { key -> NavigationScopedContent(key) { ProductDetailsScreen(...) } }
 *   })
 * }
 * ```
 *
 * @param Entry The type of back-stack entries (e.g. [NavKey], or your sealed route type). Type-safe.
 * @param backStack The current back stack (e.g. from [rememberNavBackStack]); must be observable
 *   (e.g. SnapshotStateList) so that when it changes this effect runs.
 * @param scopeKeyForEntry Mapping from [Entry] to the scope key. Pass the typed **entry** to [NavigationScopedContent];
 *   the framework derives the scope key via this lambda (single source of truth).
 * @param content Composable content with [NavScope][NavScope] receiver; put [NavDisplay] and [NavigationScopedContent](entry) here.
 *
 * **Implementation note:** A new [NavScopeImpl] is created each composition (no [remember]); this is
 * intentional and cheap. The [scopeKeyForEntry] lambda is invoked only when the back stack changes
 * (disposal) and when [NavigationScopedContent](entry) resolves a scope key.
 */
@Composable
fun <Entry : Any> NavScopeContainer(
    backStack: List<Entry>,
    scopeKeyForEntry: (Entry) -> Any,
    content: @Composable NavScope<Entry>.() -> Unit = {}
) {
    val navScope = NavScopeImpl(scopeKeyForEntry)
    val previousKeys = remember { mutableSetOf<Any>() }
    LaunchedEffect(backStack.size, backStack.lastOrNull()) {
        val currentKeys = backStack.map { scopeKeyForEntry(it) }.toSet()
        (previousKeys - currentKeys).forEach { NavigationScopeRegistry.dispose(it) }
        previousKeys.clear()
        previousKeys.addAll(currentKeys)
    }
    content(navScope)
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
