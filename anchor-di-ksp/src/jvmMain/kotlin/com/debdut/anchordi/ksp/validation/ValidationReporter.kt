package com.debdut.anchordi.ksp.validation

/**
 * Reports validation errors and warnings. Implementations can log (KSP) or collect (tests).
 */
interface ValidationReporter {
    fun error(
        message: String,
        element: Any? = null,
    )

    fun warn(
        message: String,
        element: Any? = null,
    )
}

/**
 * Collects all reported errors and warnings for testing.
 */
class CollectingReporter : ValidationReporter {
    val errors = mutableListOf<ReportedMessage>()
    val warnings = mutableListOf<ReportedMessage>()

    data class ReportedMessage(val message: String, val element: Any?)

    override fun error(
        message: String,
        element: Any?,
    ) {
        errors.add(ReportedMessage(message, element))
    }

    override fun warn(
        message: String,
        element: Any?,
    ) {
        warnings.add(ReportedMessage(message, element))
    }
}
