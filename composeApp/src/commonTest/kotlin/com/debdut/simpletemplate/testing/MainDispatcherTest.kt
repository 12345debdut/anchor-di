package com.debdut.simpletemplate.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Runs a test with Dispatchers.Main set to a [StandardTestDispatcher] tied to this test's scheduler.
 * Use [TestScope.testScheduler] inside [block] to advance time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
inline fun runMainTest(crossinline block: suspend TestScope.() -> Unit) =
    runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
