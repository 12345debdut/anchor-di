package com.debdut.anchordi.compose

/**
 * Scope for bindings tied to an Activity lifecycle (Android).
 *
 * Use with [Anchor.withScope]:
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
 * For Compose, consider scoping to the ViewModelStoreOwner (e.g. NavBackStackEntry)
 * or use [viewModelAnchor] for ViewModel-scoped dependencies.
 */
object ActivityScope
