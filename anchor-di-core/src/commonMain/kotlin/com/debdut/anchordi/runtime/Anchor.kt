package com.debdut.anchordi.runtime

import kotlin.concurrent.Volatile
import kotlin.synchronized

/**
 * Entry point for Anchor DI. Initialize at application startup, then use [inject] to resolve dependencies.
 *
 * ## Setup
 *
 * 1. Call [init] early in your application lifecycle (e.g. Application.onCreate on Android,
 *    or at app launch on iOS).
 * 2. The generated `AnchorGenerated` object will be passed to [init] - or you can pass
 *    contributors manually for testing.
 *
 * ## Usage
 *
 * ```
 * // At app startup
 * Anchor.init(AnchorGenerated)  // AnchorGenerated is KSP-generated
 *
 * // Resolve dependencies
 * val repository = Anchor.inject<Repository>()
 * val api = Anchor.inject<Api>("api")  // with qualifier
 * ```
 */
object Anchor {
    private val lock = Any()
    @Volatile
    private var container: AnchorContainer? = null
    
    // Reset listeners are called when Anchor.reset() is invoked.
    // Used by NavigationScopeRegistry to clear its state when the DI container is reset.
    private val resetListeners = mutableListOf<() -> Unit>()

    /**
     * Initializes the DI container with the given binding contributors.
     * Must be called before any [inject] calls.
     *
     * Thread-safe: can be called from any thread; concurrent calls will throw if already initialized.
     *
     * @param contributors One or more [ComponentBindingContributor] instances.
     *                     Typically the KSP-generated `AnchorGenerated` object.
     * @throws IllegalArgumentException if already initialized
     */
    fun init(vararg contributors: ComponentBindingContributor) {
        synchronized(lock) {
            require(container == null) {
                "Anchor is already initialized. Call init() only once."
            }
            container = AnchorContainer(contributors.toList())
        }
    }

    /**
     * Returns true if [init] has been called and the container is ready for [inject] / [withScope].
     * Useful for conditional init or tests.
     */
    fun isInitialized(): Boolean = container != null

    /**
     * Returns the container. Throws if not initialized.
     */
    fun requireContainer(): AnchorContainer {
        return container ?: error(
            "Anchor is not initialized. Call Anchor.init(contributors) at application startup " +
                "(e.g. in Application.onCreate() on Android, or before first composable)."
        )
    }

    /**
     * Resolves and returns an instance of [T].
     *
     * @throws IllegalStateException if Anchor is not initialized or no binding exists
     */
    inline fun <reified T : Any> inject(): T = requireContainer().get<T>()

    /**
     * Resolves and returns an instance of [T] with the given qualifier.
     */
    inline fun <reified T : Any> inject(qualifier: String): T = requireContainer().get<T>(qualifier)

    /**
     * Resolves and returns a multibound [Set] of [T]. Use when you have @IntoSet contributions.
     */
    inline fun <reified T : Any> injectSet(): Set<T> = requireContainer().getSet<T>()

    /**
     * Resolves and returns a multibound [Map] with [String] keys and value type [V]. Use when you have
     * @IntoMap with @StringKey contributions.
     */
    inline fun <reified V : Any> injectMap(): Map<String, V> = requireContainer().getMap<V>()

    /**
     * Returns an [AnchorProvider] for [T] that supplies instances on demand.
     */
    inline fun <reified T : Any> provider(): AnchorProvider<T> = requireContainer().provider<T>()

    /**
     * Executes [block] within a custom scope. Use for [Scoped] bindings.
     *
     * Supports nested scopes (component dependencies): child scopes can access
     * parent bindings. Example:
     * ```
     * Anchor.withScope(SessionScope::class) { session ->
     *     session.createScope(ScreenScope::class) { screen ->
     *         // screen can resolve SessionScope and ScreenScope bindings
     *     }
     * }
     * ```
     */
    inline fun <R> withScope(scopeClass: kotlin.reflect.KClass<*>, block: (AnchorContainer) -> R): R =
        requireContainer().createScope(scopeClass, block)

    /**
     * Executes [block] within a scope identified by [scopeId].
     * Use when the scope ID must match exactly (e.g. [ViewModelComponent.SCOPE_ID] on Kotlin/JS
     * where KClass.qualifiedName may be null).
     */
    fun <R> withScope(scopeId: String, block: (AnchorContainer) -> R): R =
        requireContainer().createScope(scopeId, block)

    /**
     * Creates a scoped container that you own and manage. Use when you need to hold the scope
     * and resolve dependencies over time (e.g. Activity, screen, session). Release the reference
     * when the scope should end.
     */
    fun scopedContainer(scopeClass: kotlin.reflect.KClass<*>): AnchorContainer =
        requireContainer().createScopeContainer(scopeClass)

    /**
     * Creates a scoped container identified by [scopeId]. Use when the scope ID must match
     * exactly (e.g. custom component scope names from KSP).
     */
    fun scopedContainer(scopeId: String): AnchorContainer =
        requireContainer().createScopeContainer(scopeId)

    /**
     * Returns the underlying container for advanced use cases.
     * Prefer [inject] for normal dependency resolution.
     */
    fun getContainer(): AnchorContainer = requireContainer()

    /**
     * Registers a callback to be invoked when [reset] is called.
     *
     * Used internally by NavigationScopeRegistry to clear its state when the DI container
     * is reset. This ensures that scopes don't hold references to orphaned containers.
     *
     * Thread-safe: can be called from any thread.
     *
     * @param listener Callback to invoke on reset. Called within the synchronized block.
     */
    fun addResetListener(listener: () -> Unit) {
        synchronized(lock) {
            resetListeners.add(listener)
        }
    }

    /**
     * Resets the container. Use in tests to allow re-initialization with test doubles.
     *
     * This also invokes all registered reset listeners (e.g., NavigationScopeRegistry.clear())
     * to ensure clean state between tests.
     *
     * Thread-safe: can be called from any thread.
     *
     * Example:
     * ```
     * @After
     * fun tearDown() {
     *     Anchor.reset()
     * }
     *
     * @Test
     * fun testWithMock() {
     *     Anchor.init(TestContributor)  // Uses mock implementations
     *     // ... test
     * }
     * ```
     */
    fun reset() {
        synchronized(lock) {
            // Clear all dependent registries first
            resetListeners.forEach { it() }
            // Note: We don't clear resetListeners here because they are registered once
            // during object initialization (e.g., NavigationScopeRegistry.init) and should
            // persist across reset cycles. Clearing them would break the automatic cleanup
            // on subsequent resets.
            container = null
        }
    }
}
