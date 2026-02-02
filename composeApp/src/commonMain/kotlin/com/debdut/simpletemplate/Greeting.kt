package com.debdut.simpletemplate

/**
 * Simple greeting helper that uses [getPlatform] to build a platform-specific message.
 * Used for demo; the main screen uses [GreetingRepository] instead for DI.
 */
class Greeting {
    private val platform = getPlatform()

    /** Returns a greeting string including the current platform name. */
    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
