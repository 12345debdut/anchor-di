package com.debdut.anchordi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import com.debdut.anchordi.runtime.AnchorContainer

/**
 * CompositionLocal that holds the current [AnchorContainer] for the navigation destination scope.
 *
 * Set by [NavigationScopedContent] (Android) when content is composed inside a navigation
 * destination. When non-null, [navigationScopedInject] resolves [NavigationComponent]-scoped
 * bindings from this container.
 */
val LocalNavigationScope = compositionLocalOf<AnchorContainer?> { null }

/**
 * Resolves a [NavigationComponent]-scoped (or unscoped/singleton) dependency from the current
 * navigation scope.
 *
 * Must be called from a composable that is inside [NavigationScopedContent]; otherwise
 * [LocalNavigationScope] is null and this throws.
 *
 * Use for types that are [NavigationScoped] or [InstallIn][com.debdut.anchordi.InstallIn]
 * (NavigationComponent::class). Singleton and unscoped bindings are also resolved from
 * the same container.
 *
 * Example:
 * ```
 * @NavigationScoped
 * class ScreenState @Inject constructor() { ... }
 *
 * @Composable
 * fun Destination() {
 *     NavigationScopedContent(navBackStackEntry) {
 *         val state = navigationScopedInject<ScreenState>()
 *     }
 * }
 * ```
 */
@Composable
inline fun <reified T : Any> navigationScopedInject(): T {
    val container = LocalNavigationScope.current
        ?: error(
            "Navigation scope is not available. Wrap destination content in NavigationScopedContent " +
                "(e.g. NavigationScopedContent(navBackStackEntry) { ... }) so that " +
                "navigationScopedInject() can resolve NavigationComponent-scoped bindings."
        )
    return remember(container) { container.get<T>() }
}
