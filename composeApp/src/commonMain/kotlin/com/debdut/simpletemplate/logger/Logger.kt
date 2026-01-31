package com.debdut.simpletemplate.logger

/**
 * Logging abstraction for the sample app.
 * Bound to [LoggerImpl] in [LoggerModule] (singleton).
 */
interface Logger {
    /** Logs [message] (e.g. to console or a logging backend). */
    fun log(message: String)
}
