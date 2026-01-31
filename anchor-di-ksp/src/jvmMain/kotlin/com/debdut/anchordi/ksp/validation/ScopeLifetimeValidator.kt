package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement

/**
 * Validates that a longer-lived scope cannot depend on a shorter-lived scope.
 * For example, a Singleton (application-wide) cannot depend on a ViewModel-scoped type,
 * because the ViewModel-scoped instance would not outlive the Singleton.
 */
object ScopeLifetimeValidator {

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

                val availableDeps = requiredBindings.filter { ScopeHierarchy.isAncestorOrSelf(it.component, requesterComponent) }
                if (availableDeps.isEmpty()) return@forEach // ParentComponentValidator handles "no binding in ancestor"

                val hasValidDependency = availableDeps.any { dep ->
                    ScopeHierarchy.longevityRank(dep.component, dep.scope) >= requesterLongevity
                }
                if (!hasValidDependency) {
                    val depBinding = availableDeps.first()
                    reporter.error(
                        "[Anchor DI] A longer-lived scope cannot depend on a shorter-lived scope. " +
                            "The binding for '$requester' (source: ${reqBinding.source}) is ${ScopeHierarchy.scopeDisplayName(requesterScope)} in ${requesterComponent.substringAfterLast('.')}, " +
                            "but it depends on '$requiredType' which is ${ScopeHierarchy.scopeDisplayName(depBinding.scope)}. " +
                            "Longer-lived bindings (e.g. Singleton) must not depend on shorter-lived ones (e.g. ViewModel-scoped), because the shorter-lived instance may not exist when the longer-lived one is created. " +
                            "Fix: move the dependency to a longer-lived scope, or inject Lazy<$requiredType> / AnchorProvider<$requiredType> to delay access.",
                        null
                    )
                }
            }
        }
    }
}
