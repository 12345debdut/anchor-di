package com.debdut.anchordi.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import com.debdut.anchordi.navigation.ViewModelScopeRegistry
import com.debdut.anchordi.runtime.AnchorContainer

private val observedOwners = mutableSetOf<ViewModelStoreOwner>()

/**
 * Returns the ViewModel-scoped container for [owner], and registers [lifecycle] so the scope
 * is disposed when the owner is destroyed (ON_DESTROY). Use in Activity or Fragment to get
 * one ViewModel scope per owner; ViewModel-scoped bindings are cleared when the owner is destroyed.
 *
 * Call once per owner (e.g. in onCreate); the returned container is stable for the owner's lifetime.
 *
 * Example (Activity):
 * ```
 * class MainActivity : ComponentActivity() {
 *     private val viewModelScope by lazy { getViewModelScope(this, lifecycle) }
 *     override fun onCreate(...) {
 *         val vm = viewModelScope.get<MainViewModel>()
 *     }
 * }
 * ```
 *
 * Example (Fragment):
 * ```
 * val viewModelScope = getViewModelScope(this, viewLifecycleOwner.lifecycle)
 * val vm = viewModelScope.get<MyViewModel>()
 * ```
 */
fun getViewModelScope(owner: ViewModelStoreOwner, lifecycle: Lifecycle): AnchorContainer {
    val container = ViewModelScopeRegistry.getOrCreate(owner)
    if (owner !in observedOwners) {
        observedOwners.add(owner)
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                ViewModelScopeRegistry.dispose(owner)
                observedOwners.remove(owner)
            }
        })
    }
    return container
}

/**
 * Returns the ViewModel-scoped container for this [LifecycleOwner], and registers its [Lifecycle]
 * so the scope is disposed when the owner is destroyed. Use when [this] is also a [ViewModelStoreOwner]
 * (e.g. [androidx.activity.ComponentActivity], [androidx.fragment.app.Fragment]).
 *
 * Example (ComponentActivity):
 * ```
 * class MainActivity : ComponentActivity() {
 *     private val viewModelScope by lazy { viewModelScope() }
 *     override fun onCreate(...) {
 *         val vm = viewModelScope.get<MainViewModel>()
 *     }
 * }
 * ```
 */
fun LifecycleOwner.viewModelScope(): AnchorContainer {
    return getViewModelScope(this as ViewModelStoreOwner, lifecycle)
}
