package com.debdut.anchordi.runtime

/**
 * Interface for registering bindings. Used by generated code to contribute
 * factories to the container.
 *
 * For multibindings, use [registerSetContribution] or [registerMapContribution];
 * the container merges all contributions into a single Set or Map binding.
 */
interface BindingRegistry {
    fun register(key: Key, binding: Binding)

    /**
     * Contributes one element to a multibound [Set] for the given key.
     * The key should be the full type name of the set (e.g. "kotlin.collections.Set<com.example.Tracker>").
     */
    fun registerSetContribution(key: Key, factory: Factory<Any>)

    /**
     * Contributes one entry to a multibound [Map] for the given key.
     * The key should be the full type name of the map (e.g. "kotlin.collections.Map<kotlin.String, com.example.Tracker>").
     * @param mapKey The key for this entry in the map (e.g. from [com.debdut.anchordi.StringKey]).
     */
    fun registerMapContribution(key: Key, mapKey: Any, factory: Factory<Any>)
}
