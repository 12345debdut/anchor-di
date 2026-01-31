package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.debdut.anchordi.ksp.model.ModuleDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModuleInstallInValidatorTest {

    private val knownComponents = mapOf(
        ValidationConstants.FQN_SINGLETON_COMPONENT to ComponentDescriptor(ValidationConstants.FQN_SINGLETON_COMPONENT),
        ValidationConstants.FQN_VIEW_MODEL_COMPONENT to ComponentDescriptor(ValidationConstants.FQN_VIEW_MODEL_COMPONENT)
    )

    @Test
    fun moduleWithValidInstallIn_noError() {
        val modules = listOf(
            ModuleDescriptor("pkg.AppModule", ValidationConstants.FQN_SINGLETON_COMPONENT, true, emptyList())
        )
        val reporter = CollectingReporter()
        ModuleInstallInValidator.validate(modules, knownComponents, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun moduleWithNullInstallIn_reportsError() {
        val modules = listOf(
            ModuleDescriptor("pkg.AppModule", null, true, emptyList())
        )
        val reporter = CollectingReporter()
        ModuleInstallInValidator.validate(modules, knownComponents, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("must declare @InstallIn"))
    }

    @Test
    fun moduleWithUnknownComponent_reportsError() {
        val modules = listOf(
            ModuleDescriptor("pkg.AppModule", "pkg.UnknownScope", true, emptyList())
        )
        val reporter = CollectingReporter()
        ModuleInstallInValidator.validate(modules, knownComponents, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("unknown component"))
    }
}
