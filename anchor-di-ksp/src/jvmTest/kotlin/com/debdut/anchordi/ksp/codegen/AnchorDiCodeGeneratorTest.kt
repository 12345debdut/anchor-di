package com.debdut.anchordi.ksp.codegen

import com.debdut.anchordi.ksp.analysis.AnchorDiModelBuilder
import com.debdut.anchordi.ksp.test.FakeKSClassDeclaration
import com.debdut.anchordi.ksp.test.FakeKSFunctionDeclaration
import com.debdut.anchordi.ksp.test.FakeResolver
import kotlin.test.Test
import kotlin.test.assertTrue

class AnchorDiCodeGeneratorTest {

    private val resolver = FakeResolver()
    private val builder = AnchorDiModelBuilder(resolver)
    private val generator = AnchorDiCodeGenerator(builder)

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
}
