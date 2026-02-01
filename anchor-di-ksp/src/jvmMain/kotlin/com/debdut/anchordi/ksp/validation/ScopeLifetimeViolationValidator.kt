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
        reporter: ValidationReporter
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
                        "[Anchor DI] A binding in a longer-lived or parent scope cannot depend on a type that is only bound in a child or shorter-lived scope. " +
                            "The binding for '$requester' (source: ${reqBinding.source}) is ${ScopeHierarchy.scopeDisplayName(requesterScope)} in ${requesterComponent.substringAfterLast('.')}, " +
                            "but it depends on '$requiredType' which is only bound in child component(s): $childComponents. " +
                            "The dependency would not be available when resolving '$requester', and a longer-lived binding must not hold a reference to a shorter-lived one. " +
                            "Fix: provide '$requiredType' in '${requesterComponent.substringAfterLast('.')}' or an ancestor, or move '$requester' to a scope that can see '$requiredType', or inject Lazy<$requiredType> / AnchorProvider<$requiredType> to delay access.",
                        null
                    )
                    return@forEach
                }

                val hasValidLongevity = visibleDeps.any { dep ->
                    ScopeHierarchy.longevityRank(dep.component, dep.scope) >= requesterLongevity
                }
                if (!hasValidLongevity) {
                    val depBinding = visibleDeps.first()
                    reporter.error(
                        "[Anchor DI] A longer-lived scope cannot depend on a shorter-lived scope. " +
                            "The binding for '$requester' (source: ${reqBinding.source}) is ${ScopeHierarchy.scopeDisplayName(requesterScope)} in ${requesterComponent.substringAfterLast('.')}, " +
                            "but it depends on '$requiredType' which is ${ScopeHierarchy.scopeDisplayName(depBinding.scope)}. " +
                            "Longer-lived bindings must not depend on shorter-lived ones, because the shorter-lived instance may not exist when the longer-lived one is created. " +
                            "Fix: move the dependency to a longer-lived scope, or inject Lazy<$requiredType> / AnchorProvider<$requiredType> to delay access.",
                        null
                    )
                }
            }
        }
    }
}
