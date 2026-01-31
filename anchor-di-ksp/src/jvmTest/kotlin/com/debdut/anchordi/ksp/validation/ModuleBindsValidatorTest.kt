package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindsMethodDescriptor
import com.debdut.anchordi.ksp.model.ModuleDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModuleBindsValidatorTest {

    @Test
    fun moduleWithProvidesOrBinds_noWarn() {
        val modules = listOf(
            ModuleDescriptor("pkg.AppModule", "com.debdut.anchordi.SingletonComponent", true, emptyList())
        )
        val reporter = CollectingReporter()
        ModuleBindsValidator.validate(modules, reporter)
        assertTrue(reporter.warnings.isEmpty())
    }

    @Test
    fun moduleWithNoProvidesOrBinds_warns() {
        val modules = listOf(
            ModuleDescriptor("pkg.EmptyModule", "com.debdut.anchordi.SingletonComponent", false, emptyList())
        )
        val reporter = CollectingReporter()
        ModuleBindsValidator.validate(modules, reporter)
        assertEquals(1, reporter.warnings.size)
        assertTrue(reporter.warnings[0].message.contains("no @Provides or @Binds"))
    }

    @Test
    fun bindsWithOneParam_noError() {
        val modules = listOf(
            ModuleDescriptor(
                "pkg.Module",
                "com.debdut.anchordi.SingletonComponent",
                true,
                listOf(BindsMethodDescriptor("pkg.Module", "bindApi", 1))
            )
        )
        val reporter = CollectingReporter()
        ModuleBindsValidator.validate(modules, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun bindsWithTwoParams_reportsError() {
        val modules = listOf(
            ModuleDescriptor(
                "pkg.Module",
                "com.debdut.anchordi.SingletonComponent",
                true,
                listOf(BindsMethodDescriptor("pkg.Module", "bindApi", 2))
            )
        )
        val reporter = CollectingReporter()
        ModuleBindsValidator.validate(modules, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("exactly one parameter"))
    }
}
