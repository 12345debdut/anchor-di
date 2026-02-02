package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorAccessibilityValidatorTest {
    @Test
    fun publicConstructor_reportsNoErrors() {
        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService")
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass.primaryConstructorBacking = constructor

        val reporter = CollectingReporter()
        ConstructorAccessibilityValidator.validate(listOf(injectClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun privateConstructor_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.PrivateService", "PrivateService")
        val constructor = FakeKSFunctionDeclaration("com.example.PrivateService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        constructor.modifiersSet.add(Modifier.PRIVATE)
        injectClass.primaryConstructorBacking = constructor

        val reporter = CollectingReporter()
        ConstructorAccessibilityValidator.validate(listOf(injectClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("@Inject constructor"))
        assertTrue(reporter.errors[0].message.contains("private"))
        assertTrue(reporter.errors[0].message.contains("com.example.PrivateService"))
    }

    @Test
    fun protectedConstructor_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.ProtectedService", "ProtectedService")
        val constructor = FakeKSFunctionDeclaration("com.example.ProtectedService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        constructor.modifiersSet.add(Modifier.PROTECTED)
        injectClass.primaryConstructorBacking = constructor

        val reporter = CollectingReporter()
        ConstructorAccessibilityValidator.validate(listOf(injectClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("protected"))
        assertTrue(reporter.errors[0].message.contains("com.example.ProtectedService"))
    }

    @Test
    fun noInjectConstructor_ignored() {
        val injectClass = FakeKSClassDeclaration("com.example.Bar", "Bar")
        injectClass.primaryConstructorBacking = null

        val reporter = CollectingReporter()
        ConstructorAccessibilityValidator.validate(listOf(injectClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
