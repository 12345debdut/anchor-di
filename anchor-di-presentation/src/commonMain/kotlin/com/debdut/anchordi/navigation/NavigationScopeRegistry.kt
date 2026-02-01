package com.debdut.anchordi.navigation

import com.debdut.anchordi.NavigationComponent
import com.debdut.anchordi.ViewModelComponent
import com.debdut.anchordi.runtime.Anchor
import com.debdut.anchordi.runtime.AnchorContainer

/**
 * Registry for per-navigation-entry scopes.
 *
 * Use [getOrCreate] when entering a navigation destination (e.g. when composing a screen).
 * Use [dispose] when leaving the destination so the scope is released and instances are cleared.
 *
 * Compose integration in this module: [NavigationScopedContent] uses [getOrCreate] and [dispose].
 * Non-Compose consumers (e.g. SwiftUI, native UI) call [getOrCreate] / [dispose] when their
 * navigation lifecycle indicates enter/exit.
 *
 * Thread safety: All operations are thread-safe. Can be called from any thread.
 */
object NavigationScopeRegistry {
    private val lock = Any()
    private val entries = mutableMapOf<Any, NavigationScopeEntry>()

    /**
     * Returns the navigation scope entry for [scopeKey], creating it if absent.
     * Call [dispose] with the same key when the destination is left.
     *
     * Thread-safe: can be called from any thread.
     *
     * @param scopeKey Stable key that uniquely identifies this navigation entry (e.g. route id).
     */
    fun getOrCreate(scopeKey: Any): NavigationScopeEntry {
        synchronized(lock) {
            return entries.getOrPut(scopeKey) {
                val navContainer: AnchorContainer = Anchor.scopedContainer(NavigationComponent.SCOPE_ID)
                val viewModelContainer: AnchorContainer = Anchor.scopedContainer(ViewModelComponent.SCOPE_ID)
                NavigationScopeEntry(navContainer, viewModelContainer)
            }
        }
    }

    /**
     * Releases the scope for [scopeKey]. Call when the navigation destination is left
     * (e.g. screen popped, or Compose [NavigationScopedContent] disposed).
     *
     * Thread-safe: can be called from any thread.
     */
    fun dispose(scopeKey: Any) {
        synchronized(lock) {
            entries.remove(scopeKey)
        }
    }

    /**
     * Clears all registered scopes. Primarily for testing.
     *
     * Thread-safe: can be called from any thread.
     */
    fun clear() {
        synchronized(lock) {
            entries.clear()
        }
    }
}
