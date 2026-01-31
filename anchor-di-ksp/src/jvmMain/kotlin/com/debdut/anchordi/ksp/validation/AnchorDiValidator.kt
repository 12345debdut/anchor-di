package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import com.debdut.anchordi.ksp.model.ModuleDescriptor

/**
 * Orchestrator for all validations.
 */
class AnchorDiValidator(
    private val reporter: ValidationReporter
) {

    fun validateAll(
        bindings: List<BindingDescriptor>,
        injectClassDescriptors: List<InjectClassDescriptor>,
        moduleDescriptors: List<ModuleDescriptor>,
        components: Map<String, ComponentDescriptor>,
        providedKeys: Set<String>,
        requirements: List<DependencyRequirement>,
        dependencyGraph: Map<String, Set<String>>
    ) {
        // 1. Module Install-In
        ModuleInstallInValidator.validate(moduleDescriptors, components, reporter)

        // 2. Module Binds/Provides
        ModuleBindsValidator.validate(moduleDescriptors, reporter)

        // 3. AnchorViewModel rules
        AnchorViewModelValidator.validate(injectClassDescriptors, reporter)

        // 4. Duplicates
        DuplicateBindingValidator.validate(bindings, reporter)

        // 5. Scope compatibility
        ScopeComponentCompatibilityValidator.validate(bindings, reporter)

        // 6. Missing bindings
        MissingBindingValidator.validate(providedKeys, requirements, reporter)

        // 7. Cycles
        CycleValidator.validate(dependencyGraph, reporter)
    }
}
