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
 * Thread safety: [getOrCreate] and [dispose] should be called from the same thread (e.g. main).
 */
object NavigationScopeRegistry {
    private val entries = mutableMapOf<Any, NavigationScopeEntry>()

    /**
     * Returns the navigation scope entry for [scopeKey], creating it if absent.
     * Call [dispose] with the same key when the destination is left.
     *
     * @param scopeKey Stable key that uniquely identifies this navigation entry (e.g. route id).
     */
    fun getOrCreate(scopeKey: Any): NavigationScopeEntry {
        return entries.getOrPut(scopeKey) {
            val navContainer: AnchorContainer = Anchor.scopedContainer(NavigationComponent.SCOPE_ID)
            val viewModelContainer: AnchorContainer = Anchor.scopedContainer(ViewModelComponent.SCOPE_ID)
            NavigationScopeEntry(navContainer, viewModelContainer)
        }
    }

    /**
     * Releases the scope for [scopeKey]. Call when the navigation destination is left
     * (e.g. screen popped, or Compose [NavigationScopedContent] disposed).
     */
    fun dispose(scopeKey: Any) {
        entries.remove(scopeKey)
    }
}
