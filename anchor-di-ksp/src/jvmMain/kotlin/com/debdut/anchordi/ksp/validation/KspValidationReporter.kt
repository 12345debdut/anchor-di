package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Bridges [ValidationReporter] to [KSPLogger]. Used by the symbol processor.
 */
class KspValidationReporter(private val logger: KSPLogger) : ValidationReporter {

    override fun error(message: String, element: Any?) {
        logger.error(message, element as? KSAnnotated)
    }

    override fun warn(message: String, element: Any?) {
        logger.warn(message, element as? KSAnnotated)
    }
}
