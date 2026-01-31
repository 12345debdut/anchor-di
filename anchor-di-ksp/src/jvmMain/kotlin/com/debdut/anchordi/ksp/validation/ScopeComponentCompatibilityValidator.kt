package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor

/**
 * Validates that scope annotations are allowed for the component
 * (e.g. SingletonComponent only allows @Singleton, ViewModelComponent only @ViewModelScoped).
 */
object ScopeComponentCompatibilityValidator {

    fun validate(bindings: List<BindingDescriptor>, reporter: ValidationReporter) {
        bindings.forEach { binding ->
            if (!isScopeAllowed(binding.component, binding.scope)) {
                val componentName = binding.component.substringAfterLast('.')
                val scopeName = binding.scope?.substringAfterLast('.') ?: "unscoped"
                reporter.error(
                    "[Anchor DI] Scope not allowed in this component: the scope '$scopeName' cannot be used in component '$componentName'. " +
                        "Binding source: ${binding.source}. " +
                        "Each component only allows specific scopes (e.g. SingletonComponent allows only @Singleton, ViewModelComponent only @ViewModelScoped). " +
                        "Fix: remove the scope annotation from this binding or install it in a component that allows this scope.",
                    null
                )
            }
        }
    }

    private fun isScopeAllowed(component: String, scope: String?): Boolean {
        if (scope == null) return true
        if (component == ValidationConstants.FQN_SINGLETON_COMPONENT) return scope == ValidationConstants.FQN_SINGLETON
        if (component == ValidationConstants.FQN_VIEW_MODEL_COMPONENT) return scope == ValidationConstants.FQN_VIEW_MODEL_SCOPED
        if (component == ValidationConstants.FQN_NAVIGATION_COMPONENT) return scope == ValidationConstants.FQN_NAVIGATION_SCOPED
        return true // custom component: any scope from @Scoped is allowed
    }
}
