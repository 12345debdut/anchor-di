package com.debdut.anchordi

/**
 * Placeholder for the Singleton component.
 *
 * In Hilt, components are generated. For Anchor DI Phase 1, we use this
 * as the default/only component - application-wide singleton scope.
 *
 * Usage in [InstallIn]:
 * ```
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object AppModule { ... }
 * ```
 */
object SingletonComponent

/**
 * Component for ViewModel-scoped bindings.
 *
 * Use with [InstallIn] for modules that provide dependencies only for ViewModels,
 * or with [Scoped] / [ViewModelScoped] to scope a binding to the ViewModel lifetime.
 * Instances are created once per ViewModel and persist until that ViewModel is cleared.
 *
 * Usage in [InstallIn]:
 * ```
 * @Module
 * @InstallIn(ViewModelComponent::class)
 * object ViewModelModule { ... }
 * ```
 *
 * ViewModels created via `viewModelAnchor()` (anchor-di-compose) run inside this scope,
 * so they can inject ViewModel-scoped dependencies.
 *
 * Use [SCOPE_ID] with `Anchor.withScope(scopeId)` when you need the scope ID as a string
 * (e.g. on Kotlin/JS where [KClass.qualifiedName] may be null).
 */
object ViewModelComponent {
    /** Scope ID used in generated bindings; use with Anchor.withScope(scopeId) for consistency. */
    const val SCOPE_ID: String = "com.debdut.anchordi.ViewModelComponent"
}

/**
 * Component for navigation-destination–scoped bindings (Compose).
 *
 * Use with [InstallIn] for modules that provide dependencies per navigation destination,
 * or with [Scoped] / [NavigationScoped] to scope a binding to the destination lifetime.
 * Instances are created once per destination and persist until the destination is removed
 * from the back stack.
 *
 * On Android, wrap destination content in [NavigationScopedContent][com.debdut.anchordi.compose.NavigationScopedContent]
 * and use [navigationScopedInject][com.debdut.anchordi.compose.navigationScopedInject] inside it.
 *
 * Usage in [InstallIn]:
 * ```
 * @Module
 * @InstallIn(NavigationComponent::class)
 * object NavigationModule { ... }
 * ```
 */
object NavigationComponent {
    /** Scope ID used in generated bindings; use with Anchor.withScope(scopeId) for consistency. */
    const val SCOPE_ID: String = "com.debdut.anchordi.NavigationComponent"
}
