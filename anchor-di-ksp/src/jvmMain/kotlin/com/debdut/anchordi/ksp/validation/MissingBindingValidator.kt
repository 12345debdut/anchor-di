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
                val typeName = required.substringAfterLast('.')
                reporter.error(
                    ValidationMessageFormat.formatError(
                        summary = "'$required' is required by '$requester' but has no binding.",
                        detail = "The type '$typeName' must be provided so the DI container can create '$requester'.",
                        fix = "Add a binding for '$typeName': (1) @Inject constructor on a concrete class, (2) @Provides in a module installed in the same or parent component, or (3) @Binds in a module to map an interface to an implementation."
                    ),
                    null
                )
            }
        }
    }
}
