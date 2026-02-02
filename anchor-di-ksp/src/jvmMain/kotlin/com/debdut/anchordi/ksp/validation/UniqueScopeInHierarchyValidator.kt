package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.ComponentDescriptor

/**
 * Validates that two components in the same hierarchy must not claim the same scope ID.
 * Each component has a scope identifier; duplicate scope IDs in the hierarchy would make
 * scope resolution ambiguous.
 */
object UniqueScopeInHierarchyValidator {
    fun validate(
        components: Map<String, ComponentDescriptor>,
        reporter: ValidationReporter,
    ) {
        val scopeIdToComponents = mutableMapOf<String, MutableList<String>>()
        components.keys.forEach { componentFqn ->
            val scopeId = ScopeHierarchy.scopeIdForComponent(componentFqn)
            scopeIdToComponents.getOrPut(scopeId) { mutableListOf() }.add(componentFqn)
        }
        scopeIdToComponents.forEach { (scopeId, componentFqns) ->
            if (componentFqns.size > 1) {
                val names = componentFqns.joinToString { it.substringAfterLast('.') }
                reporter.error(
                    ValidationMessageFormat.formatError(
                        summary = "Scope '$scopeId' is claimed by more than one component: $names.",
                        detail =
                            "Each component must have a unique scope identifier so that scoped bindings " +
                                "can be resolved unambiguously.",
                        fix =
                            "Ensure each @Component class (or built-in component) has a distinct scope; " +
                                "do not reuse the same scope ID for different components.",
                    ),
                    null,
                )
            }
        }
    }
}
