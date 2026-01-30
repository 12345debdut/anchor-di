package com.debdut.anchordi.runtime

/**
 * Represents a binding in the container: how to create an instance for a [Key].
 */
sealed class Binding {
    /**
     * Unscoped: create a new instance on each request.
     */
    data class Unscoped(val factory: Factory<Any>) : Binding()

    /**
     * Singleton: create once, reuse for all requests.
     */
    data class Singleton(val factory: Factory<Any>) : Binding()

    /**
     * Custom scope: create once per scope instance.
     * @param scopeClassName Fully-qualified name of the scope class
     */
    data class Scoped(val scopeClassName: String, val factory: Factory<Any>) : Binding()
}
