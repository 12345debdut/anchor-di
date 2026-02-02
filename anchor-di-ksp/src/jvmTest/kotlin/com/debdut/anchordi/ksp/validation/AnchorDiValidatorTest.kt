package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import kotlin.test.Test
import kotlin.test.assertTrue

class AnchorDiValidatorTest {
    @Test
    fun validateAll_runsAllValidators() {
        val reporter = CollectingReporter()
        val validator = AnchorDiValidator(reporter)

        // Setup inputs that should trigger errors in multiple validators to prove they all ran

        // 1. Missing bindings input
        val requirements =
            listOf(
                DependencyRequirement("RequiredType", "Requester"),
            )
        val providedKeys = emptySet<String>()

        // 2. Duplicate bindings input
        val bindings =
            listOf(
                BindingDescriptor("Key", null, "Component", null, "Source1"),
                BindingDescriptor("Key", null, "Component", null, "Source2"),
            )

        // 3. Cycles input
        val graph = mapOf("A" to setOf("A"))

        // Run validation
        validator.validateAll(
            bindings = bindings,
            injectClassDescriptors = emptyList(),
            moduleDescriptors = emptyList(),
            components = emptyMap(),
            providedKeys = providedKeys,
            requirements = requirements,
            dependencyGraph = graph,
        )

        // Verify errors from different validators are present
        val errorMessages = reporter.errors.map { it.message }

        // From MissingBindingValidator
        assertTrue(errorMessages.any { it.contains("has no binding") }, "Should report missing binding")

        // From DuplicateBindingValidator
        assertTrue(errorMessages.any { it.contains("Duplicate binding") }, "Should report duplicate binding")

        // From CycleValidator
        assertTrue(errorMessages.any { it.contains("Circular dependency") }, "Should report cycle")
    }

    @Test
    fun validateAll_validInput_reportsNoErrors() {
        val reporter = CollectingReporter()
        val validator = AnchorDiValidator(reporter)

        validator.validateAll(
            bindings = emptyList(),
            injectClassDescriptors = emptyList(),
            moduleDescriptors = emptyList(),
            components = emptyMap(),
            providedKeys = emptySet(),
            requirements = emptyList(),
            dependencyGraph = emptyMap(),
        )

        assertTrue(reporter.errors.isEmpty())
        assertTrue(reporter.warnings.isEmpty())
    }

    @Test
    fun validateSymbols_runsSymbolValidators() {
        val reporter = CollectingReporter()
        val validator = AnchorDiValidator(reporter)

        // Interface is not allowed for @Inject
        val injectClass = FakeKSClassDeclaration("com.example.IApi", "IApi", ClassKind.INTERFACE)
        val moduleWithBindsToInterface = FakeKSClassDeclaration("com.example.Module", "Module")
        val bindsFunc = FakeKSFunctionDeclaration("com.example.Module.bind", "bind")
        bindsFunc.addAnnotation("com.debdut.anchordi.Binds")
        val interfaceDecl = FakeKSClassDeclaration("com.example.IFoo", "IFoo", ClassKind.INTERFACE)
        bindsFunc.addParameter("impl", interfaceDecl)
        moduleWithBindsToInterface.declarationsList.add(bindsFunc)

        validator.validateSymbols(listOf(injectClass), listOf(moduleWithBindsToInterface))

        val errorMessages = reporter.errors.map { it.message }
        val hasInterfaceError =
            errorMessages.any { it.contains("interface") && it.contains("com.example.IApi") }
        assertTrue(hasInterfaceError, "Should report interface not allowed for @Inject")
        assertTrue(errorMessages.any { it.contains("@Binds") && it.contains("interface") }, "Should report @Binds to interface")
    }

    @Test
    fun validateSymbols_validInput_reportsNoErrors() {
        val reporter = CollectingReporter()
        val validator = AnchorDiValidator(reporter)

        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService", ClassKind.CLASS)
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass.primaryConstructorBacking = constructor

        validator.validateSymbols(listOf(injectClass), emptyList())

        assertTrue(reporter.errors.isEmpty())
    }
}
