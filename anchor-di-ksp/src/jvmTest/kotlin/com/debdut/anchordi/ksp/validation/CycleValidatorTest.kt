package com.debdut.anchordi.ksp.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CycleValidatorTest {

    @Test
    fun acyclicGraph_noErrors() {
        val graph = mapOf(
            "A" to setOf("B", "C"),
            "B" to setOf("C"),
            "C" to emptySet()
        )
        val reporter = CollectingReporter()
        CycleValidator.validate(graph, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun cycle_reportsError() {
        val graph = mapOf(
            "A" to setOf("B"),
            "B" to setOf("C"),
            "C" to setOf("A")
        )
        val reporter = CollectingReporter()
        CycleValidator.validate(graph, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Circular dependency"))
        assertTrue(reporter.errors[0].message.contains("A") || reporter.errors[0].message.contains("B") || reporter.errors[0].message.contains("C"))
    }

    @Test
    fun selfCycle_reportsError() {
        val graph = mapOf("A" to setOf("A"))
        val reporter = CollectingReporter()
        CycleValidator.validate(graph, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Circular dependency"))
    }

    @Test
    fun emptyGraph_noErrors() {
        val reporter = CollectingReporter()
        CycleValidator.validate(emptyMap(), reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
