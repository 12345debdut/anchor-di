package com.debdut.anchordi.ksp.analysis

import com.debdut.anchordi.ksp.hasAnnotation
import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Single source of truth for "what is a component" in Anchor DI.
 *
 * A component is any type annotated with [com.debdut.anchordi.Component] (built-in or custom).
 * No hardcoded component names: discovery is done via KSP symbols.
 */
object ComponentResolution {

    /** FQN of the @Component annotation. Only place this string appears for component detection. */
    const val FQN_COMPONENT = "com.debdut.anchordi.Component"

    /**
     * Returns true if this class is a DI component (has @Component).
     * Built-in (SingletonComponent, ViewModelComponent, NavigationComponent) and custom components.
     */
    fun KSClassDeclaration.isComponent(): Boolean =
        hasAnnotation(FQN_COMPONENT)

    /**
     * Discovers all component FQNs on the processor classpath via @Component.
     * Includes built-ins from anchor-di-api and any custom components.
     */
    fun Resolver.discoverComponentFqns(): Set<String> =
        getSymbolsWithAnnotation(FQN_COMPONENT)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { it.qualifiedName?.asString() }
            .toSet()

    /**
     * Builds the component descriptor map from discovered symbols only.
     * No hardcoded component list.
     */
    fun Resolver.discoverComponentDescriptors(): Map<String, ComponentDescriptor> =
        discoverComponentFqns().associateWith { ComponentDescriptor(fqn = it, dependencies = emptySet()) }

}
