package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor

/**
 * Validates that each key has exactly one binding per component (no duplicate key + qualifier in the same component).
 * Multibinding contributions (Set/Map) are allowed to have multiple bindings for the same key;
 * for Map, map keys must be unique within the same multibound map.
 */
object DuplicateBindingValidator {

    fun validate(bindings: List<BindingDescriptor>, reporter: ValidationReporter) {
        bindings
            .groupBy { Triple(it.key, it.qualifier, it.component) }
            .forEach { (_, group) ->
                val first = group.first()
                val isMultibindingSet = first.multibindingKind == "set"
                val isMultibindingMap = first.multibindingKind == "map"
                when {
                    isMultibindingSet || isMultibindingMap -> {
                        // Multiple contributions allowed; for map check unique mapKey
                        if (isMultibindingMap) {
                            val mapKeys = group.mapNotNull { it.mapKey }
                            val duplicateKeys = mapKeys.groupingBy { it }.eachCount().filter { it.value > 1 }
                            duplicateKeys.keys.forEach { dupKey ->
                                val sources = group.filter { it.mapKey == dupKey }.joinToString { it.source }
                                reporter.error(
                                    ValidationMessageFormat.formatError(
                                        summary = "Duplicate map key '$dupKey' in multibinding for ${first.key}.",
                                        detail = "Defined in: $sources. Each @IntoMap contribution must have a unique @StringKey.",
                                        fix = "Use a unique @StringKey for each contribution."
                                    ),
                                    null
                                )
                            }
                        }
                    }
                    group.size > 1 -> {
                        val componentName = first.component.substringAfterLast('.')
                        val sources = group.joinToString { it.source }
                        reporter.error(
                            ValidationMessageFormat.formatError(
                                summary = "Duplicate binding: '${first.key}' (qualifier: ${first.qualifier ?: "none"}) is bound more than once in component '$componentName'.",
                                detail = "Defined in: $sources.",
                                fix = "Remove or consolidate the duplicate; keep only one @Inject constructor, @Provides method, or @Binds for this type in this component."
                            ),
                            null
                        )
                    }
                }
            }
    }
}
