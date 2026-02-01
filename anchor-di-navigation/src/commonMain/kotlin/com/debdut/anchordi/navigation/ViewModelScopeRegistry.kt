package com.debdut.anchordi.navigation

import com.debdut.anchordi.runtime.AnchorContainer

/**
 * Registry for ViewModel-scoped containers keyed by a scope key (e.g. screen id, route, or owner).
 *
 * Use this to get **ViewModelComponent**-scoped resolution on all platforms without Compose:
 * - **Android:** Use anchor-di-android's `getViewModelScope(owner, lifecycle)` or
 *   `ComponentActivity.viewModelScope()` so the scope is tied to the Activity/Fragment lifecycle
 *   and disposed automatically.
 * - **iOS / JVM / JS:** Call [getOrCreate] with a stable key (e.g. screen id, route, ViewController id)
 *   when entering a screen; call [dispose] with the same key when leaving so the scope is released.
 *
 * Backed by [NavigationScopeRegistry] (same key shares nav + viewmodel containers). If you only need
 * ViewModel scope, use this registry; [dispose] clears the entry for that key.
 *
 * Thread safety: [getOrCreate] and [dispose] should be called from the same thread (e.g. main).
 */
object ViewModelScopeRegistry {

    /**
     * Returns the ViewModel-scoped container for [scopeKey], creating it if absent.
     * Resolve [ViewModelComponent]-scoped types (e.g. ViewModels) from this container.
     *
     * @param scopeKey Stable key that uniquely identifies this scope (e.g. screen id, route, or owner).
     */
    fun getOrCreate(scopeKey: Any): AnchorContainer {
        return NavigationScopeRegistry.getOrCreate(scopeKey).viewModelContainer
    }

    /**
     * Releases the scope for [scopeKey]. Call when the screen/owner is gone so instances are cleared.
     */
    fun dispose(scopeKey: Any) {
        NavigationScopeRegistry.dispose(scopeKey)
    }
}
