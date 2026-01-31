package com.debdut.anchordi.navigation

import com.debdut.anchordi.runtime.AnchorContainer

/**
 * Holds the [NavigationComponent] and [ViewModelComponent] containers for one navigation entry.
 *
 * Obtained from [NavigationScopeRegistry.getOrCreate]. When the navigation destination is left,
 * call [NavigationScopeRegistry.dispose] with the same scope key so this entry is released.
 *
 * Use [navContainer] for [NavigationComponent]-scoped bindings.
 * Use [viewModelContainer] for [ViewModelComponent]-scoped bindings (e.g. ViewModels per destination).
 */
data class NavigationScopeEntry(
    val navContainer: AnchorContainer,
    val viewModelContainer: AnchorContainer,
)
