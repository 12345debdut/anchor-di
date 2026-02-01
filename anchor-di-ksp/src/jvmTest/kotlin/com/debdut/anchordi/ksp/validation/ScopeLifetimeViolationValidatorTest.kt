package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the generic rule: a binding must not depend on another with strictly shorter effective lifetime.
 * Covers: parent/child (e.g. @Singleton Repo -> ViewModelScoped Presenter) and same-component longevity.
 */
class ScopeLifetimeViolationValidatorTest {

    @Test
    fun singletonDependsOnViewModelScopedInChild_reportsError() {
        // User example: @Singleton class Repo @Inject constructor(val presenter: Presenter)
        // Repo in SingletonComponent, Presenter only in ViewModelComponent -> violation
        val bindings = listOf(
            BindingDescriptor("com.example.Repo", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "Repo"),
            BindingDescriptor("com.example.Presenter", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_VIEW_MODEL_SCOPED, "Presenter")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.Presenter", "com.example.Repo")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("depends on"))
        assertTrue(reporter.errors[0].message.contains("Repo"))
        assertTrue(reporter.errors[0].message.contains("Presenter"))
    }

    @Test
    fun parentDependsOnChildOnly_reportsError() {
        val bindings = listOf(
            BindingDescriptor("com.example.SingletonService", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "SingletonService"),
            BindingDescriptor("com.example.ViewModelOnly", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, null, "ViewModelOnly")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.ViewModelOnly", "com.example.SingletonService")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("depends on"))
        assertTrue(reporter.errors[0].message.contains("SingletonService"))
        assertTrue(reporter.errors[0].message.contains("ViewModelOnly"))
    }

    @Test
    fun longerLivedDependsOnShorterLivedInSameComponent_reportsError() {
        val bindings = listOf(
            BindingDescriptor("com.example.SingletonService", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_SINGLETON, "SingletonService"),
            BindingDescriptor("com.example.ViewModelHelper", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_VIEW_MODEL_SCOPED, "ViewModelHelper")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.ViewModelHelper", "com.example.SingletonService")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("longer-lived scope cannot depend on shorter-lived"))
        assertTrue(reporter.errors[0].message.contains("SingletonService"))
        assertTrue(reporter.errors[0].message.contains("ViewModelHelper"))
    }

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
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun dependencyInSameComponentSameScope_reportsNoErrors() {
        val bindings = listOf(
            BindingDescriptor("com.example.A", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "A"),
            BindingDescriptor("com.example.B", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "B")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.B", "com.example.A")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun dependencyInAncestorComponent_reportsNoErrors() {
        val bindings = listOf(
            BindingDescriptor("com.example.Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Api"),
            BindingDescriptor("com.example.Repo", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, null, "Repo")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.Api", "com.example.Repo")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
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
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun bindsSingletonDependsOnViewModelScopedImpl_reportsError() {
        // @Binds @Singleton fun bindProductApi(impl: ProductApiImpl): ProductApi in SingletonComponent
        // ProductApiImpl is @ViewModelScoped (@Inject class). Requirement (impl, interface) is now recorded.
        val bindings = listOf(
            BindingDescriptor("com.example.ProductApi", null, ValidationConstants.FQN_SINGLETON_COMPONENT, ValidationConstants.FQN_SINGLETON, "ProductApiBindsModule.bindProductApi"),
            BindingDescriptor("com.example.ProductApiImpl", null, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, ValidationConstants.FQN_VIEW_MODEL_SCOPED, "ProductApiImpl")
        )
        val requirements = listOf(
            DependencyRequirement("com.example.ProductApiImpl", "com.example.ProductApi")
        )
        val reporter = CollectingReporter()
        ScopeLifetimeViolationValidator.validate(bindings, requirements, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("longer-lived") && reporter.errors[0].message.contains("ProductApi"))
        assertTrue(reporter.errors[0].message.contains("ProductApiImpl"))
    }
}
