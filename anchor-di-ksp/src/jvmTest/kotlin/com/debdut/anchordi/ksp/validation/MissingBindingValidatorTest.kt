package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.DependencyRequirement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MissingBindingValidatorTest {

    @Test
    fun allRequirementsProvided_noErrors() {
        val providedKeys = setOf("pkg.Api", "pkg.Repo")
        val requirements = listOf(
            DependencyRequirement("pkg.Api", "pkg.Repo"),
            DependencyRequirement("pkg.Repo", "pkg.ViewModel")
        )
        val reporter = CollectingReporter()
        MissingBindingValidator.validate(providedKeys, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun missingBinding_reportsError() {
        val providedKeys = setOf("pkg.Api")
        val requirements = listOf(
            DependencyRequirement("pkg.Repo", "pkg.ViewModel")
        )
        val reporter = CollectingReporter()
        MissingBindingValidator.validate(providedKeys, requirements, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("has no binding"))
        assertTrue(reporter.errors[0].message.contains("pkg.Repo"))
    }

    @Test
    fun skippedTypes_notReportedAsMissing() {
        val providedKeys = emptySet<String>()
        val requirements = listOf(
            DependencyRequirement("kotlin.Lazy", "pkg.SomeClass"),
            DependencyRequirement("kotlin.String", "pkg.Other")
        )
        val reporter = CollectingReporter()
        MissingBindingValidator.validate(providedKeys, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
