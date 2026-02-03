package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * A single validation pass with a stable name for ordering and debugging.
 * Symbol passes run in [ValidationPhase.SYMBOL]; model passes run in [ValidationPhase.MODEL].
 */
data class SymbolValidationPass(
    val name: String,
    val run: (List<KSClassDeclaration>, List<KSClassDeclaration>, ValidationReporter) -> Unit,
)

/**
 * Model-level pass: receives [ValidationModelContext] and [ValidationReporter].
 */
data class ModelValidationPass(
    val name: String,
    val run: (ValidationModelContext, ValidationReporter) -> Unit,
)
