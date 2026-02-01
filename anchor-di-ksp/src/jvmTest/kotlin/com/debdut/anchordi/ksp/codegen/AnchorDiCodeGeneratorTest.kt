package com.debdut.anchordi.ksp.codegen

import com.debdut.anchordi.ksp.analysis.AnchorDiModelBuilder
import com.debdut.anchordi.ksp.analysis.ComponentResolution
import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.debdut.anchordi.ksp.test.FakeKSValueArgument
import com.debdut.anchordi.ksp.test.FakeResolver
import com.debdut.anchordi.ksp.validation.ValidationConstants
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnchorDiCodeGeneratorTest {

    private val resolver = FakeResolver().apply {
        symbols[ComponentResolution.FQN_COMPONENT] = listOf(
            FakeKSClassDeclaration(ValidationConstants.FQN_SINGLETON_COMPONENT, "SingletonComponent"),
            FakeKSClassDeclaration(ValidationConstants.FQN_VIEW_MODEL_COMPONENT, "ViewModelComponent"),
            FakeKSClassDeclaration(ValidationConstants.FQN_NAVIGATION_COMPONENT, "NavigationComponent")
        )
    }
    private val builder = AnchorDiModelBuilder(resolver)
    private val generator = AnchorDiCodeGenerator(builder)

    // ===================
    // Basic @Inject Tests
    // ===================

    @Test
    fun generateAnchorContributor_injectClass_generatesFactory() {
        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService")
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        // param: repo: Repo
        constructor.addParameter("repo", "com.example.Repo")
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        // Verify key parts of generated code
        assertTrue(output.contains("class MyService_Factory : Factory<com.example.MyService>"))
        assertTrue(output.contains("val repo = container.get<com.example.Repo>()"))
        assertTrue(output.contains("return com.example.MyService(repo)"))
        assertTrue(output.contains("registry.register(Key(\"com.example.MyService\", null), Binding.Unscoped(MyService_Factory()))"))
    }

    @Test
    fun generateAllFiles_producesMultipleFilesAndAggregator() {
        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService")
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        constructor.addParameter("repo", "com.example.Repo")
        injectClass._primaryConstructor = constructor

        val files = generator.generateAllFiles(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated_app"
        )

        // Per-group factory file + one contributor (Inject) + aggregator
        assertTrue(files.size >= 3)
        val fileNames = files.map { it.fileName }.toSet()
        assertTrue(fileNames.contains("AnchorGenerated_app_Inject_Factories.kt"))
        assertTrue(fileNames.contains("AnchorGenerated_app.kt"))
        assertTrue(fileNames.contains("AnchorGenerated_app_Inject.kt"))

        val factoriesFile = files.find { it.fileName == "AnchorGenerated_app_Inject_Factories.kt" }!!.content
        assertTrue(factoriesFile.contains("internal class MyService_Factory"))

        val aggregatorFile = files.find { it.fileName == "AnchorGenerated_app.kt" }!!.content
        assertTrue(aggregatorFile.contains("object AnchorGenerated_app : ComponentBindingContributor"))
        assertTrue(aggregatorFile.contains("AnchorGenerated_app_Inject.contribute(registry)"))
    }

    // ===================
    // Scope Annotation Tests
    // ===================

    @Test
    fun generateAnchorContributor_singletonScope_generatesSingletonBinding() {
        val injectClass = FakeKSClassDeclaration("com.example.SingletonService", "SingletonService")
        injectClass.addAnnotation("com.debdut.anchordi.Singleton")
        val constructor = FakeKSFunctionDeclaration("com.example.SingletonService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        assertTrue(output.contains("Binding.Singleton(SingletonService_Factory())"))
    }

    @Test
    fun generateAnchorContributor_viewModelScoped_generatesScopedBinding() {
        val injectClass = FakeKSClassDeclaration("com.example.MyViewModel", "MyViewModel")
        injectClass.addAnnotation("com.debdut.anchordi.ViewModelScoped")
        val constructor = FakeKSFunctionDeclaration("com.example.MyViewModel.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        assertTrue(output.contains("Binding.Scoped(\"${ValidationConstants.FQN_VIEW_MODEL_COMPONENT}\", MyViewModel_Factory())"))
    }

    @Test
    fun generateAnchorContributor_navigationScoped_generatesScopedBinding() {
        val injectClass = FakeKSClassDeclaration("com.example.ScreenState", "ScreenState")
        injectClass.addAnnotation("com.debdut.anchordi.NavigationScoped")
        val constructor = FakeKSFunctionDeclaration("com.example.ScreenState.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        assertTrue(output.contains("Binding.Scoped(\"${ValidationConstants.FQN_NAVIGATION_COMPONENT}\", ScreenState_Factory())"))
    }

    // ===================
    // Qualifier Tests
    // ===================

    @Test
    fun generateAnchorContributor_namedQualifier_includesQualifierInKey() {
        val injectClass = FakeKSClassDeclaration("com.example.ApiClient", "ApiClient")
        injectClass.addAnnotation("com.debdut.anchordi.Named", listOf(
            FakeKSValueArgument(null, "production")
        ))
        val constructor = FakeKSFunctionDeclaration("com.example.ApiClient.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        // Key should include qualifier
        assertTrue(output.contains("Key(\"com.example.ApiClient\", \"production\")"))
    }

    // ===================
    // Multiple Dependencies Tests
    // ===================

    @Test
    fun generateAnchorContributor_multipleDependencies_resolvesAll() {
        val injectClass = FakeKSClassDeclaration("com.example.ComplexService", "ComplexService")
        val constructor = FakeKSFunctionDeclaration("com.example.ComplexService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        constructor.addParameter("repo", "com.example.Repository")
        constructor.addParameter("api", "com.example.ApiClient")
        constructor.addParameter("logger", "com.example.Logger")
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        assertTrue(output.contains("val repo = container.get<com.example.Repository>()"))
        assertTrue(output.contains("val api = container.get<com.example.ApiClient>()"))
        assertTrue(output.contains("val logger = container.get<com.example.Logger>()"))
        assertTrue(output.contains("return com.example.ComplexService(repo, api, logger)"))
    }

    // ===================
    // No Dependencies Test
    // ===================

    @Test
    fun generateAnchorContributor_noDependencies_simpleConstruction() {
        val injectClass = FakeKSClassDeclaration("com.example.SimpleClass", "SimpleClass")
        val constructor = FakeKSFunctionDeclaration("com.example.SimpleClass.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        // No parameters
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        assertTrue(output.contains("return com.example.SimpleClass()"))
        // Should not contain "container.get" since no dependencies
        assertFalse(output.contains("container.get<"))
    }

    // ===================
    // Factory Structure Tests
    // ===================

    @Test
    fun generateAnchorContributor_factoryImplementsCorrectInterface() {
        val injectClass = FakeKSClassDeclaration("com.example.MyService", "MyService")
        val constructor = FakeKSFunctionDeclaration("com.example.MyService.<init>", "<init>")
        constructor.addAnnotation("com.debdut.anchordi.Inject")
        injectClass._primaryConstructor = constructor

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(injectClass),
            emptyList(),
            "AnchorGenerated"
        )

        assertTrue(output.contains("class MyService_Factory : Factory<com.example.MyService>"))
        assertTrue(output.contains("override fun create(container: com.debdut.anchordi.runtime.AnchorContainer): com.example.MyService"))
    }

    // ===================
    // Multiple Classes Test
    // ===================

    @Test
    fun generateAnchorContributor_multipleClasses_generatesAllFactories() {
        val class1 = FakeKSClassDeclaration("com.example.ServiceA", "ServiceA")
        val constructor1 = FakeKSFunctionDeclaration("com.example.ServiceA.<init>", "<init>")
        constructor1.addAnnotation("com.debdut.anchordi.Inject")
        class1._primaryConstructor = constructor1

        val class2 = FakeKSClassDeclaration("com.example.ServiceB", "ServiceB")
        val constructor2 = FakeKSFunctionDeclaration("com.example.ServiceB.<init>", "<init>")
        constructor2.addAnnotation("com.debdut.anchordi.Inject")
        class2._primaryConstructor = constructor2

        val output = generator.generateAnchorContributor(
            "com.example.generated",
            listOf(class1, class2),
            emptyList(),
            "AnchorGenerated"
        )

        assertTrue(output.contains("class ServiceA_Factory"))
        assertTrue(output.contains("class ServiceB_Factory"))
        assertTrue(output.contains("Key(\"com.example.ServiceA\""))
        assertTrue(output.contains("Key(\"com.example.ServiceB\""))
    }
}
