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
 * Validates @AnchorViewModel rules: must be @ViewModelScoped, belong to ViewModelComponent, have @Inject constructor.
 */
object AnchorViewModelValidator {

    fun validate(
        injectClasses: List<InjectClassDescriptor>,
        reporter: ValidationReporter
    ) {
        injectClasses
            .filter { it.hasAnchorViewModel }
            .forEach { desc ->
                if (!desc.hasViewModelScoped) {
                    reporter.error(
                        "[Anchor DI] @AnchorViewModel ${desc.simpleName} must be @ViewModelScoped",
                        null
                    )
                }
                if (desc.component != ValidationConstants.FQN_VIEW_MODEL_COMPONENT) {
                    reporter.error(
                        "[Anchor DI] @AnchorViewModel ${desc.simpleName} must be installed in ViewModelComponent",
                        null
                    )
                }
                if (!desc.hasInjectConstructor) {
                    reporter.error(
                        "[Anchor DI] @AnchorViewModel ${desc.simpleName} must have an @Inject constructor",
                        null
                    )
                }
            }
    }
}
