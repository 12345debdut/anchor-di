package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor

/**
 * Descriptor for an @Inject class that may be @AnchorViewModel.
 */
data class InjectClassDescriptor(
    val simpleName: String,
    val hasAnchorViewModel: Boolean,
    val hasViewModelScoped: Boolean,
    val component: String,
    val hasInjectConstructor: Boolean
)

/**
 * Validates @AnchorViewModel rules: must be bound in ViewModelComponent and have an @Inject constructor.
 */
object AnchorViewModelValidator {

    fun validate(
        injectClasses: List<InjectClassDescriptor>,
        reporter: ValidationReporter
    ) {
        injectClasses
            .filter { it.hasAnchorViewModel }
            .forEach { desc ->
                if (desc.component != ValidationConstants.FQN_VIEW_MODEL_COMPONENT) {
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "@AnchorViewModel ${desc.simpleName} is bound in a component other than ViewModelComponent.",
                            detail = "ViewModels created via viewModelAnchor() run inside ViewModelComponent.",
                            fix = "Ensure the binding is installed in ViewModelComponent (e.g. via @InstallIn(ViewModelComponent::class) on the module that provides it)."
                        ),
                        null
                    )
                }
                if (!desc.hasInjectConstructor) {
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "${desc.simpleName} is annotated with @AnchorViewModel but has no @Inject constructor.",
                            detail = "The DI container needs an @Inject constructor to create the ViewModel.",
                            fix = "Add @Inject to the primary (or secondary) constructor."
                        ),
                        null
                    )
                }
            }
    }
}
