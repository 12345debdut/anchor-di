package com.debdut.simpletemplate.repository

import com.debdut.anchordi.Inject
import com.debdut.simpletemplate.Platform
import com.debdut.simpletemplate.logger.Logger
import kotlin.Lazy

/**
 * Implementation of [GreetingRepository] with injected [Platform] and [Logger].
 * ViewModel-scoped (one instance per ViewModel) via [RepositoryModule].
 */
class GreetingRepositoryImpl
    @Inject
    constructor(
        private val platform: Lazy<Platform>,
        private val logger: Lazy<Logger>,
    ) : GreetingRepository {
        override fun greet(): String {
            val message = "Hello from ${platform.value.name}!"
            logger.value.log("Greeting: $message")
            return message
        }
    }
