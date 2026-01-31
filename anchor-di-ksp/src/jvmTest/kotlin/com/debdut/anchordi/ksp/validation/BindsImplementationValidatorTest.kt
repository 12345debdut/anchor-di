package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BindsImplementationValidatorTest {

    @Test
    fun bindsToConcreteClass_reportsNoErrors() {
        val moduleClass = FakeKSClassDeclaration("com.example.RepoModule", "RepoModule")
        val bindsFunc = FakeKSFunctionDeclaration("com.example.RepoModule.bindRepo", "bindRepo")
        bindsFunc.addAnnotation("com.debdut.anchordi.Binds")
        bindsFunc.addParameter("impl", "com.example.RepoImpl") // concrete class by default
        moduleClass._declarations.add(bindsFunc)

        val reporter = CollectingReporter()
        BindsImplementationValidator.validate(listOf(moduleClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun bindsToInterface_reportsError() {
        val interfaceDecl = FakeKSClassDeclaration("com.example.IRepo", "IRepo", ClassKind.INTERFACE)
        val moduleClass = FakeKSClassDeclaration("com.example.RepoModule", "RepoModule")
        val bindsFunc = FakeKSFunctionDeclaration("com.example.RepoModule.bindRepo", "bindRepo")
        bindsFunc.addAnnotation("com.debdut.anchordi.Binds")
        bindsFunc.addParameter("impl", interfaceDecl)
        moduleClass._declarations.add(bindsFunc)

        val reporter = CollectingReporter()
        BindsImplementationValidator.validate(listOf(moduleClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("@Binds"))
        assertTrue(reporter.errors[0].message.contains("interface"))
        assertTrue(reporter.errors[0].message.contains("com.example.IRepo"))
    }

    @Test
    fun bindsToAbstractClass_reportsError() {
        val abstractDecl = FakeKSClassDeclaration("com.example.BaseRepo", "BaseRepo", ClassKind.CLASS)
        abstractDecl._modifiers.add(Modifier.ABSTRACT)
        val moduleClass = FakeKSClassDeclaration("com.example.RepoModule", "RepoModule")
        val bindsFunc = FakeKSFunctionDeclaration("com.example.RepoModule.bindRepo", "bindRepo")
        bindsFunc.addAnnotation("com.debdut.anchordi.Binds")
        bindsFunc.addParameter("impl", abstractDecl)
        moduleClass._declarations.add(bindsFunc)

        val reporter = CollectingReporter()
        BindsImplementationValidator.validate(listOf(moduleClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("abstract"))
        assertTrue(reporter.errors[0].message.contains("com.example.BaseRepo"))
    }

    @Test
    fun bindsToObject_reportsError() {
        val objectDecl = FakeKSClassDeclaration("com.example.SingletonRepo", "SingletonRepo", ClassKind.OBJECT)
        val moduleClass = FakeKSClassDeclaration("com.example.RepoModule", "RepoModule")
        val bindsFunc = FakeKSFunctionDeclaration("com.example.RepoModule.bindRepo", "bindRepo")
        bindsFunc.addAnnotation("com.debdut.anchordi.Binds")
        bindsFunc.addParameter("impl", objectDecl)
        moduleClass._declarations.add(bindsFunc)

        val reporter = CollectingReporter()
        BindsImplementationValidator.validate(listOf(moduleClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("object"))
        assertTrue(reporter.errors[0].message.contains("com.example.SingletonRepo"))
    }

    @Test
    fun bindsToEnum_reportsError() {
        val enumDecl = FakeKSClassDeclaration("com.example.RepoEnum", "RepoEnum", ClassKind.ENUM_CLASS)
        val moduleClass = FakeKSClassDeclaration("com.example.RepoModule", "RepoModule")
        val bindsFunc = FakeKSFunctionDeclaration("com.example.RepoModule.bindRepo", "bindRepo")
        bindsFunc.addAnnotation("com.debdut.anchordi.Binds")
        bindsFunc.addParameter("impl", enumDecl)
        moduleClass._declarations.add(bindsFunc)

        val reporter = CollectingReporter()
        BindsImplementationValidator.validate(listOf(moduleClass), reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("enum"))
        assertTrue(reporter.errors[0].message.contains("com.example.RepoEnum"))
    }

    @Test
    fun moduleWithoutBinds_ignored() {
        val moduleClass = FakeKSClassDeclaration("com.example.AppModule", "AppModule")
        val reporter = CollectingReporter()
        BindsImplementationValidator.validate(listOf(moduleClass), reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
