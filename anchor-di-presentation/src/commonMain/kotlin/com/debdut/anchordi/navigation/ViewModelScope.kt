package com.debdut.anchordi.navigation

import com.debdut.anchordi.runtime.AnchorContainer

/**
 * Clear API for ViewModel-scoped resolution on all platforms.
 *
 * Use [holder] when you own the scope for the lifetime of a screen/owner (resolve multiple times,
 * then call [ViewModelScopeHolder.close] when the screen is gone). Use [use] when the scope should
 * last only for the duration of a block (disposes automatically on exit).
 *
 * **All platforms:**
 * ```
 * // Long-lived: hold the scope while the screen is active
 * val scope = ViewModelScope.holder(screenId)
 * val vm = scope.get<MyViewModel>()
 * // ... use vm ...
 * scope.close()  // when leaving the screen
 * ```
 *
 * ```
 * // Block-scoped: scope is disposed when the block returns
 * ViewModelScope.use(screenId) { container ->
 *     val vm = container.get<MyViewModel>()
 *     // ... use vm ...
 * }
 * ```
 *
 * **Android:** Prefer anchor-di-android's [getViewModelScope][com.debdut.anchordi.android.getViewModelScope](owner, lifecycle)
 * or [viewModelScope][com.debdut.anchordi.android.viewModelScope] so the scope is disposed automatically
 * when the Activity/Fragment is destroyed.
 */
object ViewModelScope {
    /**
     * Returns a holder for the ViewModel scope identified by [scopeKey]. Resolve types with
     * [ViewModelScopeHolder.get]; call [ViewModelScopeHolder.close] when the screen/owner is gone.
     *
     * Same key on any platform (e.g. screen id, route, ViewController id) yields the same scope.
     */
    fun holder(scopeKey: Any): ViewModelScopeHolder {
        return ViewModelScopeHolder(scopeKey, ViewModelScopeRegistry.getOrCreate(scopeKey))
    }

    /**
     * Runs [block] with the ViewModel-scoped container for [scopeKey], then disposes the scope
     * when the block returns (normally or with an exception). Use for one-off or short-lived use.
     */
    inline fun <R> use(
        scopeKey: Any,
        block: (AnchorContainer) -> R,
    ): R {
        val container = ViewModelScopeRegistry.getOrCreate(scopeKey)
        return try {
            block(container)
        } finally {
            ViewModelScopeRegistry.dispose(scopeKey)
        }
    }
}

/**
 * Holder for a ViewModel scope. Resolve [ViewModelComponent]-scoped types with [get]; call
 * [close] when the screen/owner is gone so the scope is released.
 */
class ViewModelScopeHolder(
    private val scopeKey: Any,
    val container: AnchorContainer,
) : AutoCloseable {
    /**
     * Resolves an instance of [T] from this ViewModel scope.
     */
    inline fun <reified T : Any> get(): T = container.get<T>()

    /**
     * Resolves an instance of [T] with the given qualifier from this ViewModel scope.
     */
    inline fun <reified T : Any> get(qualifier: String): T = container.get<T>(qualifier)

    /**
     * Releases this scope. Call when the screen/owner is gone (e.g. screen popped, Activity destroyed).
     * Idempotent: calling [close] more than once has no effect after the first.
     */
    override fun close() {
        ViewModelScopeRegistry.dispose(scopeKey)
    }
}
