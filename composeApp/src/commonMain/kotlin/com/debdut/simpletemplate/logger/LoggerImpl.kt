package com.debdut.simpletemplate.logger

import com.debdut.anchordi.Inject
import com.debdut.anchordi.Singleton

@Singleton
class LoggerImpl @Inject constructor() : Logger {
    override fun log(message: String) {
        println("[AnchorDI] $message")
    }
}
