package com.debdut.anchordi.compose

/**
 * Scope for bindings tied to an Activity lifecycle (Android).
 *
 * Use with [Anchor.withScope][com.debdut.anchordi.runtime.Anchor.withScope]:
 * ```
 * @Scoped(ActivityScope::class)
 * class MyActivityScoped @Inject constructor(...)
 *
 * // In Activity:
 * Anchor.withScope(ActivityScope::class) { scope ->
 *     val thing = scope.get<MyActivityScoped>()
 * }
 * ```
 *
 * This module (anchor-di-android) has no Compose dependency, so KMP Android apps
 * without Compose can use activity-scoped DI by adding anchor-di-android only.
 *
 * For Compose, consider scoping to the ViewModelStoreOwner (e.g. NavBackStackEntry)
 * or use [viewModelAnchor][com.debdut.anchordi.compose.viewModelAnchor] for ViewModel-scoped dependencies.
 */
object ActivityScope
