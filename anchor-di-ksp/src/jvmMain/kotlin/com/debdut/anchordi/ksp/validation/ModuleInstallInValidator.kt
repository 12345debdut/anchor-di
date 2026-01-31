package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.debdut.anchordi.ksp.model.ModuleDescriptor

/**
 * Validates that every module has @InstallIn and that the target component is known.
 */
object ModuleInstallInValidator {

    fun validate(
        modules: List<ModuleDescriptor>,
        knownComponents: Map<String, ComponentDescriptor>,
        reporter: ValidationReporter
    ) {
        modules.forEach { module ->
            val installInFqn = module.installInComponentFqn
            if (installInFqn == null) {
                reporter.error(
                    "[Anchor DI] @Module ${module.moduleName} must declare @InstallIn.",
                    null
                )
            } else if (installInFqn !in knownComponents) {
                reporter.error(
                    "[Anchor DI] ${module.moduleName} installs into unknown component $installInFqn",
                    null
                )
            }
        }
    }
}
