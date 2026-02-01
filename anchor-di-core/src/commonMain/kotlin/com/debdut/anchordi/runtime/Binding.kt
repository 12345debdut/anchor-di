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

    /**
     * Multibinding into a Set: multiple contributions are merged into one set.
     * Key is the Set element type (e.g. "kotlin.collections.Set<com.example.Tracker>").
     */
    data class MultibindingSet(val contributions: List<Factory<Any>>) : Binding()

    /**
     * Multibinding into a Map: multiple contributions (each with a map key) are merged into one map.
     * Key is the Map type (e.g. "kotlin.collections.Map<kotlin.String, com.example.Tracker>").
     */
    data class MultibindingMap(val contributions: List<Pair<Any, Factory<Any>>>) : Binding()
}
