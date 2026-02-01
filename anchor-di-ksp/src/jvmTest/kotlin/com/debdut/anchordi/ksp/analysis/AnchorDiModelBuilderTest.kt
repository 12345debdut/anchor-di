package com.debdut.anchordi.ksp.analysis

import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.debdut.anchordi.ksp.test.FakeKSValueArgument
import com.debdut.anchordi.ksp.test.FakeResolver
import com.debdut.anchordi.ksp.validation.ValidationConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AnchorDiModelBuilderTest {

    private val resolver = FakeResolver().apply {
        // Seed @Component symbols so discoverComponentFqns() returns built-ins (single source of truth).
        symbols[ComponentResolution.FQN_COMPONENT] = listOf(
            FakeKSClassDeclaration(ValidationConstants.FQN_SINGLETON_COMPONENT, "SingletonComponent"),
            FakeKSClassDeclaration(ValidationConstants.FQN_VIEW_MODEL_COMPONENT, "ViewModelComponent"),
            FakeKSClassDeclaration(ValidationConstants.FQN_NAVIGATION_COMPONENT, "NavigationComponent")
        )
    }
    private val builder = AnchorDiModelBuilder(resolver)

    @Test
    fun buildModuleDescriptors_parsesInstallInAndBinds() {
        val module = FakeKSClassDeclaration("com.example.MyModule", "MyModule")
        // @InstallIn(SingletonComponent::class): value resolved via discoverComponentFqns() by simpleName.
        module.addAnnotation("com.debdut.anchordi.InstallIn", listOf(
            FakeKSValueArgument(null, "SingletonComponent")
        ))

        // Add @Binds method
        val bindsMethod = FakeKSFunctionDeclaration("com.example.MyModule.bindApi", "bindApi")
        bindsMethod.addAnnotation("com.debdut.anchordi.Binds")
        bindsMethod.addParameter("impl", "com.example.ApiImpl")
        module._declarations.add(bindsMethod)

        // Add @Provides method
        val providesMethod = FakeKSFunctionDeclaration("com.example.MyModule.provideRepo", "provideRepo")
        providesMethod.addAnnotation("com.debdut.anchordi.Provides")
        module._declarations.add(providesMethod)

        val descriptors = builder.buildModuleDescriptors(listOf(module))

        assertEquals(1, descriptors.size)
        val desc = descriptors[0]
        assertEquals("com.example.MyModule", desc.moduleName)
        assertEquals(ValidationConstants.FQN_SINGLETON_COMPONENT, desc.installInComponentFqn)
        assertTrue(desc.hasProvidesOrBinds)
        
        assertEquals(1, desc.bindsMethods.size)
        assertEquals("bindApi", desc.bindsMethods[0].methodName)
        assertEquals(1, desc.bindsMethods[0].parameterCount)
    }

    @Test
    fun buildBindings_injectClass() {
        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService")
        // @Singleton
        injectClass.addAnnotation("com.debdut.anchordi.Singleton")
        
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val bindings = builder.buildBindings(listOf(injectClass), emptyList())

        assertEquals(1, bindings.size)
        val binding = bindings[0]
        assertEquals("com.example.MyService", binding.key)
        assertEquals(ValidationConstants.FQN_SINGLETON_COMPONENT, binding.component)
        assertEquals(ValidationConstants.FQN_SINGLETON, binding.scope)
    }
}
