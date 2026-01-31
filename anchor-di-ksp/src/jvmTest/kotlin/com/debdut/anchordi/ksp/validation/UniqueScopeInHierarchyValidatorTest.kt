package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.ComponentDescriptor
import kotlin.test.Test
import kotlin.test.assertTrue

class UniqueScopeInHierarchyValidatorTest {

    @Test
    fun distinctBuiltInComponents_reportsNoErrors() {
        val components = mapOf(
            ValidationConstants.FQN_SINGLETON_COMPONENT to ComponentDescriptor(ValidationConstants.FQN_SINGLETON_COMPONENT),
            ValidationConstants.FQN_VIEW_MODEL_COMPONENT to ComponentDescriptor(ValidationConstants.FQN_VIEW_MODEL_COMPONENT),
            ValidationConstants.FQN_NAVIGATION_COMPONENT to ComponentDescriptor(ValidationConstants.FQN_NAVIGATION_COMPONENT)
        )
        val reporter = CollectingReporter()
        UniqueScopeInHierarchyValidator.validate(components, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun singleComponent_reportsNoErrors() {
        val components = mapOf(
            ValidationConstants.FQN_SINGLETON_COMPONENT to ComponentDescriptor(ValidationConstants.FQN_SINGLETON_COMPONENT)
        )
        val reporter = CollectingReporter()
        UniqueScopeInHierarchyValidator.validate(components, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun emptyComponents_reportsNoErrors() {
        val reporter = CollectingReporter()
        UniqueScopeInHierarchyValidator.validate(emptyMap(), reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun customComponentsWithDistinctFqns_reportsNoErrors() {
        val components = mapOf(
            "com.example.app.SingletonComponent" to ComponentDescriptor("com.example.app.SingletonComponent"),
            "com.example.app.ViewModelComponent" to ComponentDescriptor("com.example.app.ViewModelComponent")
        )
        val reporter = CollectingReporter()
        UniqueScopeInHierarchyValidator.validate(components, reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
