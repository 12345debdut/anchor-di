package com.debdut.anchordi.runtime

/**
 * Implemented by generated code to register all bindings for a component.
 *
 * The KSP processor generates an object that implements this interface and
 * registers all [Inject] constructors, [Provides], and [Binds] from modules
 * installed in the component.
 */
interface ComponentBindingContributor {
    fun contribute(registry: BindingRegistry)
}
