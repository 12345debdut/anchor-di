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
 * **Important:** This registry is backed by [NavigationScopeRegistry]. Each scope key maps to an
 * entry containing BOTH a NavigationComponent container and a ViewModelComponent container.
 * Calling [dispose] releases the entire entry (both containers). If you need to use both
 * [navigationScopedInject] and [navViewModelAnchor] for the same destination, they will share
 * the same underlying entry and be disposed together.
 *
 * Thread safety: All operations are thread-safe. Can be called from any thread.
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
     *
     * **Note:** This disposes the entire [NavigationScopeEntry] for this key, including both the
     * ViewModelComponent container AND the NavigationComponent container. If you're using both
     * ViewModel-scoped and Navigation-scoped dependencies for the same destination, they share
     * the same lifecycle and will be disposed together.
     *
     * Safe to call multiple times (idempotent).
     */
    fun dispose(scopeKey: Any) {
        NavigationScopeRegistry.dispose(scopeKey)
    }
}
