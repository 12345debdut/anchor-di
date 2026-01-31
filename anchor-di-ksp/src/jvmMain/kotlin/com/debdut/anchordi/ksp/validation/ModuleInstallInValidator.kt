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
                    "[Anchor DI] Module must declare a target component: @Module ${module.moduleName} must have @InstallIn(SomeComponent::class). " +
                        "Without @InstallIn, the container does not know which scope this module's bindings belong to. " +
                        "Fix: add @InstallIn(SingletonComponent::class), @InstallIn(ViewModelComponent::class), or @InstallIn(NavigationComponent::class) (or your custom component).",
                    null
                )
            } else if (installInFqn !in knownComponents) {
                val componentName = installInFqn.substringAfterLast('.')
                reporter.error(
                    "[Anchor DI] Unknown component: ${module.moduleName} installs into '$componentName' ($installInFqn), which is not a known component. " +
                        "Known built-in components are SingletonComponent, ViewModelComponent, and NavigationComponent. " +
                        "Fix: use one of the built-in components or ensure your custom @Component class is on the processor classpath.",
                    null
                )
            }
        }
    }
}
