package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor

/**
 * Validates that only bindings reachable from a component entry point are considered valid.
 * Entry points = bindings that are not a dependency of any other binding in the same component.
 * Unreachable bindings (not depended on by any other binding and not an entry point) may be dead code.
 */
object ReachableBindingsValidator {
    fun validate(
        bindings: List<BindingDescriptor>,
        dependencyGraph: Map<String, Set<String>>,
        reporter: ValidationReporter,
    ) {
        val bindingsByComponent = bindings.groupBy { it.component }
        val dependentsOf = mutableMapOf<String, MutableSet<String>>()
        dependencyGraph.forEach { (from, deps) ->
            deps.forEach { to ->
                dependentsOf.getOrPut(to) { mutableSetOf() }.add(from)
            }
        }

        bindingsByComponent.forEach { (component, componentBindings) ->
            val keysInComponent = componentBindings.map { it.key }.toSet()
            val dependentsInComponent = dependencyGraph.filter { it.key in keysInComponent }
            val entryPoints =
                keysInComponent.filter { key ->
                    val dependents = dependentsOf[key] ?: emptySet()
                    dependents.none { it in keysInComponent }
                }
            val reachable = mutableSetOf<String>()

            fun visit(key: String) {
                if (key in reachable) return
                reachable.add(key)
                dependencyGraph[key]?.forEach { dep -> if (dep in keysInComponent) visit(dep) }
            }
            entryPoints.forEach { visit(it) }

            val unreachable = keysInComponent - reachable
            unreachable.forEach { key ->
                val binding = componentBindings.firstOrNull { it.key == key } ?: return@forEach
                reporter.warn(
                    ValidationMessageFormat.formatWarn(
                        summary =
                            "Binding for '$key' (source: ${binding.source}) in component " +
                                "'${component.substringAfterLast('.')}' is not reachable from any entry point.",
                        detail =
                            "Only bindings reachable from a component entry point are used at runtime. " +
                                "This binding may be dead code if nothing depends on it and it is not injected directly.",
                        fix =
                            "Ensure it is either a dependency of another binding in this component or is " +
                                "requested via Anchor.inject<>() / viewModelAnchor() etc. in the same scope.",
                    ),
                    null,
                )
            }
        }
    }
}
