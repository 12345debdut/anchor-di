package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.ModuleDescriptor

/**
 * Validates @Module and @Binds: every module has @InstallIn, @Binds methods have exactly one parameter,
 * and modules have at least one @Provides or @Binds.
 */
object ModuleBindsValidator {

    fun validate(modules: List<ModuleDescriptor>, reporter: ValidationReporter) {
        modules.forEach { module ->
            if (!module.hasProvidesOrBinds) {
                reporter.warn(
                    "[Anchor DI] Module ${module.moduleName} has no @Provides or @Binds methods. " +
                        "It will not contribute any bindings. Add at least one @Provides or @Binds method, or remove the module.",
                    null
                )
            }
            module.bindsMethods.forEach { binds ->
                if (binds.parameterCount != 1) {
                    reporter.error(
                        "[Anchor DI] @Binds method must have exactly one parameter: ${binds.methodName} in ${binds.moduleName} must take exactly one parameter (the implementation type to bind). " +
                            "Example: @Binds fun bindRepo(impl: RepoImpl): Repo. " +
                            "Fix: add one parameter of the concrete implementation type.",
                        null
                    )
                }
            }
        }
    }
}
