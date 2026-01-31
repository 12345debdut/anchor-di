package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor

/**
 * Validates that there are no duplicate bindings (same key + qualifier + component).
 */
object DuplicateBindingValidator {

    fun validate(bindings: List<BindingDescriptor>, reporter: ValidationReporter) {
        bindings
            .groupBy { Triple(it.key, it.qualifier, it.component) }
            .filter { it.value.size > 1 }
            .forEach { (_, dups) ->
                val sources = dups.joinToString { it.source }
                reporter.error(
                    "[Anchor DI] Duplicate binding for ${dups.first().key} in ${dups.first().component}. " +
                        "Defined in: $sources",
                    null
                )
            }
    }
}
