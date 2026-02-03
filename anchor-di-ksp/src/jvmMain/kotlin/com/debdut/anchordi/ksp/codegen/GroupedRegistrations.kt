package com.debdut.anchordi.ksp.codegen

import com.debdut.anchordi.ksp.validation.ValidationConstants

/**
 * Result of grouping binding registrations by module or "Inject" for split-file code generation.
 * Keys are file suffixes: @Module simple name (e.g. "AppModule", "LoggerModule") or "Inject"
 * for @Inject class bindings not exclusively @Binds in one module. Values are the source
 * lines to emit inside contribute(registry) (each inner list is one registry.register(...) block).
 */
typealias GroupedRegistrations = Map<String, List<List<String>>>

/**
 * Converts a component FQN to a safe file/object suffix for generated names.
 * Built-in components use fixed names; custom scopes use the scope class simple name.
 */
fun componentIdToFileSuffix(componentId: String): String =
    when (componentId) {
        ValidationConstants.FQN_SINGLETON_COMPONENT -> "Singleton"
        ValidationConstants.FQN_VIEW_MODEL_COMPONENT -> "ViewModel"
        ValidationConstants.FQN_NAVIGATION_COMPONENT -> "Navigation"
        else -> {
            // Custom scope: use simple name (e.g. "com.example.SessionScope" -> "SessionScope")
            val simple = componentId.substringAfterLast('.')
            if (simple.isNotEmpty()) simple else componentId.replace(".", "_")
        }
    }
