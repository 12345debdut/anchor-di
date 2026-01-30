package com.debdut.anchordi.runtime

/**
 * Interface for registering bindings. Used by generated code to contribute
 * factories to the container.
 */
interface BindingRegistry {
    fun register(key: Key, binding: Binding)
}
