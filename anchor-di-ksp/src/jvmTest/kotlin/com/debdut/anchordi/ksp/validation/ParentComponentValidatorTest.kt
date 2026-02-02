package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParentComponentValidatorTest {
    @Test
    fun dependencyInSameComponent_reportsNoErrors() {
        val bindings =
            listOf(
                BindingDescriptor("com.example.Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Api"),
                BindingDescriptor("com.example.Repo", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Repo"),
            )
        val requirements =
            listOf(
                DependencyRequirement("com.example.Api", "com.example.Repo"),
            )
        val reporter = CollectingReporter()
        ParentComponentValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun dependencyInAncestorComponent_reportsNoErrors() {
        val bindings =
            listOf(
                BindingDescriptor("com.example.Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Api"),
                BindingDescriptor("com.example.Repo", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, null, "Repo"),
            )
        val requirements =
            listOf(
                DependencyRequirement("com.example.Api", "com.example.Repo"),
            )
        val reporter = CollectingReporter()
        ParentComponentValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun parentDependsOnChildOnly_reportsError() {
        val bindings =
            listOf(
                BindingDescriptor(
                    "com.example.SingletonService",
                    null,
                    ValidationConstants.FQN_SINGLETON_COMPONENT,
                    null,
                    "SingletonService",
                ),
                BindingDescriptor("com.example.ViewModelOnly", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, null, "ViewModelOnly"),
            )
        val requirements =
            listOf(
                DependencyRequirement("com.example.ViewModelOnly", "com.example.SingletonService"),
            )
        val reporter = CollectingReporter()
        ParentComponentValidator.validate(bindings, requirements, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("parent component cannot depend"))
        assertTrue(reporter.errors[0].message.contains("SingletonService"))
        assertTrue(reporter.errors[0].message.contains("ViewModelOnly"))
        assertTrue(reporter.errors[0].message.contains("ViewModelComponent"))
    }

    @Test
    fun missingRequiredBinding_skipped() {
        val bindings =
            listOf(
                BindingDescriptor("com.example.Requester", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Requester"),
            )
        val requirements =
            listOf(
                DependencyRequirement("com.example.Missing", "com.example.Requester"),
            )
        val reporter = CollectingReporter()
        ParentComponentValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
