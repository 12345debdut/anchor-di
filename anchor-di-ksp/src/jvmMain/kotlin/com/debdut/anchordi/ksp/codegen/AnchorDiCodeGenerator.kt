package com.debdut.anchordi.ksp.codegen

import com.debdut.anchordi.ksp.KspUtils
import com.debdut.anchordi.ksp.analysis.AnchorDiModelBuilder
import com.debdut.anchordi.ksp.findAnnotation
import com.debdut.anchordi.ksp.hasAnnotation
import com.debdut.anchordi.ksp.validation.ValidationConstants
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * A single generated file: (fileName without path, content).
 */
data class GeneratedFile(val fileName: String, val content: String)

/**
 * Generates the AnchorGenerated code, optionally split into multiple files
 * (factories, one contributor per component, aggregator) for separation of concerns and scaling.
 */
class AnchorDiCodeGenerator(
    private val builder: AnchorDiModelBuilder
) {

    companion object {
        private const val FQN_INJECT = "com.debdut.anchordi.Inject"
        private const val FQN_INSTALL_IN = "com.debdut.anchordi.InstallIn"
        private const val FQN_PROVIDES = "com.debdut.anchordi.Provides"
        private const val FQN_BINDS = "com.debdut.anchordi.Binds"
        private const val FQN_SINGLETON = "com.debdut.anchordi.Singleton"
        private const val FQN_SCOPED = "com.debdut.anchordi.Scoped"
        private const val FQN_VIEW_MODEL_SCOPED = "com.debdut.anchordi.ViewModelScoped"
        private const val FQN_NAVIGATION_SCOPED = "com.debdut.anchordi.NavigationScoped"
        private const val FQN_NAMED = "com.debdut.anchordi.Named"
        private const val FQN_ANCHOR_VIEW_MODEL = "com.debdut.anchordi.compose.AnchorViewModel"
        private const val FQN_INTO_SET = "com.debdut.anchordi.IntoSet"
        private const val FQN_INTO_MAP = "com.debdut.anchordi.IntoMap"
        private const val FQN_STRING_KEY = "com.debdut.anchordi.StringKey"
        /** Group key for @Inject bindings not exclusively @Binds in one module. */
        private const val INJECT_GROUP_KEY = "Inject"
    }

    /**
     * Generates multiple files: one for factories, one contributor per component, and an aggregator.
     * The aggregator object name is [baseObjectName]; app code continues to use e.g. AnchorGenerated_composeapp.
     */
    fun generateAllFiles(
        packageName: String,
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>,
        baseObjectName: String
    ): List<GeneratedFile> {
        val grouped = buildGroupedRegistrations(injectClasses, moduleClasses)
        val injectClassesByGroup = groupInjectClassesByModule(injectClasses, moduleClasses)
        val files = mutableListOf<GeneratedFile>()

        // 1. Factory files — one per group (module or Inject) so they don't bloat
        val factoryGroupKeys = injectClassesByGroup.keys
            .filter { injectClassesByGroup[it]?.isNotEmpty() == true }
            .sortedBy { if (it == INJECT_GROUP_KEY) 1 else 0; it }
        factoryGroupKeys.forEach { groupKey ->
            val classes = injectClassesByGroup[groupKey] ?: emptyList()
            if (classes.isNotEmpty()) {
                files.add(
                    GeneratedFile(
                        fileName = "${baseObjectName}_${groupKey}_Factories.kt",
                        content = generateFactoriesFile(packageName, classes)
                    )
                )
            }
        }

        // Modules first (sorted), then "Inject" last
        val contributorSuffixes = grouped.keys
            .filter { (grouped[it]?.size ?: 0) > 0 }
            .sortedBy { if (it == INJECT_GROUP_KEY) 1 else 0; it }

        // 2. One contributor file per module / Inject (non-empty only)
        contributorSuffixes.forEach { suffix ->
            val blocks = grouped[suffix] ?: emptyList()
            files.add(
                GeneratedFile(
                    fileName = "${baseObjectName}_$suffix.kt",
                    content = generateContributorFile(packageName, baseObjectName, suffix, blocks)
                )
            )
        }

        // 3. Aggregator file (single entry point; app still uses AnchorGenerated_composeapp)
        files.add(
            GeneratedFile(
                fileName = "$baseObjectName.kt",
                content = generateAggregatorFile(packageName, baseObjectName, contributorSuffixes)
            )
        )

        return files
    }

    /**
     * Groups @Inject classes by the same key used for registrations: module that @Binds them
     * (if exactly one), otherwise "Inject". Used to split factory files by module/Inject.
     */
    fun groupInjectClassesByModule(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>
    ): Map<String, List<KSClassDeclaration>> {
        val implFqnToModule = builder.getBindsImplModuleMap(moduleClasses)
        val byGroup = mutableMapOf<String, MutableList<KSClassDeclaration>>()
        injectClasses.forEach { classDecl ->
            val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEach
            val groupKey = implFqnToModule[qualifiedName] ?: INJECT_GROUP_KEY
            byGroup.getOrPut(groupKey) { mutableListOf() }.add(classDecl)
        }
        return byGroup
    }

    /**
     * Builds registrations grouped by module or "Inject".
     * - @Provides and @Binds: grouped by the defining @Module's simple name (e.g. AppModule, LoggerModule).
     * - @Inject classes: grouped by the module that @Binds them (if exactly one); otherwise "Inject".
     * Each value is a list of "blocks" (each block = list of source lines for one registry.register).
     */
    fun buildGroupedRegistrations(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>
    ): GroupedRegistrations {
        val implFqnToComponent = builder.getBindsImplComponentMap(moduleClasses)
        val implFqnToModule = builder.getBindsImplModuleMap(moduleClasses)
        val grouped = mutableMapOf<String, MutableList<List<String>>>()

        fun addBlock(groupKey: String, lines: List<String>) {
            grouped.getOrPut(groupKey) { mutableListOf() }.add(lines)
        }

        // @Inject class bindings — group by module that @Binds this class (if unique), else "Inject"
        injectClasses.forEach { classDecl ->
            val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEach
            val simpleName = classDecl.simpleName.asString()
            val hasAnchorViewModel = classDecl.hasAnnotation(FQN_ANCHOR_VIEW_MODEL)
            val hasViewModelScoped = classDecl.primaryConstructor?.hasAnnotation(FQN_VIEW_MODEL_SCOPED) == true
                || classDecl.hasAnnotation(FQN_VIEW_MODEL_SCOPED)
            val hasNavigationScoped = classDecl.primaryConstructor?.hasAnnotation(FQN_NAVIGATION_SCOPED) == true
                || classDecl.hasAnnotation(FQN_NAVIGATION_SCOPED)
            val scopedAnnotation = classDecl.primaryConstructor?.findAnnotation(FQN_SCOPED)
                ?: classDecl.findAnnotation(FQN_SCOPED)
            val hasSingleton = classDecl.primaryConstructor?.hasAnnotation(FQN_SINGLETON) == true
                || classDecl.hasAnnotation(FQN_SINGLETON)
            val binding = when {
                hasAnchorViewModel || hasViewModelScoped -> "Binding.Scoped(\"${ValidationConstants.FQN_VIEW_MODEL_COMPONENT}\", ${simpleName}_Factory())"
                hasNavigationScoped -> "Binding.Scoped(\"${ValidationConstants.FQN_NAVIGATION_COMPONENT}\", ${simpleName}_Factory())"
                scopedAnnotation != null -> {
                    val scopeClass = KspUtils.getScopedClassName(scopedAnnotation) ?: return@forEach
                    "Binding.Scoped(\"$scopeClass\", ${simpleName}_Factory())"
                }
                hasSingleton -> "Binding.Singleton(${simpleName}_Factory())"
                else -> {
                    val inherited = implFqnToComponent[qualifiedName]
                    if (inherited != null) {
                        "Binding.Scoped(\"$inherited\", ${simpleName}_Factory())"
                    } else {
                        "Binding.Unscoped(${simpleName}_Factory())"
                    }
                }
            }
            val groupKey = implFqnToModule[qualifiedName] ?: INJECT_GROUP_KEY
            addBlock(groupKey, listOf("registry.register(Key(\"$qualifiedName\", null), $binding)"))
        }

        // Module @Provides and @Binds — group by module simple name
        moduleClasses.forEach { moduleDecl ->
            val moduleName = moduleDecl.qualifiedName?.asString() ?: return@forEach
            val moduleSimpleName = moduleDecl.simpleName.asString()
            val installIn = moduleDecl.findAnnotation(FQN_INSTALL_IN) ?: return@forEach
            val componentScopeId = builder.getComponentScopeIdFromInstallIn(installIn)
                ?: return@forEach
            val isSingletonComponent = componentScopeId == ValidationConstants.FQN_SINGLETON_COMPONENT
            val scopeClassName = when (componentScopeId) {
                ValidationConstants.FQN_SINGLETON_COMPONENT -> null
                else -> componentScopeId
            }

            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { func ->
                if (func.hasAnnotation(FQN_BINDS)) {
                    val block = buildBindsRegistrationLines(func, isSingletonComponent, scopeClassName)
                    if (block.isNotEmpty()) addBlock(moduleSimpleName, block)
                } else if (func.hasAnnotation(FQN_PROVIDES)) {
                    val block = buildProvidesRegistrationLines(moduleName, func, scopeClassName)
                    if (block.isNotEmpty()) addBlock(moduleSimpleName, block)
                }
            }
        }

        return grouped
    }

    fun generateFactoriesFile(packageName: String, injectClasses: List<KSClassDeclaration>): String {
        val sb = StringBuilder()
        sb.appendLine("package $packageName")
        sb.appendLine()
        sb.appendLine("import com.debdut.anchordi.runtime.AnchorProvider")
        sb.appendLine("import com.debdut.anchordi.runtime.Factory")
        sb.appendLine()
        injectClasses.forEach { classDecl ->
            generateFactory(sb, classDecl, internal = true)
        }
        return sb.toString().trimEnd()
    }

    fun generateContributorFile(
        packageName: String,
        baseObjectName: String,
        componentSuffix: String,
        registrationBlocks: List<List<String>>
    ): String {
        val contributorObjectName = "${baseObjectName}_$componentSuffix"
        val sb = StringBuilder()
        sb.appendLine("package $packageName")
        sb.appendLine()
        sb.appendLine("import com.debdut.anchordi.runtime.Binding")
        sb.appendLine("import com.debdut.anchordi.runtime.BindingRegistry")
        sb.appendLine("import com.debdut.anchordi.runtime.ComponentBindingContributor")
        sb.appendLine("import com.debdut.anchordi.runtime.Factory")
        sb.appendLine("import com.debdut.anchordi.runtime.Key")
        sb.appendLine()
        sb.appendLine("internal object $contributorObjectName : ComponentBindingContributor {")
        sb.appendLine("    override fun contribute(registry: BindingRegistry) {")
        registrationBlocks.forEach { block ->
            block.forEach { line ->
                sb.appendLine("        $line")
            }
        }
        sb.appendLine("    }")
        sb.appendLine("}")
        return sb.toString()
    }

    fun generateAggregatorFile(
        packageName: String,
        baseObjectName: String,
        contributorSuffixes: List<String>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("package $packageName")
        sb.appendLine()
        sb.appendLine("import com.debdut.anchordi.runtime.BindingRegistry")
        sb.appendLine("import com.debdut.anchordi.runtime.ComponentBindingContributor")
        sb.appendLine()
        sb.appendLine("object $baseObjectName : ComponentBindingContributor {")
        sb.appendLine("    override fun contribute(registry: BindingRegistry) {")
        contributorSuffixes.forEach { suffix ->
            sb.appendLine("        ${baseObjectName}_$suffix.contribute(registry)")
        }
        sb.appendLine("    }")
        sb.appendLine("}")
        return sb.toString()
    }

    /** Single-file generation (legacy); still supported for tests or small modules. */
    fun generateAnchorContributor(
        packageName: String,
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>,
        objectName: String
    ): String {
        val grouped = buildGroupedRegistrations(injectClasses, moduleClasses)
        val sb = StringBuilder()
        sb.appendLine("package $packageName")
        sb.appendLine()
        sb.appendLine("import com.debdut.anchordi.runtime.AnchorProvider")
        sb.appendLine("import com.debdut.anchordi.runtime.Binding")
        sb.appendLine("import com.debdut.anchordi.runtime.BindingRegistry")
        sb.appendLine("import com.debdut.anchordi.runtime.ComponentBindingContributor")
        sb.appendLine("import com.debdut.anchordi.runtime.Factory")
        sb.appendLine("import com.debdut.anchordi.runtime.Key")
        sb.appendLine()

        injectClasses.forEach { classDecl ->
            generateFactory(sb, classDecl, internal = false)
        }

        sb.appendLine("object $objectName : ComponentBindingContributor {")
        sb.appendLine("    override fun contribute(registry: BindingRegistry) {")
        grouped.values.flatten().forEach { block ->
            block.forEach { line -> sb.appendLine("        $line") }
        }
        sb.appendLine("    }")
        sb.appendLine("}")
        return sb.toString()
    }

    private fun generateFactory(
        sb: StringBuilder,
        classDecl: KSClassDeclaration,
        internal: Boolean = false
    ) {
        val qualifiedName = classDecl.qualifiedName?.asString() ?: return
        val simpleName = classDecl.simpleName.asString()
        val primaryConstructor = classDecl.primaryConstructor ?: return

        val params = primaryConstructor.parameters
        val paramNames = params.map { it.name?.asString() ?: "p" }
        val visibility = if (internal) "internal " else "private "

        sb.appendLine("${visibility}class ${simpleName}_Factory : Factory<$qualifiedName> {")
        sb.appendLine("    override fun create(container: com.debdut.anchordi.runtime.AnchorContainer): $qualifiedName {")
        params.forEachIndexed { i, param ->
            val name = paramNames[i]
            val (resolvedType, isLazy, isProvider) = builder.resolveParameterType(param)
            val qualifier = KspUtils.getAnnotationStringValue(param.findAnnotation(FQN_NAMED))
            when {
                isLazy -> sb.appendLine("        val $name = lazy { container.get<$resolvedType>() }")
                isProvider -> sb.appendLine("        val $name = object : AnchorProvider<$resolvedType> { override fun get() = container.get<$resolvedType>() }")
                else -> {
                    val getCall = if (qualifier != null) "container.get<$resolvedType>(\"$qualifier\")" else "container.get<$resolvedType>()"
                    sb.appendLine("        val $name = $getCall")
                }
            }
        }
        sb.appendLine("        return $qualifiedName(${paramNames.joinToString(", ")})")
        sb.appendLine("    }")
        sb.appendLine("}")
        sb.appendLine()
    }

    private fun buildProvidesRegistrationLines(
        moduleName: String,
        func: KSFunctionDeclaration,
        scopeClassName: String?
    ): List<String> {
        val returnType = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return emptyList()
        val hasIntoSet = func.hasAnnotation(FQN_INTO_SET)
        val hasIntoMap = func.hasAnnotation(FQN_INTO_MAP)
        if (hasIntoSet && hasIntoMap) return emptyList() // Invalid; validator can report

        val paramNames = func.parameters.map { it.name?.asString() ?: "p" }
        val callArgs = paramNames.joinToString(", ")
        val factoryLines = mutableListOf<String>()
        factoryLines.add("object : Factory<Any> {")
        factoryLines.add("            override fun create(container: com.debdut.anchordi.runtime.AnchorContainer): Any {")
        paramNames.forEachIndexed { i, name ->
            val pType = func.parameters[i].type.resolve().declaration.qualifiedName?.asString() ?: "Any"
            val pQualifier = KspUtils.getAnnotationStringValue(func.parameters[i].findAnnotation(FQN_NAMED))
            val getCall = if (pQualifier != null) "container.get<$pType>(\"$pQualifier\")" else "container.get<$pType>()"
            factoryLines.add("                val $name = $getCall")
        }
        factoryLines.add("                return $moduleName.${func.simpleName.asString()}($callArgs)")
        factoryLines.add("            }")
        factoryLines.add("        }")

        return when {
            hasIntoSet -> {
                val setKey = "kotlin.collections.Set<$returnType>"
                mutableListOf<String>().apply {
                    add("registry.registerSetContribution(Key(\"$setKey\", null), ${factoryLines.first()}")
                    addAll(factoryLines.drop(1))
                    add("        })")
                }
            }
            hasIntoMap -> {
                val mapKeyValue = KspUtils.getAnnotationStringValue(func.findAnnotation(FQN_STRING_KEY)) ?: return emptyList()
                val mapKeyType = "kotlin.collections.Map<kotlin.String,$returnType>"
                mutableListOf<String>().apply {
                    add("registry.registerMapContribution(Key(\"$mapKeyType\", null), \"$mapKeyValue\", ${factoryLines.first()}")
                    addAll(factoryLines.drop(1))
                    add("        })")
                }
            }
            else -> {
                val hasViewModelScoped = func.hasAnnotation(FQN_VIEW_MODEL_SCOPED)
                val hasNavigationScoped = func.hasAnnotation(FQN_NAVIGATION_SCOPED)
                val scopedAnnotation = func.findAnnotation(FQN_SCOPED)
                val hasSingleton = func.hasAnnotation(FQN_SINGLETON)
                val (bindingPrefix, bindingSuffix) = when {
                    scopeClassName != null || hasViewModelScoped || hasNavigationScoped -> {
                        val scope = scopeClassName
                            ?: (if (hasViewModelScoped) ValidationConstants.FQN_VIEW_MODEL_COMPONENT else ValidationConstants.FQN_NAVIGATION_COMPONENT)
                        "Binding.Scoped(\"$scope\", " to ")"
                    }
                    scopedAnnotation != null -> {
                        val scopeClass = KspUtils.getScopedClassName(scopedAnnotation) ?: return emptyList()
                        "Binding.Scoped(\"$scopeClass\", " to ")"
                    }
                    hasSingleton -> "Binding.Singleton(" to ")"
                    else -> "Binding.Unscoped(" to ")"
                }
                val qualifier = KspUtils.getAnnotationStringValue(func.findAnnotation(FQN_NAMED))
                val keyQualifier = if (qualifier != null) ", \"$qualifier\"" else ", null"
                mutableListOf<String>().apply {
                    add("registry.register(Key(\"$returnType\"$keyQualifier), ${bindingPrefix}${factoryLines.first()}")
                    addAll(factoryLines.drop(1))
                    add("        )$bindingSuffix")
                }
            }
        }
    }

    private fun buildBindsRegistrationLines(
        func: KSFunctionDeclaration,
        isSingletonComponent: Boolean,
        scopeClassName: String?
    ): List<String> {
        if (func.parameters.size != 1) return emptyList()
        val returnType = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return emptyList()
        val implType = func.parameters.single().type.resolve().declaration.qualifiedName?.asString() ?: return emptyList()
        val hasSingleton = func.hasAnnotation(FQN_SINGLETON)
        val binding = when {
            !isSingletonComponent && scopeClassName != null -> "Binding.Scoped(\"$scopeClassName\", "
            hasSingleton -> "Binding.Singleton("
            else -> "Binding.Unscoped("
        }
        val qualifier = KspUtils.getAnnotationStringValue(func.findAnnotation(FQN_NAMED))
        val keyQualifier = if (qualifier != null) ", \"$qualifier\"" else ", null"
        val implQualifier = KspUtils.getAnnotationStringValue(func.parameters.single().findAnnotation(FQN_NAMED))
        val getCall = if (implQualifier != null) {
            "container.get<$implType>(\"$implQualifier\")"
        } else {
            "container.get<$implType>()"
        }
        return listOf(
            "registry.register(Key(\"$returnType\"$keyQualifier), $binding object : Factory<Any> {",
            "            override fun create(container: com.debdut.anchordi.runtime.AnchorContainer): Any {",
            "                return $getCall",
            "            }",
            "        }))"
        )
    }
}
