package com.debdut.anchordi

/**
 * Scopes a binding to [NavigationComponent]: one instance per navigation destination.
 *
 * When applied to a [Provides] method or [Inject] constructor, the binding
 * will be scoped to the current navigation destination (e.g. NavBackStackEntry on Android).
 * The instance persists for the lifetime of that destination and is cleared when the
 * destination is removed from the back stack.
 *
 * Resolve navigation-scoped types only inside [NavigationScopedContent][com.debdut.anchordi.compose.NavigationScopedContent]
 * (or equivalent) so that the correct scope is active; use [navigationScopedInject][com.debdut.anchordi.compose.navigationScopedInject]
 * in composables.
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
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class NavigationScoped
