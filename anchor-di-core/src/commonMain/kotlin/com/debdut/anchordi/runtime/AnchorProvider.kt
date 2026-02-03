package com.debdut.anchordi.runtime

/**
 * A factory that provides instances of [T]. Each call to [get] returns
 * a new instance for unscoped bindings, or the same instance for singletons.
 *
 * Use when you need to obtain instances on demand rather than at construction.
 *
 * Example:
 * ```
 * class MyClass @Inject constructor(
 *     private val apiProvider: AnchorProvider<Api>
 * ) {
 *     fun doWork() {
 *         val api = apiProvider.get()
 *     }
 * }
 * ```
 */
interface AnchorProvider<out T : Any> {
    fun get(): T
}
