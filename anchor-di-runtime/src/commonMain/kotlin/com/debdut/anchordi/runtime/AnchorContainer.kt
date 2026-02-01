package com.debdut.anchordi.runtime

import kotlin.reflect.KClass

/**
 * The dependency injection container. Holds all bindings and resolves dependencies.
 *
 * Typically accessed through [Anchor]. Use [Anchor.init] to bootstrap the container
 * with generated binding contributors.
 */
class AnchorContainer(
    private val contributors: List<ComponentBindingContributor>,
    private val parent: AnchorContainer? = null,
    private val currentScopeId: String? = null,
    private val inheritedBindings: Map<Key, Binding>? = null
) {
    val bindings = inheritedBindings?.toMutableMap() ?: mutableMapOf()
    private val singletonCache = mutableMapOf<Key, Any>()
    private val scopedCache = mutableMapOf<Key, Any>()
    private var initialized = false

    init {
        initialize()
    }

    private fun initialize() {
        if (initialized) return
        if (inheritedBindings == null) {
            val setContributions = mutableMapOf<Key, MutableList<Factory<Any>>>()
            val mapContributions = mutableMapOf<Key, MutableList<Pair<Any, Factory<Any>>>>()
            val registry = object : BindingRegistry {
                override fun register(key: Key, binding: Binding) {
                    bindings[key] = binding
                }
                override fun registerSetContribution(key: Key, factory: Factory<Any>) {
                    setContributions.getOrPut(key) { mutableListOf() }.add(factory)
                }
                override fun registerMapContribution(key: Key, mapKey: Any, factory: Factory<Any>) {
                    mapContributions.getOrPut(key) { mutableListOf() }.add(mapKey to factory)
                }
            }
            contributors.forEach { it.contribute(registry) }
            setContributions.forEach { (key, list) ->
                bindings[key] = Binding.MultibindingSet(list)
            }
            mapContributions.forEach { (key, list) ->
                bindings[key] = Binding.MultibindingMap(list)
            }
        }
        initialized = true
    }

    /**
     * Returns an instance of the type identified by [key].
     * @throws IllegalStateException if no binding exists for the key
     */
    fun get(key: Key): Any {
        val binding = bindings[key]
            ?: throw IllegalStateException(
                "No binding found for $key. Possible causes:\n" +
                    "  • Add @Inject to the class constructor, or\n" +
                    "  • Add @Provides in a @Module, or\n" +
                    "  • Add @Binds for interface types, or\n" +
                    "  • Ensure the module is @InstallIn(SingletonComponent::class), @InstallIn(ViewModelComponent::class), or @InstallIn(YourScope::class)\n" +
                    "  • Rebuild the project (KSP generates bindings at compile time)"
            )
        return when (binding) {
            is Binding.Unscoped -> binding.factory.create(resolveContainer(binding))
            is Binding.Singleton -> resolveSingleton(key, binding)
            is Binding.Scoped -> resolveScoped(key, binding)
            is Binding.MultibindingSet -> resolveMultibindingSet(key, binding)
            is Binding.MultibindingMap -> resolveMultibindingMap(key, binding)
        }
    }

    private fun resolveContainer(binding: Binding): AnchorContainer {
        return when (binding) {
            is Binding.Scoped -> if (currentScopeId == binding.scopeClassName) this else (parent ?: this)
            else -> this
        }
    }

    private fun resolveSingleton(key: Key, binding: Binding.Singleton): Any {
        val root = parent?.let { p -> generateSequence(p) { it.parent }.lastOrNull() } ?: this
        return root.singletonCache.getOrPut(key) { binding.factory.create(root) }
    }

    private fun resolveScoped(key: Key, binding: Binding.Scoped): Any {
        if (currentScopeId != binding.scopeClassName) {
            if (parent == null) {
                val hint = when (binding.scopeClassName) {
                    "com.debdut.anchordi.ViewModelComponent" ->
                        " Use viewModelAnchor() to create ViewModels, or Anchor.withScope(ViewModelComponent::class) { ... }."
                    "com.debdut.anchordi.NavigationComponent" ->
                        " Wrap destination content in NavigationScopedContent(scopeKey) { ... } (anchor-di-navigation) and use navigationScopedInject() inside it."
                    else ->
                        " Use Anchor.withScope(${binding.scopeClassName}::class) { ... } or Anchor.scopedContainer(...) to provide the scope."
                }
                throw IllegalStateException(
                    "Scoped binding for $key requires a scope.$hint"
                )
            }
            return parent.get(key)
        }
        return scopedCache.getOrPut(key) { binding.factory.create(this) }
    }

    private fun resolveMultibindingSet(key: Key, binding: Binding.MultibindingSet): Any {
        val root = parent?.let { p -> generateSequence(p) { it.parent }.lastOrNull() } ?: this
        return root.singletonCache.getOrPut(key) {
            val container = resolveContainer(binding)
            binding.contributions.map { it.create(container) }.toSet()
        }
    }

    private fun resolveMultibindingMap(key: Key, binding: Binding.MultibindingMap): Any {
        val root = parent?.let { p -> generateSequence(p) { it.parent }.lastOrNull() } ?: this
        return root.singletonCache.getOrPut(key) {
            val container = resolveContainer(binding)
            binding.contributions.associate { (mapKey, factory) ->
                mapKey to factory.create(container)
            }
        }
    }

    /**
     * Creates a child scope (component dependency). The child inherits all bindings
     * and can resolve parent's Singleton/Unscoped bindings. Scoped bindings for
     * [scopeClass] are cached per child instance.
     *
     * Supports nesting: call createScope on the result for nested component hierarchy.
     *
     * @param scopeClass The scope class (e.g. MyScreenScope::class)
     * @param block Executes with the scoped container; scoped bindings are cached per invocation
     */
    inline fun <R> createScope(scopeClass: KClass<*>, block: (AnchorContainer) -> R): R {
        return block(createScopeContainer(scopeClass))
    }

    /**
     * Creates a child scope with the given [scopeId] and runs [block] with it.
     * Use when the scope ID must match exactly (e.g. ViewModelComponent.SCOPE_ID).
     */
    fun <R> createScope(scopeId: String, block: (AnchorContainer) -> R): R {
        return block(createScopeContainer(scopeId))
    }

    /**
     * Creates a scoped container for [scopeClass] and returns it. Use when you need to hold
     * the scope and manage its lifecycle yourself.
     */
    fun createScopeContainer(scopeClass: KClass<*>): AnchorContainer {
        val scopeId = scopeClass.qualifiedName ?: error("Scope class must have qualified name")
        return AnchorContainer(emptyList(), this, scopeId, bindings)
    }

    /**
     * Creates a scoped container for [scopeId] and returns it.
     */
    fun createScopeContainer(scopeId: String): AnchorContainer {
        return AnchorContainer(emptyList(), this, scopeId, bindings)
    }

    /**
     * Returns a [AnchorProvider] that supplies instances of [T] from this container.
     */
    inline fun <reified T : Any> provider(): AnchorProvider<T> {
        return object : AnchorProvider<T> {
            override fun get(): T = get<T>()
        }
    }

    /**
     * Returns an instance of [T]. Uses the fully-qualified class name as the key.
     * For qualified bindings, use [get] with an explicit [Key].
     *
     * This requires [reified] to obtain the type at compile time (no reflection on Native).
     */
    inline fun <reified T : Any> get(): T {
        val key = Key(T::class.qualifiedName ?: error("Class ${T::class} has no qualified name"))
        @Suppress("UNCHECKED_CAST")
        return get(key) as T
    }

    /**
     * Returns an instance of [T] with the given qualifier.
     */
    inline fun <reified T : Any> get(qualifier: String): T {
        val key = Key(T::class.qualifiedName ?: error("Class ${T::class} has no qualified name"), qualifier)
        @Suppress("UNCHECKED_CAST")
        return get(key) as T
    }

    /**
     * Returns a multibound [Set] of [T]. Use when you have @IntoSet contributions;
     * the key is built as `Set<T>` so it matches the generated multibinding key.
     */
    inline fun <reified T : Any> getSet(): Set<T> {
        val elementName = T::class.qualifiedName ?: error("Class ${T::class} has no qualified name")
        val key = Key("kotlin.collections.Set<$elementName>", null)
        @Suppress("UNCHECKED_CAST")
        return get(key) as Set<T>
    }

    /**
     * Returns a multibound [Map] with [String] keys and value type [V]. Use when you have
     * @IntoMap with @StringKey contributions.
     */
    inline fun <reified V : Any> getMap(): Map<String, V> {
        val valueName = V::class.qualifiedName ?: error("Class ${V::class} has no qualified name")
        val key = Key("kotlin.collections.Map<kotlin.String,$valueName>", null)
        @Suppress("UNCHECKED_CAST")
        return get(key) as Map<String, V>
    }
}
