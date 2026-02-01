package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DuplicateBindingValidatorTest {

    @Test
    fun noDuplicates_reportsNoErrors() {
        val bindings = listOf(
            BindingDescriptor("Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Api"),
            BindingDescriptor("Repo", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Repo")
        )
        val reporter = CollectingReporter()
        DuplicateBindingValidator.validate(bindings, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun duplicateSameKeyQualifierComponent_reportsError() {
        val bindings = listOf(
            BindingDescriptor("Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module1.provideApi"),
            BindingDescriptor("Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module2.provideApi")
        )
        val reporter = CollectingReporter()
        DuplicateBindingValidator.validate(bindings, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Duplicate binding"))
        assertTrue(reporter.errors[0].message.contains("Api"))
    }

    @Test
    fun sameKeyDifferentComponent_noError() {
        val bindings = listOf(
            BindingDescriptor("Api", null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "AppModule.provideApi"),
            BindingDescriptor("Api", null, "com.debdut.anchordi.ViewModelComponent", null, "ViewModelModule.provideApi")
        )
        val reporter = CollectingReporter()
        DuplicateBindingValidator.validate(bindings, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun multibindingSet_multipleContributionsSameKey_noError() {
        val setKey = "kotlin.collections.Set<com.example.Tracker>"
        val bindings = listOf(
            BindingDescriptor(setKey, null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module1.provideTrackerA", multibindingKind = "set"),
            BindingDescriptor(setKey, null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module2.provideTrackerB", multibindingKind = "set")
        )
        val reporter = CollectingReporter()
        DuplicateBindingValidator.validate(bindings, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun multibindingMap_multipleContributionsDifferentMapKeys_noError() {
        val mapKey = "kotlin.collections.Map<kotlin.String,com.example.Tracker>"
        val bindings = listOf(
            BindingDescriptor(mapKey, null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module1.provideFirebase", multibindingKind = "map", mapKey = "firebase"),
            BindingDescriptor(mapKey, null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module2.provideAnalytics", multibindingKind = "map", mapKey = "analytics")
        )
        val reporter = CollectingReporter()
        DuplicateBindingValidator.validate(bindings, reporter)
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun multibindingMap_duplicateMapKey_reportsError() {
        val mapKey = "kotlin.collections.Map<kotlin.String,com.example.Tracker>"
        val bindings = listOf(
            BindingDescriptor(mapKey, null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module1.provideFirebase", multibindingKind = "map", mapKey = "firebase"),
            BindingDescriptor(mapKey, null, ValidationConstants.FQN_SINGLETON_COMPONENT, null, "Module2.provideFirebaseAgain", multibindingKind = "map", mapKey = "firebase")
        )
        val reporter = CollectingReporter()
        DuplicateBindingValidator.validate(bindings, reporter)
        assertEquals(1, reporter.errors.size)
        assertTrue(reporter.errors[0].message.contains("Duplicate map key"))
        assertTrue(reporter.errors[0].message.contains("firebase"))
    }
}
