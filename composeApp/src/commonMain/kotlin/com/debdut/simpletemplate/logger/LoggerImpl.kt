package com.debdut.simpletemplate.logger

import com.debdut.anchordi.Inject
import com.debdut.anchordi.Singleton

/**
 * Default [Logger] implementation: prints to stdout with an [AnchorDI] prefix.
 * Singleton so one instance is shared app-wide.
 */
@Singleton
class LoggerImpl @Inject constructor() : Logger {
    override fun log(message: String) {
        println("[AnchorDI] $message")
    }
}
