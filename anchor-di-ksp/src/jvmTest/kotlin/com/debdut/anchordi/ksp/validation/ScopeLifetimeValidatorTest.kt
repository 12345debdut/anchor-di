package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScopeLifetimeValidatorTest {

    @Test
    fun shorterLivedDependsOnLongerLived_reportsNoErrors() {
        val bindings = listOf(
            BindingDescriptor("com.example.Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "Api"),
            BindingDescriptor("com.example.Repo", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_VIEW_MODEL_SCOPED, "Repo")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.Api", "com.example.Repo")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun longerLivedDependsOnShorterLived_reportsError() {
        // Both in same component so the dependency is visible; longer-lived (Singleton) depends on shorter-lived (ViewModelScoped).
        val bindings = listOf(
            BindingDescriptor("com.example.SingletonService", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_SINGLETON, "SingletonService"),
            BindingDescriptor("com.example.ViewModelHelper", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_VIEW_MODEL_SCOPED, "ViewModelHelper")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.ViewModelHelper", "com.example.SingletonService")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeValidator.validate(bindings, requirements, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("longer-lived scope cannot depend on a shorter-lived"))
        assertTrue(reporter.errors[0].message.contains("SingletonService"))
        assertTrue(reporter.errors[0].message.contains("ViewModelHelper"))
    }

    @Test
    fun sameScopeDependsOnSame_reportsNoErrors() {
        val bindings = listOf(
            BindingDescriptor("com.example.A", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "A"),
            BindingDescriptor("com.example.B", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "B")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.B", "com.example.A")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun missingRequiredBinding_skipped() {
        val bindings = listOf(
            BindingDescriptor("com.example.Requester", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Requester")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.Missing", "com.example.Requester")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
