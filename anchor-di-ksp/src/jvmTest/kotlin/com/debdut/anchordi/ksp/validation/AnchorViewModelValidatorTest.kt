package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.validation.InjectClassDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnchorViewModelValidatorTest {

    @Test
    fun nonAnchorViewModelClasses_ignored() {
        val classes = listOf(
            InjectClassDescriptor("Repo", false, false, ValidationConstants.FQN_SINGLETON_COMPONENT, true)
        )
        val reporter = CollectingReporter()
        AnchorViewModelValidator.validate(classes, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun anchorViewModelWithoutViewModelScoped_noErrorWhenComponentSet() {
        val classes = listOf(
            InjectClassDescriptor("MyVM", true, false, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, true)
        )
        val reporter = CollectingReporter()
        AnchorViewModelValidator.validate(classes, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun anchorViewModelWithWrongComponent_reportsError() {
        val classes = listOf(
            InjectClassDescriptor("MyVM", true, true, ValidationConstants.FQN_SINGLETON_COMPONENT, true)
        )
        val reporter = CollectingReporter()
        AnchorViewModelValidator.validate(classes, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("ViewModelComponent"))
    }

    @Test
    fun anchorViewModelWithoutInjectConstructor_reportsError() {
        val classes = listOf(
            InjectClassDescriptor("MyVM", true, true, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, false)
        )
        val reporter = CollectingReporter()
        AnchorViewModelValidator.validate(classes, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("@Inject constructor"))
    }

    @Test
    fun validAnchorViewModel_noErrors() {
        val classes = listOf(
            InjectClassDescriptor("MyVM", true, true, ValidationConstants.FQN_VIEW_MODEL_COMPONENT, true)
        )
        val reporter = CollectingReporter()
        AnchorViewModelValidator.validate(classes, reporter)
        assertTrue(reporter.errors.isEmpty())
    }
}
