package com.debdut.simpletemplate

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun greetIncludesPlatformName() {
        val greeting = Greeting().greet()
        val platformName = getPlatform().name

        assertTrue(greeting.startsWith("Hello, "))
        assertTrue(greeting.contains(platformName))
    }
}
