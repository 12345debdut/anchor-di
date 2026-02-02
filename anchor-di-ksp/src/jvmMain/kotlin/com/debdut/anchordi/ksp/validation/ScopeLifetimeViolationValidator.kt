package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement

/**
 * Validates that a binding never depends on another binding with a strictly shorter effective lifetime.
 *
 * This is a single generic rule that covers:
 * 1. **Parent/child visibility**: A binding in a parent component cannot depend on a type that is only
 *    bound in a child component (the dependency would not be visible and would violate lifetime).
 * 2. **Same/ancestor but shorter scope**: A binding with longer longevity (e.g. Singleton) cannot
 *    depend on a binding with shorter longevity (e.g. ViewModel-scoped) even when both are in the
 *    same component or the dependency is in an ancestor.
 *
 * Uses [ScopeHierarchy] for component ancestry and longevity rank — no hardcoded scope names.
 */
object ScopeLifetimeViolationValidator {
    fun validate(
        bindings: List<BindingDescriptor>,
        requirements: List<DependencyRequirement>,
        reporter: ValidationReporter,
    ) {
        val keyToBindings = bindings.groupBy { it.key }
        requirements.forEach { (requiredType, requester) ->
            if (requiredType in ValidationConstants.SKIPPED_TYPES) return@forEach
            val requesterBindings = keyToBindings[requester] ?: return@forEach
            val requiredBindings = keyToBindings[requiredType] ?: return@forEach
            if (requiredBindings.isEmpty()) return@forEach // MissingBindingValidator reports missing binding

            requesterBindings.forEach { reqBinding ->
                val requesterComponent = reqBinding.component
                val requesterScope = reqBinding.scope
                val requesterLongevity = ScopeHierarchy.longevityRank(requesterComponent, requesterScope)

                val visibleDeps = requiredBindings.filter { ScopeHierarchy.isAncestorOrSelf(it.component, requesterComponent) }
                if (visibleDeps.isEmpty()) {
                    val childComponents = requiredBindings.map { it.component }.distinct().joinToString { it.substringAfterLast('.') }
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary =
                                "Binding for '$requester' (${ScopeHierarchy.scopeDisplayName(requesterScope)} " +
                                    "in ${requesterComponent.substringAfterLast('.')}) depends on '$requiredType', " +
                                    "which is only bound in child component(s): $childComponents.",
                            detail =
                                "Source: ${reqBinding.source}. The dependency would not be available when " +
                                    "resolving '$requester'; a longer-lived binding must not hold a reference to a shorter-lived one.",
                            fix =
                                "Provide '$requiredType' in '${requesterComponent.substringAfterLast('.')}' or an ancestor, " +
                                    "or move '$requester' to a scope that can see '$requiredType', or inject " +
                                    "Lazy<$requiredType> / AnchorProvider<$requiredType> to delay access.",
                        ),
                        null,
                    )
                    return@forEach
                }

                val hasValidLongevity =
                    visibleDeps.any { dep ->
                        ScopeHierarchy.longevityRank(dep.component, dep.scope) >= requesterLongevity
                    }
                if (!hasValidLongevity) {
                    val depBinding = visibleDeps.first()
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary =
                                "Binding for '$requester' (${ScopeHierarchy.scopeDisplayName(requesterScope)}) " +
                                    "depends on '$requiredType' (${ScopeHierarchy.scopeDisplayName(depBinding.scope)}); " +
                                    "longer-lived scope cannot depend on shorter-lived scope.",
                            detail =
                                "Source: ${reqBinding.source}. The shorter-lived instance may not exist " +
                                    "when the longer-lived one is created.",
                            fix =
                                "Move the dependency to a longer-lived scope, or inject " +
                                    "Lazy<$requiredType> / AnchorProvider<$requiredType> to delay access.",
                        ),
                        null,
                    )
                }
            }
        }
    }
}
