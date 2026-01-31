package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import com.debdut.anchordi.ksp.model.ModuleDescriptor
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Orchestrator for all validations.
 *
 * Symbol-level validations (run first):
 * - Multiple @Inject constructors per class
 * - Injectable class kind (not abstract, interface, object, enum)
 * - Constructor accessibility (@Inject constructor must be public)
 * - @Binds implementation type must be concrete
 * - Single scope per binding (at most one of @Singleton, @ViewModelScoped, @NavigationScoped, @Scoped)
 *
 * Model-level validations:
 * - Duplicate binding (each key exactly one binding per component)
 * - Two components must not claim the same scope in the same hierarchy
 * - Scope compatibility (component allows the scope)
 * - Parent component cannot depend on child binding
 * - Longer-lived scope cannot depend on shorter-lived scope
 * - Missing dependency binding
 * - Binding cannot depend on itself (cycles)
 * - Only bindings reachable from entry point (warn)
 */
class AnchorDiValidator(
    private val reporter: ValidationReporter
) {

    /**
     * Validations that require KSP symbols (inject and module classes).
     * Call before [validateAll].
     */
    fun validateSymbols(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>
    ) {
        val distinctInject = injectClasses.distinct()
        MultipleInjectConstructorValidator.validate(distinctInject, reporter)
        InjectableClassKindValidator.validate(distinctInject, reporter)
        ConstructorAccessibilityValidator.validate(distinctInject, reporter)
        BindsImplementationValidator.validate(moduleClasses, reporter)
        SingleScopeValidator.validate(distinctInject, moduleClasses, reporter)
    }

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

        // 4. Duplicate binding (each key exactly one binding per component)
        DuplicateBindingValidator.validate(bindings, reporter)

        // 5. Single scope per binding (also checked at symbol level; model-level double-check)
        // Handled in SingleScopeValidator (symbol level)

        // 6. Two components must not claim the same scope in the same hierarchy
        UniqueScopeInHierarchyValidator.validate(components, reporter)

        // 7. Scope compatibility (component allows the scope)
        ScopeComponentCompatibilityValidator.validate(bindings, reporter)

        // 8. Parent component cannot depend on child binding
        ParentComponentValidator.validate(bindings, requirements, reporter)

        // 9. Longer-lived scope cannot depend on shorter-lived scope
        ScopeLifetimeValidator.validate(bindings, requirements, reporter)

        // 10. Missing dependency binding
        MissingBindingValidator.validate(providedKeys, requirements, reporter)

        // 11. Cycles (binding cannot depend on itself)
        CycleValidator.validate(dependencyGraph, reporter)

        // 12. Only bindings reachable from entry point (warn)
        ReachableBindingsValidator.validate(bindings, dependencyGraph, reporter)
    }
}
