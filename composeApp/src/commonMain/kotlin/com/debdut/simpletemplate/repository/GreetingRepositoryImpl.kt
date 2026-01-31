package com.debdut.simpletemplate.repository

import com.debdut.anchordi.Inject
import com.debdut.simpletemplate.Platform
import com.debdut.simpletemplate.logger.Logger

/**
 * Implementation of [GreetingRepository] with injected [Platform] and [Logger].
 * ViewModel-scoped (one instance per ViewModel) via [RepositoryModule].
 */
class GreetingRepositoryImpl @Inject constructor(
    private val platform: Platform,
    private val logger: Logger
): GreetingRepository {
    override fun greet(): String {
        val message = "Hello from ${platform.name}!"
        logger.log("Greeting: $message")
        return message
    }
}