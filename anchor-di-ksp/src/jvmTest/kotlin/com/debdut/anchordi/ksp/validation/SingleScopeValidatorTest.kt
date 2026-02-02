package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SingleScopeValidatorTest {
    @Test
    fun singleScopeOnInjectClass_reportsNoErrors() {
        val injectClass = FakeKSClassDeclaration("com.example.Api", "Api")
        injectClass.addAnnotation("com.debdut.anchordi.Singleton")
        val reporter = CollectingReporter()
        SingleScopeValidator.validate(listOf(injectClass), emptyList(), reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun multipleScopesOnInjectClass_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.Foo", "Foo")
        injectClass.addAnnotation("com.debdut.anchordi.Singleton")
        injectClass.addAnnotation("com.debdut.anchordi.ViewModelScoped")
        val reporter = CollectingReporter()
        SingleScopeValidator.validate(listOf(injectClass), emptyList(), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Multiple scope annotations"))
        assertTrue(reporter.errors[0].message.contains("com.example.Foo"))
        assertTrue(reporter.errors[0].message.contains("only one scope"))
    }

    @Test
    fun noScopeOnInjectClass_reportsNoErrors() {
        val injectClass = FakeKSClassDeclaration("com.example.Bar", "Bar")
        val reporter = CollectingReporter()
        SingleScopeValidator.validate(listOf(injectClass), emptyList(), reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun multipleScopesOnProvides_reportsError() {
        val moduleClass = FakeKSClassDeclaration("com.example.AppModule", "AppModule")
        val providesFunc = FakeKSFunctionDeclaration("com.example.AppModule.provideApi", "provideApi")
        providesFunc.addAnnotation("com.debdut.anchordi.Provides")
        providesFunc.addAnnotation("com.debdut.anchordi.Singleton")
        providesFunc.addAnnotation("com.debdut.anchordi.ViewModelScoped")
        moduleClass.declarationsList.add(providesFunc)
        val reporter = CollectingReporter()
        SingleScopeValidator.validate(emptyList(), listOf(moduleClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Multiple scope annotations"))
        assertTrue(reporter.errors[0].message.contains("AppModule.provideApi"))
    }

    @Test
    fun singleScopeOnProvides_reportsNoErrors() {
        val moduleClass = FakeKSClassDeclaration("com.example.AppModule", "AppModule")
        val providesFunc = FakeKSFunctionDeclaration("com.example.AppModule.provideApi", "provideApi")
        providesFunc.addAnnotation("com.debdut.anchordi.Provides")
        providesFunc.addAnnotation("com.debdut.anchordi.Singleton")
        moduleClass.declarationsList.add(providesFunc)
        val reporter = CollectingReporter()
        SingleScopeValidator.validate(emptyList(), listOf(moduleClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
