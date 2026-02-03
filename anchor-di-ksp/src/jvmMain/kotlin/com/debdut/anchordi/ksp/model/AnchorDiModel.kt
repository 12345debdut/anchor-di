package com.debdut.anchordi.ksp.model

/**
 * Descriptor for a DI component (SingletonComponent, ViewModelComponent, etc.).
 * Used by validators; built from KSP symbols or from built-in constants.
 */
data class ComponentDescriptor(
    val fqn: String,
    val dependencies: Set<String> = emptySet(),
)

/**
 * Descriptor for a single binding (key + qualifier + component + scope).
 * Used by validators and codegen; built from @Inject classes or @Provides/@Binds.
 *
 * For multibindings: [multibindingKind] is "set" or "map"; multiple descriptors
 * can share the same key (contributions). For map, [mapKey] must be unique per key.
 */
data class BindingDescriptor(
    val key: String,
    val qualifier: String?,
    val component: String,
    /** null = unscoped */
    val scope: String?,
    val source: String,
    /** "set" or "map" */
    val multibindingKind: String? = null,
    /** For map multibinding, the key value (e.g. from @StringKey). */
    val mapKey: String? = null,
)

/**
 * Descriptor for a module's @Binds method, for validation only.
 */
data class BindsMethodDescriptor(
    val moduleName: String,
    val methodName: String,
    val parameterCount: Int,
)

/**
 * Descriptor for a module (for InstallIn and binds validation).
 */
data class ModuleDescriptor(
    val moduleName: String,
    val installInComponentFqn: String?,
    val hasProvidesOrBinds: Boolean,
    val bindsMethods: List<BindsMethodDescriptor>,
)

/**
 * Descriptor for dependency requirement (required type, requester).
 */
data class DependencyRequirement(
    val requiredType: String,
    val requester: String,
)
