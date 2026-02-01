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

    @Test
    fun multipleIndependentCycles_reportsAll() {
        // Two independent cycles: A→B→A and C→D→C
        val graph = mapOf(
            "A" to setOf("B"),
            "B" to setOf("A"),
            "C" to setOf("D"),
            "D" to setOf("C")
        )
        val reporter = CollectingReporter()
        CycleValidator.validate(graph, reporter)
        
        // Should report both cycles
        assertEquals(2, reporter.errors.size)
        val messages = reporter.errors.map { it.message }
        assertTrue(messages.any { it.contains("A") && it.contains("B") })
        assertTrue(messages.any { it.contains("C") && it.contains("D") })
    }

    @Test
    fun multipleSelfCycles_reportsAll() {
        // Multiple self-cycles: A→A, B→B, C→C
        val graph = mapOf(
            "A" to setOf("A"),
            "B" to setOf("B"),
            "C" to setOf("C")
        )
        val reporter = CollectingReporter()
        CycleValidator.validate(graph, reporter)
        
        // Should report all three self-cycles
        assertEquals(3, reporter.errors.size)
    }

    @Test
    fun mixedCyclesAndAcyclic_reportsOnlyCycles() {
        // One cycle (A→B→A) and some acyclic nodes (C→D, E)
        val graph = mapOf(
            "A" to setOf("B"),
            "B" to setOf("A"),
            "C" to setOf("D"),
            "D" to emptySet(),
            "E" to emptySet()
        )
        val reporter = CollectingReporter()
        CycleValidator.validate(graph, reporter)
        
        // Should only report the one cycle
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("A") || reporter.errors[0].message.contains("B"))
    }

    @Test
    fun longerCycle_reportsFullPath() {
        // Longer cycle: A→B→C→D→E→A
        val graph = mapOf(
            "A" to setOf("B"),
            "B" to setOf("C"),
            "C" to setOf("D"),
            "D" to setOf("E"),
            "E" to setOf("A")
        )
        val reporter = CollectingReporter()
        CycleValidator.validate(graph, reporter)
        
        assertEquals(1, reporter.errors.size)
        val message = reporter.errors[0].message
        assertTrue(message.contains("Circular dependency"))
        // The cycle path should include all nodes
        assertTrue(message.contains("A"))
        assertTrue(message.contains("B"))
        assertTrue(message.contains("C"))
        assertTrue(message.contains("D"))
        assertTrue(message.contains("E"))
    }
}
