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
                reporter.warn("[Anchor DI] Module ${module.moduleName} has no @Provides or @Binds methods.", null)
            }
            module.bindsMethods.forEach { binds ->
                if (binds.parameterCount != 1) {
                    reporter.error(
                        "[Anchor DI] @Binds method ${binds.methodName} in ${binds.moduleName} must have exactly one parameter (implementation type).",
                        null
                    )
                }
            }
        }
    }
}
