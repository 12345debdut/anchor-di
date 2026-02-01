package com.debdut.anchordi.ksp.analysis

import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.debdut.anchordi.ksp.test.FakeKSValueArgument
import com.debdut.anchordi.ksp.test.FakeResolver
import com.debdut.anchordi.ksp.validation.ValidationConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

    // ===================
    // Module Descriptor Tests
    // ===================

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
    fun buildModuleDescriptors_viewModelComponent() {
        val module = FakeKSClassDeclaration("com.example.ViewModelModule", "ViewModelModule")
        module.addAnnotation("com.debdut.anchordi.InstallIn", listOf(
            FakeKSValueArgument(null, "ViewModelComponent")
        ))

        val providesMethod = FakeKSFunctionDeclaration("com.example.ViewModelModule.provideUseCase", "provideUseCase")
        providesMethod.addAnnotation("com.debdut.anchordi.Provides")
        module._declarations.add(providesMethod)

        val descriptors = builder.buildModuleDescriptors(listOf(module))

        assertEquals(1, descriptors.size)
        assertEquals(ValidationConstants.FQN_VIEW_MODEL_COMPONENT, descriptors[0].installInComponentFqn)
    }

    @Test
    fun buildModuleDescriptors_navigationComponent() {
        val module = FakeKSClassDeclaration("com.example.NavModule", "NavModule")
        module.addAnnotation("com.debdut.anchordi.InstallIn", listOf(
            FakeKSValueArgument(null, "NavigationComponent")
        ))

        val providesMethod = FakeKSFunctionDeclaration("com.example.NavModule.provideState", "provideState")
        providesMethod.addAnnotation("com.debdut.anchordi.Provides")
        module._declarations.add(providesMethod)

        val descriptors = builder.buildModuleDescriptors(listOf(module))

        assertEquals(1, descriptors.size)
        assertEquals(ValidationConstants.FQN_NAVIGATION_COMPONENT, descriptors[0].installInComponentFqn)
    }

    // ===================
    // Binding Tests
    // ===================

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

    @Test
    fun buildBindings_viewModelScoped() {
        val injectClass = FakeKSClassDeclaration("com.example.MyViewModel", "MyViewModel")
        injectClass.addAnnotation("com.debdut.anchordi.ViewModelScoped")
        
        val constructor = FakeKSFunctionDeclaration("com.example.MyViewModel.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val bindings = builder.buildBindings(listOf(injectClass), emptyList())

        assertEquals(1, bindings.size)
        val binding = bindings[0]
        assertEquals("com.example.MyViewModel", binding.key)
        assertEquals(ValidationConstants.FQN_VIEW_MODEL_COMPONENT, binding.component)
        assertEquals(ValidationConstants.FQN_VIEW_MODEL_SCOPED, binding.scope)
    }

    @Test
    fun buildBindings_navigationScoped() {
        val injectClass = FakeKSClassDeclaration("com.example.ScreenState", "ScreenState")
        injectClass.addAnnotation("com.debdut.anchordi.NavigationScoped")
        
        val constructor = FakeKSFunctionDeclaration("com.example.ScreenState.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val bindings = builder.buildBindings(listOf(injectClass), emptyList())

        assertEquals(1, bindings.size)
        val binding = bindings[0]
        assertEquals("com.example.ScreenState", binding.key)
        assertEquals(ValidationConstants.FQN_NAVIGATION_COMPONENT, binding.component)
        assertEquals(ValidationConstants.FQN_NAVIGATION_SCOPED, binding.scope)
    }

    @Test
    fun buildBindings_unscoped() {
        val injectClass = FakeKSClassDeclaration("com.example.UnscopedService", "UnscopedService")
        // No scope annotation
        
        val constructor = FakeKSFunctionDeclaration("com.example.UnscopedService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val bindings = builder.buildBindings(listOf(injectClass), emptyList())

        assertEquals(1, bindings.size)
        val binding = bindings[0]
        assertEquals("com.example.UnscopedService", binding.key)
        assertNull(binding.scope) // Unscoped has null scope
    }

    @Test
    fun buildBindings_withDependencies() {
        val repoClass = FakeKSClassDeclaration("com.example.Repository", "Repository")
        val repoConstructor = FakeKSFunctionDeclaration("com.example.Repository.<init>", "<init>")
        repoConstructor.addAnnotation("com.debdut.anchordi.Inject")
        repoClass._primaryConstructor = repoConstructor

        val serviceClass = FakeKSClassDeclaration("com.example.Service", "Service")
        val serviceConstructor = FakeKSFunctionDeclaration("com.example.Service.<init>", "<init>")
        serviceConstructor.addAnnotation("com.debdut.anchordi.Inject")
        serviceConstructor.addParameter("repo", "com.example.Repository")
        serviceClass._primaryConstructor = serviceConstructor

        val bindings = builder.buildBindings(listOf(repoClass, serviceClass), emptyList())

        assertEquals(2, bindings.size)
        val serviceBinding = bindings.find { it.key == "com.example.Service" }
        assertNotNull(serviceBinding)
        assertTrue(serviceBinding.dependencies.contains("com.example.Repository"))
    }

    // ===================
    // Dependency Graph Tests
    // ===================

    @Test
    fun buildDependencyGraph_simple() {
        val repoClass = FakeKSClassDeclaration("com.example.Repository", "Repository")
        val repoConstructor = FakeKSFunctionDeclaration("com.example.Repository.<init>", "<init>")
        repoConstructor.addAnnotation("com.debdut.anchordi.Inject")
        repoClass._primaryConstructor = repoConstructor

        val serviceClass = FakeKSClassDeclaration("com.example.Service", "Service")
        val serviceConstructor = FakeKSFunctionDeclaration("com.example.Service.<init>", "<init>")
        serviceConstructor.addAnnotation("com.debdut.anchordi.Inject")
        serviceConstructor.addParameter("repo", "com.example.Repository")
        serviceClass._primaryConstructor = serviceConstructor

        val graph = builder.buildDependencyGraph(listOf(serviceClass, repoClass), emptyList())

        assertTrue(graph.containsKey("com.example.Service"))
        assertTrue(graph["com.example.Service"]!!.contains("com.example.Repository"))
        assertTrue(graph.containsKey("com.example.Repository"))
        assertTrue(graph["com.example.Repository"]!!.isEmpty())
    }

    @Test
    fun buildDependencyGraph_multipleDepth() {
        val apiClass = FakeKSClassDeclaration("com.example.Api", "Api")
        val apiConstructor = FakeKSFunctionDeclaration("com.example.Api.<init>", "<init>")
        apiConstructor.addAnnotation("com.debdut.anchordi.Inject")
        apiClass._primaryConstructor = apiConstructor

        val repoClass = FakeKSClassDeclaration("com.example.Repository", "Repository")
        val repoConstructor = FakeKSFunctionDeclaration("com.example.Repository.<init>", "<init>")
        repoConstructor.addAnnotation("com.debdut.anchordi.Inject")
        repoConstructor.addParameter("api", "com.example.Api")
        repoClass._primaryConstructor = repoConstructor

        val serviceClass = FakeKSClassDeclaration("com.example.Service", "Service")
        val serviceConstructor = FakeKSFunctionDeclaration("com.example.Service.<init>", "<init>")
        serviceConstructor.addAnnotation("com.debdut.anchordi.Inject")
        serviceConstructor.addParameter("repo", "com.example.Repository")
        serviceClass._primaryConstructor = serviceConstructor

        val graph = builder.buildDependencyGraph(listOf(serviceClass, repoClass, apiClass), emptyList())

        assertEquals(setOf("com.example.Repository"), graph["com.example.Service"])
        assertEquals(setOf("com.example.Api"), graph["com.example.Repository"])
        assertEquals(emptySet<String>(), graph["com.example.Api"])
    }

    // ===================
    // Provided Keys and Requirements Tests
    // ===================

    @Test
    fun buildProvidedKeysAndRequirements_injectClasses() {
        val repoClass = FakeKSClassDeclaration("com.example.Repository", "Repository")
        val repoConstructor = FakeKSFunctionDeclaration("com.example.Repository.<init>", "<init>")
        repoConstructor.addAnnotation("com.debdut.anchordi.Inject")
        repoClass._primaryConstructor = repoConstructor

        val serviceClass = FakeKSClassDeclaration("com.example.Service", "Service")
        val serviceConstructor = FakeKSFunctionDeclaration("com.example.Service.<init>", "<init>")
        serviceConstructor.addAnnotation("com.debdut.anchordi.Inject")
        serviceConstructor.addParameter("repo", "com.example.Repository")
        serviceClass._primaryConstructor = serviceConstructor

        val (providedKeys, requirements) = builder.buildProvidedKeysAndRequirements(
            listOf(serviceClass, repoClass),
            emptyList()
        )

        assertTrue(providedKeys.contains("com.example.Repository"))
        assertTrue(providedKeys.contains("com.example.Service"))
        
        val serviceReq = requirements.find { it.consumer == "com.example.Service" }
        assertNotNull(serviceReq)
        assertEquals("com.example.Repository", serviceReq.requiredKey)
    }

    // ===================
    // Inject Class Descriptor Tests  
    // ===================

    @Test
    fun buildInjectClassDescriptors_basic() {
        val myClass = FakeKSClassDeclaration("com.example.MyClass", "MyClass")
        val constructor = FakeKSFunctionDeclaration("com.example.MyClass.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        myClass._primaryConstructor = constructor

        val descriptors = builder.buildInjectClassDescriptors(listOf(myClass))

        assertEquals(1, descriptors.size)
        assertEquals("com.example.MyClass", descriptors[0].qualifiedName)
        assertEquals("MyClass", descriptors[0].simpleName)
    }
}
