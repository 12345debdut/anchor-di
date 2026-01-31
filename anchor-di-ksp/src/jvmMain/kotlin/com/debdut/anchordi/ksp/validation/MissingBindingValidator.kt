package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.DependencyRequirement

/**
 * Validates that every required dependency has a binding (provided key).
 */
object MissingBindingValidator {

    fun validate(
        providedKeys: Set<String>,
        requirements: List<DependencyRequirement>,
        reporter: ValidationReporter
    ) {
        requirements.forEach { (required, requester) ->
            if (!providedKeys.contains(required) && !ValidationConstants.SKIPPED_TYPES.contains(required)) {
                reporter.error(
                    "[Anchor DI] Missing binding for $required (required by $requester). " +
                        "Add @Inject constructor, @Provides in a module, or @Binds.",
                    null
                )
            }
        }
    }
}
