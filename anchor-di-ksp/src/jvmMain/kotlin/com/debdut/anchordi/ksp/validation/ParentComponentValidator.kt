package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement

/**
 * Validates that a parent component cannot depend on a binding that exists only in a child component.
 * For example, SingletonComponent (root) cannot depend on a type that is only provided in ViewModelComponent,
 * because that type is not available in the Singleton scope.
 */
object ParentComponentValidator {

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
                val availableInAncestor = requiredBindings.any { ScopeHierarchy.isAncestorOrSelf(it.component, requesterComponent) }
                if (!availableInAncestor) {
                    val onlyInChild = requiredBindings.map { it.component }.distinct().joinToString { it.substringAfterLast('.') }
                    reporter.error(
                        "[Anchor DI] A parent component cannot depend on a binding that exists only in a child component. " +
                            "The binding for '$requester' (source: ${reqBinding.source}) is in component '${requesterComponent.substringAfterLast('.')}', " +
                            "but it depends on '$requiredType' which is only bound in child component(s): $onlyInChild. " +
                            "Parent scopes cannot see child scopes: the dependency would not be available when resolving '$requester'. " +
                            "Fix: install a module that provides '$requiredType' in '${requesterComponent.substringAfterLast('.')}' or an ancestor, or move '$requester' to a component that can see '$requiredType'.",
                        null
                    )
                }
            }
        }
    }
}
