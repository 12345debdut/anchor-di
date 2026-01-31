package com.debdut.anchordi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

/**
 * Scope for navigation-scoped DI. Provided by [NavScopeContainer] with a [scopeKeyForEntry] lambda
 * so you can call [NavigationScopedContent](entry) and pass the route/entry; the scope key is
 * derived via [scopeKeyForEntry].
 */
interface NavScope {

    /** Maps a back-stack entry (route) to the scope key used for [NavigationScopeRegistry]. */
    val scopeKeyForEntry: (Any) -> Any

    /**
     * Provides navigation scope for this destination using [NavigationScopeRegistry].
     *
     * Pass the **entry** (route/key for this screen). The scope key is derived via [scopeKeyForEntry],
     * so the mapping lives in one place in [NavScopeContainer]. Wrap destination content so that
     * [navigationScopedInject] and [navViewModelAnchor] work.
     *
     * **Scope lifetime:** The scope is kept while the destination may still be shown (e.g. when
     * pushing another screen). Scopes for removed entries are disposed by [NavScopeContainer].
     *
     * @param entry The back-stack entry for this destination (e.g. [ProductListRoute], or `key` in
     *   `entry<ProductDetailsRoute> { key -> ... }`). Same object you pass to [scopeKeyForEntry] in [NavScopeContainer].
     * @param content Composable content that can use [navigationScopedInject] and [navViewModelAnchor].
     */
    @Composable
    fun NavigationScopedContent(
        entry: Any,
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
internal object DefaultNavScope : NavScope {
    override val scopeKeyForEntry: (Any) -> Any = { it }
}

/**
 * Implementation of [NavScope] that uses the given [scopeKeyForEntry] lambda.
 * Created by [NavScopeContainer]; prefer using [NavScopeContainer] rather than constructing this directly.
 */
class NavScopeImpl(
    override val scopeKeyForEntry: (Any) -> Any,
) : NavScope