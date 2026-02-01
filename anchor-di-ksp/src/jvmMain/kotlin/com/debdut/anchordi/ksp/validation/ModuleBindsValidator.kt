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
                    ValidationMessageFormat.formatWarn(
                        summary = "Module ${module.moduleName} has no @Provides or @Binds methods.",
                        fix = "Add at least one @Provides or @Binds method, or remove the module."
                    ),
                    null
                )
            }
            module.bindsMethods.forEach { binds ->
                if (binds.parameterCount != 1) {
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "@Binds method ${binds.methodName} in ${binds.moduleName} must have exactly one parameter (the implementation type to bind).",
                            detail = "Example: @Binds fun bindRepo(impl: RepoImpl): Repo.",
                            fix = "Add one parameter of the concrete implementation type."
                        ),
                        null
                    )
                }
            }
        }
    }
}
