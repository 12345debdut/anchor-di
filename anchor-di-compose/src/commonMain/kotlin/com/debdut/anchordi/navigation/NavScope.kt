package com.debdut.anchordi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

/**
 * Type-safe scope for navigation-scoped DI. Provided by [NavScopeContainer] with a [scopeKeyForEntry]
 * lambda so you can call [NavigationScopedContent](entry) with the typed [Entry]; the scope key is
 * derived via [scopeKeyForEntry].
 *
 * @param Entry The type of back-stack entries (e.g. [NavKey][androidx.navigation3.runtime.NavKey],
 *   or your sealed route type). Use the same type as your back stack list.
 */
interface NavScope<Entry : Any> {

    /** Maps a back-stack entry (route) to the scope key used for [NavigationScopeRegistry]. */
    val scopeKeyForEntry: (Entry) -> Any

    /**
     * Provides navigation scope for this destination using [NavigationScopeRegistry].
     *
     * Pass the **entry** (route for this screen). The scope key is derived via [scopeKeyForEntry],
     * so the mapping lives in one place in [NavScopeContainer]. Wrap destination content so that
     * [navigationScopedInject] and [navViewModelAnchor] work.
     *
     * **Scope lifetime:** The scope is kept while the destination may still be shown (e.g. when
     * pushing another screen). Scopes for removed entries are disposed by [NavScopeContainer].
     *
     * **Important:** This function is designed to be used inside [NavScopeContainer], which handles
     * scope disposal when entries are removed from the back stack. It does NOT dispose the scope
     * when leaving composition (e.g., when navigating forward), because the scope should persist
     * for the back stack entry's lifetime. If using this standalone (without [NavScopeContainer]),
     * you must call [NavigationScopeRegistry.dispose] manually when appropriate.
     *
     * @param entry The back-stack entry for this destination (e.g. [ProductListRoute], or `key` in
     *   `entry<ProductDetailsRoute> { key -> ... }`). Type-safe [Entry].
     * @param content Composable content that can use [navigationScopedInject] and [navViewModelAnchor].
     */
    @Composable
    fun NavigationScopedContent(
        entry: Entry,
        content: @Composable () -> Unit,
    ) {
        val scopeKey = scopeKeyForEntry(entry)
        val scopeEntry = remember(scopeKey) {
            NavigationScopeRegistry.getOrCreate(scopeKey)
        }
        CompositionLocalProvider(
            LocalNavigationScope provides scopeEntry.navContainer,
            LocalNavViewModelScope provides scopeEntry.viewModelContainer,
            content = content,
        )
    }
}

/** Default implementation: [scopeKeyForEntry] is identity (entry is used as scope key). */
internal object DefaultNavScope : NavScope<Any> {
    override val scopeKeyForEntry: (Any) -> Any = { it }
}

/**
 * Type-safe implementation of [NavScope] that uses the given [scopeKeyForEntry] lambda.
 * Created by [NavScopeContainer]; prefer using [NavScopeContainer] rather than constructing this directly.
 *
 * @param Entry The type of back-stack entries; same as [NavScopeContainer] [Entry].
 */
class NavScopeImpl<Entry : Any>(
    override val scopeKeyForEntry: (Entry) -> Any,
) : NavScope<Entry>
