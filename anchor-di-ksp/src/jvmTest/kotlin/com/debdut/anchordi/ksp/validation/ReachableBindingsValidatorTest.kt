package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReachableBindingsValidatorTest {

    @Test
    fun allReachable_reportsNoWarnings() {
        val bindings = listOf(
            BindingDescriptor("com.example.Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Api"),
            BindingDescriptor("com.example.Repo", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Repo")
        )
        val dependencyGraph = mapOf(
            "com.example.Repo" to setOf("com.example.Api")
        )
        val reporter = CollectingReporter()
        ReachableBindingsValidator.validate(bindings, dependencyGraph, reporter)
        assertTrue(reporter.warnings.isEmpty())
    }

    @Test
    fun unreachableBinding_reportsWarning() {
        val bindings = listOf(
            BindingDescriptor("com.example.Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Api"),
            BindingDescriptor("com.example.Repo", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Repo"),
            BindingDescriptor("com.example.Orphan", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Orphan")
        )
        val dependencyGraph = mapOf(
            "com.example.Repo" to setOf("com.example.Api"),
            "com.example.Orphan" to setOf("com.example.Orphan")
        )
        val reporter = CollectingReporter()
        ReachableBindingsValidator.validate(bindings, dependencyGraph, reporter)
        assertEquals(1, reporter.warnings.size)
        assertTrue(reporter.warnings[0].message.contains("not reachable from any entry point"))
        assertTrue(reporter.warnings[0].message.contains("Orphan"))
    }

    @Test
    fun multipleEntryPointsBothReachable_reportsNoWarnings() {
        val bindings = listOf(
            BindingDescriptor("com.example.A", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "A"),
            BindingDescriptor("com.example.B", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "B"),
            BindingDescriptor("com.example.C", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "C")
        )
        val dependencyGraph = mapOf(
            "com.example.A" to setOf("com.example.C"),
            "com.example.B" to setOf("com.example.C")
        )
        val reporter = CollectingReporter()
        ReachableBindingsValidator.validate(bindings, dependencyGraph, reporter)
        assertTrue(reporter.warnings.isEmpty())
    }

    @Test
    fun emptyBindings_reportsNoWarnings() {
        val reporter = CollectingReporter()
        ReachableBindingsValidator.validate(emptyList(), emptyMap(), reporter)
        assertTrue(reporter.warnings.isEmpty())
    }

    @Test
    fun singleBindingIsEntryPoint_reportsNoWarnings() {
        val bindings = listOf(
            BindingDescriptor("com.example.Only", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Only")
        )
        val reporter = CollectingReporter()
        ReachableBindingsValidator.validate(bindings, emptyMap(), reporter)
        assertTrue(reporter.warnings.isEmpty())
    }
}
