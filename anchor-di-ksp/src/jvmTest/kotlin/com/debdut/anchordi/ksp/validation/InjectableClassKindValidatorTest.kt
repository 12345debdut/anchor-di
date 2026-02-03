package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InjectableClassKindValidatorTest {
    @Test
    fun concreteClass_reportsNoErrors() {
        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService", ClassKind.CLASS)
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass.primaryConstructorBacking = constructor

        val reporter = CollectingReporter()
        InjectableClassKindValidator.validate(listOf(injectClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun interface_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.IApi", "IApi", ClassKind.INTERFACE)
        val reporter = CollectingReporter()
        InjectableClassKindValidator.validate(listOf(injectClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("interface"))
        assertTrue(reporter.errors[0].message.contains("com.example.IApi"))
    }

    @Test
    fun object_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.Singleton", "Singleton", ClassKind.OBJECT)
        val reporter = CollectingReporter()
        InjectableClassKindValidator.validate(listOf(injectClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("object"))
        assertTrue(reporter.errors[0].message.contains("com.example.Singleton"))
    }

    @Test
    fun enumClass_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.MyEnum", "MyEnum", ClassKind.ENUM_CLASS)
        val reporter = CollectingReporter()
        InjectableClassKindValidator.validate(listOf(injectClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("enum"))
        assertTrue(reporter.errors[0].message.contains("com.example.MyEnum"))
    }

    @Test
    fun abstractClass_reportsError() {
        val injectClass = FakeKSClassDeclaration("com.example.BaseService", "BaseService", ClassKind.CLASS)
        injectClass.modifiersSet.add(Modifier.ABSTRACT)
        val reporter = CollectingReporter()
        InjectableClassKindValidator.validate(listOf(injectClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("abstract"))
        assertTrue(reporter.errors[0].message.contains("com.example.BaseService"))
    }
}
