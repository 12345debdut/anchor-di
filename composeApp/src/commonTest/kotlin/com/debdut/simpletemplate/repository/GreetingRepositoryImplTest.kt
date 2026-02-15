package com.debdut.simpletemplate.repository

import com.debdut.simpletemplate.Platform
import com.debdut.simpletemplate.logger.Logger
import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingRepositoryImplTest {
    @Test
    fun greetReturnsPlatformMessageAndLogs() {
        val logger = CapturingLogger()
        val repository = GreetingRepositoryImpl(lazyOf(FakePlatform("TestOS")), lazyOf(logger))

        val message = repository.greet()

        assertEquals("Hello from TestOS!", message)
        assertEquals(listOf("Greeting: Hello from TestOS!"), logger.messages)
    }

    private class FakePlatform(override val name: String) : Platform

    private class CapturingLogger : Logger {
        val messages = mutableListOf<String>()

        override fun log(message: String) {
            messages += message
        }
    }
}
