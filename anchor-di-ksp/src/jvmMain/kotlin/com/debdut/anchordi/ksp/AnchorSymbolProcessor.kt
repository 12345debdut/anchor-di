package com.debdut.anchordi.ksp

import com.debdut.anchordi.ksp.analysis.AnchorDiModelBuilder
import com.debdut.anchordi.ksp.codegen.AnchorDiCodeGenerator
import com.debdut.anchordi.ksp.validation.AnchorDiValidator
import com.debdut.anchordi.ksp.validation.KspValidationReporter
import com.debdut.anchordi.ksp.validation.ValidationReporter
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
class AnchorSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String> = emptyMap()
) : SymbolProcessor {

    private val reporter: ValidationReporter get() = KspValidationReporter(logger)

    companion object {
        private const val FQN_INJECT = "com.debdut.anchordi.Inject"
        private const val FQN_MODULE = "com.debdut.anchordi.Module"
    }

    private var invoked = false

    private val moduleId: String
        get() = options["anchorDiModuleId"]?.takeIf { it.isNotBlank() } ?: ""

    private val generatedObjectName: String
        get() = if (moduleId.isNotEmpty()) "AnchorGenerated_${moduleId.replace("-", "_")}" else "AnchorGenerated"

    override fun process(resolver: com.google.devtools.ksp.processing.Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        // 1. Initialize components
        val builder = AnchorDiModelBuilder(resolver)
        val validator = AnchorDiValidator(reporter)
        val generator = AnchorDiCodeGenerator(builder)

        // 2. Collect symbols (Phase 1: Discovery)
        val injectClasses = mutableListOf<KSClassDeclaration>()
        val moduleClasses = mutableListOf<KSClassDeclaration>()

        resolver.getSymbolsWithAnnotation(FQN_INJECT)
            .forEach { symbol ->
                when (symbol) {
                    is KSFunctionDeclaration -> {
                        if (symbol.isConstructor()) {
                            val parent = symbol.parent as? KSClassDeclaration
                            if (parent != null) {
                                injectClasses.add(parent)
                            }
                        }
                    }
                    is KSClassDeclaration -> {
                        if (symbol.primaryConstructor?.hasAnnotation(FQN_INJECT) == true) {
                            injectClasses.add(symbol)
                        }
                    }
                    else -> {}
                }
            }

        resolver.getSymbolsWithAnnotation(FQN_MODULE)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .forEach { moduleClasses.add(it) }

        val distinctInject = injectClasses.distinct()

        // 3. Build Model (Phase 2: Analysis)
        val components = builder.buildComponents()
        val bindings = builder.buildBindings(distinctInject, moduleClasses)
        val injectClassDescriptors = builder.buildInjectClassDescriptors(distinctInject)
        val moduleDescriptors = builder.buildModuleDescriptors(moduleClasses)
        val (providedKeys, requirements) = builder.buildProvidedKeysAndRequirements(distinctInject, moduleClasses)
        val dependencyGraph = builder.buildDependencyGraph(distinctInject, moduleClasses)

        // 4. Validate (Phase 3: Validation)
        validator.validateSymbols(distinctInject, moduleClasses)
        validator.validateAll(
            bindings = bindings,
            injectClassDescriptors = injectClassDescriptors,
            moduleDescriptors = moduleDescriptors,
            components = components,
            providedKeys = providedKeys,
            requirements = requirements,
            dependencyGraph = dependencyGraph
        )

        // 5. Generate Code (Phase 4: Codegen) — multiple files for separation of concerns and scaling
        val packageName = "com.debdut.anchordi.generated"
        val generatedFiles = generator.generateAllFiles(
            packageName = packageName,
            injectClasses = distinctInject,
            moduleClasses = moduleClasses,
            baseObjectName = generatedObjectName
        )

        val dependencies = com.google.devtools.ksp.processing.Dependencies.ALL_FILES
        generatedFiles.forEach { (fileName, content) ->
            val nameWithoutExt = fileName.removeSuffix(".kt")
            val file = codeGenerator.createNewFile(dependencies, packageName, nameWithoutExt)
            file.write(content.toByteArray())
            file.close()
        }

        invoked = true

        return emptyList()
    }

    private fun KSFunctionDeclaration.isConstructor(): Boolean =
        parent is KSClassDeclaration && (parent as KSClassDeclaration).primaryConstructor == this
}
