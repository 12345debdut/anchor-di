package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor

/**
 * Validates that a binding's scope is allowed in its component (module's @InstallIn).
 * Rule: single source of truth in [ScopeHierarchy.scopeAllowedInComponent].
 * E.g. @ViewModelScoped in a module @InstallIn(SingletonComponent::class) → error.
 */
object ScopeComponentCompatibilityValidator {

    fun validate(bindings: List<BindingDescriptor>, reporter: ValidationReporter) {
        bindings.forEach { binding ->
            if (!ScopeHierarchy.scopeAllowedInComponent(binding.component, binding.scope)) {
                val componentName = binding.component.substringAfterLast('.')
                val scopeName = ScopeHierarchy.scopeDisplayName(binding.scope)
                reporter.error(
                    "[Anchor DI] Scope not allowed in this component: the scope '$scopeName' cannot be used in component '$componentName'. " +
                        "Binding source: ${binding.source}. " +
                        "The binding is in a module installed in $componentName but uses a scope that belongs to a different component. " +
                        "Fix: remove the scope annotation from this binding, or install the module in the component that allows this scope (e.g. @InstallIn(ViewModelComponent::class) for @ViewModelScoped).",
                    null
                )
            }
        }
    }
}
