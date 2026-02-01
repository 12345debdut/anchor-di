package com.debdut.anchordi.ksp.validation

/**
 * Explicit ordering of validation phases. Symbol-level validations run first;
 * model-level validations run after the full DI model is built.
 */
enum class ValidationPhase(
    val description: String
) {
    /** Validations that require only KSP symbols (@Inject classes, @Module classes). Run before model build. */
    SYMBOL("Symbol-level: inject/module structure and annotations"),

    /** Validations that require the built model (bindings, components, requirements, graph). Run after model build. */
    MODEL("Model-level: bindings, components, dependencies, and lifetime rules")
}
