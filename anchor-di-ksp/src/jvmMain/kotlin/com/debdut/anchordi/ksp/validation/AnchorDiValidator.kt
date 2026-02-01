package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import com.debdut.anchordi.ksp.model.ModuleDescriptor
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Orchestrator for all validations. Uses [ValidationPhase] ordering (SYMBOL then MODEL)
 * and [ValidationPipeline] for named, modular passes.
 *
 * **Phase order:**
 * 1. [ValidationPhase.SYMBOL] — symbol-level (inject/module structure and annotations)
 * 2. [ValidationPhase.MODEL] — model-level (bindings, components, dependencies, lifetime)
 *
 * **Symbol passes:** MultipleInjectConstructor, InjectableClassKind, ConstructorAccessibility,
 * BindsImplementation, SingleScope.
 *
 * **Model passes:** ModuleInstallIn, ModuleBinds, AnchorViewModel, DuplicateBinding,
 * UniqueScopeInHierarchy, ScopeComponentCompatibility, ScopeLifetimeViolation, MissingBinding,
 * Cycle, ReachableBindings.
 */
class AnchorDiValidator(
    private val reporter: ValidationReporter
) {

    /**
     * Runs [ValidationPhase.SYMBOL] passes. Call before [validateAll].
     */
    fun validateSymbols(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>
    ) {
        ValidationPipeline.runSymbolPhase(injectClasses.distinct(), moduleClasses, reporter)
    }

    /**
     * Runs [ValidationPhase.MODEL] passes. Call after model build and after [validateSymbols].
     */
    fun validateAll(
        bindings: List<BindingDescriptor>,
        injectClassDescriptors: List<InjectClassDescriptor>,
        moduleDescriptors: List<ModuleDescriptor>,
        components: Map<String, ComponentDescriptor>,
        providedKeys: Set<String>,
        requirements: List<DependencyRequirement>,
        dependencyGraph: Map<String, Set<String>>
    ) {
        val context = ValidationModelContext(
            bindings = bindings,
            injectClassDescriptors = injectClassDescriptors,
            moduleDescriptors = moduleDescriptors,
            components = components,
            providedKeys = providedKeys,
            requirements = requirements,
            dependencyGraph = dependencyGraph
        )
        ValidationPipeline.runModelPhase(context, reporter)
    }
}
