package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultipleInjectConstructorValidatorTest {
    @Test
    fun singleInjectConstructor_reportsNoErrors() {
        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService")
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        constructor.addParameter("repo", "com.example.Repo")
        injectClass.primaryConstructorBacking = constructor

        val reporter = CollectingReporter()
        MultipleInjectConstructorValidator.validate(listOf(injectClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun multipleInjectConstructors_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.Foo", "Foo")
        val primary = FakeKSFunctionDeclaration("com.example.Foo.<init>", "<init>")
        primary.addAnnotation("com.debdut.anchordi.Inject")
        primary.addParameter("a", "com.example.A")
        injectClass.primaryConstructorBacking = primary

        val secondary = FakeKSFunctionDeclaration("com.example.Foo.<init>#1", "<init>")
        secondary.addAnnotation("com.debdut.anchordi.Inject")
        secondary.addParameter("b", "com.example.B")
        injectClass.declarationsList.add(secondary)

        val reporter = CollectingReporter()
        MultipleInjectConstructorValidator.validate(listOf(injectClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Multiple @Inject constructors"))
        assertTrue(reporter.errors[0].message.contains("com.example.Foo"))
    }

    @Test
    fun noInjectConstructor_ignored() {
        val injectClass = FakeKSClassDeclaration("com.example.Bar", "Bar")
        injectClass.primaryConstructorBacking = null

        val reporter = CollectingReporter()
        MultipleInjectConstructorValidator.validate(listOf(injectClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
