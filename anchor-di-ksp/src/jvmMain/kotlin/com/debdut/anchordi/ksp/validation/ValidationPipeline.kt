package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Explicit phase ordering and named validation passes. Run [ValidationPhase.SYMBOL] first,
 * then [ValidationPhase.MODEL]. Within each phase, passes run in list order.
 */
object ValidationPipeline {
    private val symbolPasses: List<SymbolValidationPass> =
        listOf(
            SymbolValidationPass("MultipleInjectConstructor") { inject, _, r ->
                MultipleInjectConstructorValidator.validate(inject, r)
            },
            SymbolValidationPass("InjectableClassKind") { inject, _, r ->
                InjectableClassKindValidator.validate(inject, r)
            },
            SymbolValidationPass("ConstructorAccessibility") { inject, _, r ->
                ConstructorAccessibilityValidator.validate(inject, r)
            },
            SymbolValidationPass("BindsImplementation") { _, modules, r ->
                BindsImplementationValidator.validate(modules, r)
            },
            SymbolValidationPass("SingleScope") { inject, modules, r ->
                SingleScopeValidator.validate(inject, modules, r)
            },
        )

    private val modelPasses: List<ModelValidationPass> =
        listOf(
            ModelValidationPass("ModuleInstallIn") { ctx, r ->
                ModuleInstallInValidator.validate(ctx.moduleDescriptors, ctx.components, r)
            },
            ModelValidationPass("ModuleBinds") { ctx, r ->
                ModuleBindsValidator.validate(ctx.moduleDescriptors, r)
            },
            ModelValidationPass("AnchorViewModel") { ctx, r ->
                AnchorViewModelValidator.validate(ctx.injectClassDescriptors, r)
            },
            ModelValidationPass("DuplicateBinding") { ctx, r ->
                DuplicateBindingValidator.validate(ctx.bindings, r)
            },
            ModelValidationPass("UniqueScopeInHierarchy") { ctx, r ->
                UniqueScopeInHierarchyValidator.validate(ctx.components, r)
            },
            ModelValidationPass("ScopeComponentCompatibility") { ctx, r ->
                ScopeComponentCompatibilityValidator.validate(ctx.bindings, r)
            },
            ModelValidationPass("ScopeLifetimeViolation") { ctx, r ->
                ScopeLifetimeViolationValidator.validate(ctx.bindings, ctx.requirements, r)
            },
            ModelValidationPass("MissingBinding") { ctx, r ->
                MissingBindingValidator.validate(ctx.providedKeys, ctx.requirements, r)
            },
            ModelValidationPass("Cycle") { ctx, r ->
                CycleValidator.validate(ctx.dependencyGraph, r)
            },
            ModelValidationPass("ReachableBindings") { ctx, r ->
                ReachableBindingsValidator.validate(ctx.bindings, ctx.dependencyGraph, r)
            },
        )

    /** Runs all [ValidationPhase.SYMBOL] passes in order. */
    fun runSymbolPhase(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>,
        reporter: ValidationReporter,
    ) {
        symbolPasses.forEach { it.run(injectClasses, moduleClasses, reporter) }
    }

    /** Runs all [ValidationPhase.MODEL] passes in order. */
    fun runModelPhase(
        context: ValidationModelContext,
        reporter: ValidationReporter,
    ) {
        modelPasses.forEach { it.run(context, reporter) }
    }
}
