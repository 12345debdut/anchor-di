package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScopeComponentCompatibilityValidatorTest {

    @Test
    fun singletonComponentWithSingletonScope_ok() {
        val bindings = listOf(
            BindingDescriptor("Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "Module.provideApi")
        )
        val reporter = CollectingReporter()
        ScopeComponentCompatibilityValidator.validate(bindings, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun singletonComponentWithViewModelScoped_reportsError() {
        val bindings = listOf(
            BindingDescriptor("Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_VIEW_MODEL_SCOPED, "Module.provideApi")
        )
        val reporter = CollectingReporter()
        ScopeComponentCompatibilityValidator.validate(bindings, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Scope") && reporter.errors[0].message.contains("not allowed"))
    }

    @Test
    fun viewModelComponentWithViewModelScoped_ok() {
        val bindings = listOf(
            BindingDescriptor("Repo", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_VIEW_MODEL_SCOPED, "Module.bindRepo")
        )
        val reporter = CollectingReporter()
        ScopeComponentCompatibilityValidator.validate(bindings, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun unscopedBinding_ok() {
        val bindings = listOf(
            BindingDescriptor("Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Api")
        )
        val reporter = CollectingReporter()
        ScopeComponentCompatibilityValidator.validate(bindings, reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
