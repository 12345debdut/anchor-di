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
        reporter: ValidationReporter,
    ) {
        modules.forEach { module ->
            val installInFqn = module.installInComponentFqn
            if (installInFqn == null) {
                reporter.error(
                    ValidationMessageFormat.formatError(
                        summary = "@Module ${module.moduleName} must have @InstallIn(SomeComponent::class).",
                        detail = "Without @InstallIn, the container does not know which scope this module's bindings belong to.",
                        fix =
                            "Add @InstallIn(SingletonComponent::class), @InstallIn(ViewModelComponent::class), " +
                                "or @InstallIn(NavigationComponent::class) (or your custom component).",
                    ),
                    null,
                )
            } else if (installInFqn !in knownComponents) {
                val componentName = installInFqn.substringAfterLast('.')
                reporter.error(
                    ValidationMessageFormat.formatError(
                        summary =
                            "${module.moduleName} installs into '$componentName' ($installInFqn), " +
                                "which is not a known component.",
                        detail =
                            "Known built-in components are SingletonComponent, ViewModelComponent, " +
                                "and NavigationComponent.",
                        fix =
                            "Use one of the built-in components or ensure your custom @Component class " +
                                "is on the processor classpath.",
                    ),
                    null,
                )
            }
        }
    }
}
