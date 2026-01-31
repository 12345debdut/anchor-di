package com.debdut.anchordi.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate

class AnchorSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String> = emptyMap()
) : SymbolProcessor {

    companion object {
        private const val FQN_INJECT = "com.debdut.anchordi.Inject"
        private const val FQN_MODULE = "com.debdut.anchordi.Module"
        private const val FQN_INSTALL_IN = "com.debdut.anchordi.InstallIn"
        private const val FQN_PROVIDES = "com.debdut.anchordi.Provides"
        private const val FQN_BINDS = "com.debdut.anchordi.Binds"
        private const val FQN_SINGLETON = "com.debdut.anchordi.Singleton"
        private const val FQN_SCOPED = "com.debdut.anchordi.Scoped"
        private const val FQN_VIEW_MODEL_SCOPED = "com.debdut.anchordi.ViewModelScoped"
        private const val FQN_NAMED = "com.debdut.anchordi.Named"
        private const val FQN_SINGLETON_COMPONENT = "com.debdut.anchordi.SingletonComponent"
        private const val FQN_VIEW_MODEL_COMPONENT = "com.debdut.anchordi.ViewModelComponent"
        private const val FQN_NAVIGATION_COMPONENT = "com.debdut.anchordi.NavigationComponent"
        private const val FQN_NAVIGATION_SCOPED = "com.debdut.anchordi.NavigationScoped"
    }

    private var invoked = false

    private val moduleId: String
        get() = options["anchorDiModuleId"]?.takeIf { it.isNotBlank() } ?: ""

    private val generatedObjectName: String
        get() = if (moduleId.isNotEmpty()) "AnchorGenerated_${moduleId.replace("-", "_")}" else "AnchorGenerated"

    override fun process(resolver: com.google.devtools.ksp.processing.Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val injectClasses = mutableListOf<KSClassDeclaration>()
        val moduleClasses = mutableListOf<KSClassDeclaration>()

        resolver.getSymbolsWithAnnotation(FQN_INJECT)
            .filterIsInstance<KSAnnotated>()
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

        validateDependencies(injectClasses.distinct(), moduleClasses, resolver)
        validateModuleBinds(moduleClasses)
        validateNoCycles(injectClasses.distinct(), moduleClasses, resolver)

        val packageName = "com.debdut.anchordi.generated"
        val fileName = generatedObjectName

        val file = codeGenerator.createNewFile(
            com.google.devtools.ksp.processing.Dependencies.ALL_FILES,
            packageName,
            fileName
        )

        file.write(generateAnchorContributor(
            packageName = packageName,
            injectClasses = injectClasses.distinct(),
            moduleClasses = moduleClasses,
            resolver = resolver,
            objectName = generatedObjectName
        ).toByteArray())

        file.close()

        invoked = true

        return emptyList()
    }

    private fun generateAnchorContributor(
        packageName: String,
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>,
        resolver: com.google.devtools.ksp.processing.Resolver,
        objectName: String = "AnchorGenerated"
    ): String {
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
            generateFactory(sb, classDecl, resolver)
        }

        sb.appendLine("object $objectName : ComponentBindingContributor {")
        sb.appendLine("    override fun contribute(registry: BindingRegistry) {")

        injectClasses.forEach { classDecl ->
            val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEach
            val simpleName = classDecl.simpleName.asString()
            val hasViewModelScoped = classDecl.primaryConstructor?.hasAnnotation(FQN_VIEW_MODEL_SCOPED) == true
                || classDecl.hasAnnotation(FQN_VIEW_MODEL_SCOPED)
            val hasNavigationScoped = classDecl.primaryConstructor?.hasAnnotation(FQN_NAVIGATION_SCOPED) == true
                || classDecl.hasAnnotation(FQN_NAVIGATION_SCOPED)
            val scopedAnnotation = classDecl.primaryConstructor?.findAnnotation(FQN_SCOPED)
                ?: classDecl.findAnnotation(FQN_SCOPED)
            val hasSingleton = classDecl.primaryConstructor?.hasAnnotation(FQN_SINGLETON) == true
                || classDecl.hasAnnotation(FQN_SINGLETON)
            val binding = when {
                hasViewModelScoped -> "Binding.Scoped(\"$FQN_VIEW_MODEL_COMPONENT\", ${simpleName}_Factory())"
                hasNavigationScoped -> "Binding.Scoped(\"$FQN_NAVIGATION_COMPONENT\", ${simpleName}_Factory())"
                scopedAnnotation != null -> {
                    val scopeClass = getScopedClassName(scopedAnnotation) ?: return@forEach
                    "Binding.Scoped(\"$scopeClass\", ${simpleName}_Factory())"
                }
                hasSingleton -> "Binding.Singleton(${simpleName}_Factory())"
                else -> "Binding.Unscoped(${simpleName}_Factory())"
            }
            sb.appendLine("        registry.register(Key(\"$qualifiedName\", null), $binding)")
        }

        moduleClasses.forEach { moduleDecl ->
            generateModuleRegistrations(sb, moduleDecl, resolver)
        }

        sb.appendLine("    }")
        sb.appendLine("}")

        return sb.toString()
    }

    private fun generateFactory(
        sb: StringBuilder,
        classDecl: KSClassDeclaration,
        resolver: com.google.devtools.ksp.processing.Resolver
    ) {
        val qualifiedName = classDecl.qualifiedName?.asString() ?: return
        val simpleName = classDecl.simpleName.asString()
        val primaryConstructor = classDecl.primaryConstructor ?: return

        val params = primaryConstructor.parameters
        val paramNames = params.map { it.name?.asString() ?: "p" }

        sb.appendLine("private class ${simpleName}_Factory : Factory<$qualifiedName> {")
        sb.appendLine("    override fun create(container: com.debdut.anchordi.runtime.AnchorContainer): $qualifiedName {")
        params.forEachIndexed { i, param ->
            val name = paramNames[i]
            val (resolvedType, isLazy, isProvider) = resolveParameterType(param)
            val qualifier = getAnnotationStringValue(param.findAnnotation(FQN_NAMED))
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

    private fun resolveParameterType(param: KSValueParameter): Triple<String, Boolean, Boolean> {
        val resolved = param.type.resolve()
        val decl = resolved.declaration
        val qualifiedName = decl.qualifiedName?.asString() ?: return Triple("Any", false, false)
        when (qualifiedName) {
            "kotlin.Lazy" -> {
                val innerType = resolved.arguments.firstOrNull()?.type?.resolve()?.declaration?.qualifiedName?.asString()
                    ?: return Triple("Any", false, false)
                return Triple(innerType, true, false)
            }
            "com.debdut.anchordi.runtime.AnchorProvider" -> {
                val innerType = resolved.arguments.firstOrNull()?.type?.resolve()?.declaration?.qualifiedName?.asString()
                    ?: return Triple("Any", false, false)
                return Triple(innerType, false, true)
            }
        }
        return Triple(qualifiedName, false, false)
    }

    /**
     * Resolves @Scoped(ScopeClass::class) to the scope's qualified name.
     * Prefers symbol resolution (KSTypeReference) so scope ID matches runtime KClass.qualifiedName.
     */
    private fun getScopedClassName(annotation: com.google.devtools.ksp.symbol.KSAnnotation): String? {
        val arg = annotation.arguments.firstOrNull() ?: return null
        val value = arg.value
        if (value is KSTypeReference) {
            val decl = value.resolve().declaration
            if (decl is KSClassDeclaration) return decl.qualifiedName?.asString()
        }
        val str = value?.toString() ?: return null
        val clean = str.replace("class ", "").substringBefore(" ").substringBefore("\n").trim()
        return clean.takeIf { it.isNotBlank() }
    }

    /**
     * Resolves @InstallIn(Component::class) to the component's qualified name via symbol resolution.
     * Use this for custom components so scope ID matches runtime KClass.qualifiedName.
     */
    /**
     * Validates @Binds methods (exactly one parameter) and warns if a @Module has no @Provides/@Binds.
     */
    private fun validateModuleBinds(moduleClasses: List<KSClassDeclaration>) {
        moduleClasses.forEach { moduleDecl ->
            val moduleName = moduleDecl.qualifiedName?.asString() ?: return@forEach
            val functions = moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>()
            var hasProvidesOrBinds = false
            functions.forEach { func ->
                when {
                    func.hasAnnotation(FQN_BINDS) -> {
                        hasProvidesOrBinds = true
                        if (func.parameters.size != 1) {
                            logger.error(
                                "[Anchor DI] @Binds method ${func.simpleName.asString()} in $moduleName must have exactly one parameter (implementation type).",
                                func
                            )
                        }
                    }
                    func.hasAnnotation(FQN_PROVIDES) -> hasProvidesOrBinds = true
                }
            }
            if (!hasProvidesOrBinds) {
                logger.warn("[Anchor DI] Module $moduleName has no @Provides or @Binds methods.", moduleDecl)
            }
        }
    }

    /**
     * Resolves @InstallIn(Component::class) to the component's qualified name via symbol resolution.
     * Use this for custom components so scope ID matches runtime KClass.qualifiedName.
     */
    private fun getComponentScopeIdFromInstallIn(installIn: com.google.devtools.ksp.symbol.KSAnnotation): String? {
        val value = installIn.arguments.firstOrNull()?.value ?: return null
        if (value is KSTypeReference) {
            val decl = value.resolve().declaration
            if (decl is KSClassDeclaration) {
                return decl.qualifiedName?.asString()
            }
        }
        return null
    }

    /**
     * Fallback when symbol resolution returns null (e.g. annotation value not KSTypeReference).
     * Only recognizes built-ins so we don't guess wrong for custom components.
     */
    private fun getComponentScopeIdFromInstallInFallback(installIn: com.google.devtools.ksp.symbol.KSAnnotation): String? {
        val str = installIn.arguments.firstOrNull()?.value?.toString() ?: return null
        return when {
            str.contains("SingletonComponent") -> FQN_SINGLETON_COMPONENT
            str.contains("ViewModelComponent") -> FQN_VIEW_MODEL_COMPONENT
            str.contains("NavigationComponent") -> FQN_NAVIGATION_COMPONENT
            else -> null
        }
    }

    private fun generateModuleRegistrations(
        sb: StringBuilder,
        moduleDecl: KSClassDeclaration,
        resolver: com.google.devtools.ksp.processing.Resolver
    ) {
        val moduleName = moduleDecl.qualifiedName?.asString() ?: return
        val installIn = moduleDecl.findAnnotation(FQN_INSTALL_IN) ?: return
        val componentScopeId = getComponentScopeIdFromInstallIn(installIn)
            ?: getComponentScopeIdFromInstallInFallback(installIn)
            ?: return
        val isSingletonComponent = componentScopeId == FQN_SINGLETON_COMPONENT
        val scopeClassName = when (componentScopeId) {
            FQN_SINGLETON_COMPONENT -> null
            else -> componentScopeId
        }

        moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { func ->
            if (func.hasAnnotation(FQN_BINDS)) {
                generateBindsRegistration(sb, func, isSingletonComponent, scopeClassName)
            } else if (func.hasAnnotation(FQN_PROVIDES)) {
                val returnType = func.returnType?.resolve()?.declaration?.qualifiedName?.asString()
                    ?: return@forEach
                val hasViewModelScoped = func.hasAnnotation(FQN_VIEW_MODEL_SCOPED)
                val hasNavigationScoped = func.hasAnnotation(FQN_NAVIGATION_SCOPED)
                val scopedAnnotation = func.findAnnotation(FQN_SCOPED)
                val hasSingleton = func.hasAnnotation(FQN_SINGLETON)
                val (bindingPrefix, bindingSuffix) = when {
                    scopeClassName != null || hasViewModelScoped || hasNavigationScoped -> {
                        val scope = scopeClassName
                            ?: (if (hasViewModelScoped) FQN_VIEW_MODEL_COMPONENT else FQN_NAVIGATION_COMPONENT)
                        "Binding.Scoped(\"$scope\", " to ")"
                    }
                    scopedAnnotation != null -> {
                        val scopeClass = getScopedClassName(scopedAnnotation) ?: return@forEach
                        "Binding.Scoped(\"$scopeClass\", " to ")"
                    }
                    hasSingleton -> "Binding.Singleton(" to ")"
                    else -> "Binding.Unscoped(" to ")"
                }
                val qualifier = getAnnotationStringValue(func.findAnnotation(FQN_NAMED))

                val paramNames = func.parameters.map { it.name?.asString() ?: "p" }
                val callArgs = paramNames.joinToString(", ")
                val keyQualifier = if (qualifier != null) ", \"$qualifier\"" else ", null"

                sb.appendLine("        registry.register(Key(\"$returnType\"$keyQualifier), ${bindingPrefix}object : Factory<Any> {")
                sb.appendLine("            override fun create(container: com.debdut.anchordi.runtime.AnchorContainer): Any {")
                paramNames.forEachIndexed { i, name ->
                    val pType = func.parameters[i].type.resolve().declaration.qualifiedName?.asString() ?: "Any"
                    val pQualifier = getAnnotationStringValue(func.parameters[i].findAnnotation(FQN_NAMED))
                    val getCall = if (pQualifier != null) "container.get<$pType>(\"$pQualifier\")" else "container.get<$pType>()"
                    sb.appendLine("                val $name = $getCall")
                }
                sb.appendLine("                return $moduleName.${func.simpleName.asString()}($callArgs)")
                sb.appendLine("            }")
                sb.appendLine("        })$bindingSuffix")
                sb.appendLine()
            }
        }
    }

    private fun generateBindsRegistration(
        sb: StringBuilder,
        func: KSFunctionDeclaration,
        isSingletonComponent: Boolean,
        scopeClassName: String?
    ) {
        // @Binds: abstract fun bindApi(impl: ApiImpl): Api
        // Binds return type (Api) to implementation (ApiImpl) - when Api is requested, resolve ApiImpl
        if (func.parameters.size != 1) return
        val returnType = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return
        val implType = func.parameters.single().type.resolve().declaration.qualifiedName?.asString() ?: return
        val hasSingleton = func.hasAnnotation(FQN_SINGLETON)
        val binding = when {
            !isSingletonComponent && scopeClassName != null -> "Binding.Scoped(\"$scopeClassName\", "
            hasSingleton -> "Binding.Singleton("
            else -> "Binding.Unscoped("
        }
        val qualifier = getAnnotationStringValue(func.findAnnotation(FQN_NAMED))
        val keyQualifier = if (qualifier != null) ", \"$qualifier\"" else ", null"

        sb.appendLine("        registry.register(Key(\"$returnType\"$keyQualifier), $binding object : Factory<Any> {")
        sb.appendLine("            override fun create(container: com.debdut.anchordi.runtime.AnchorContainer): Any {")
        val implQualifier = getAnnotationStringValue(func.parameters.single().findAnnotation(FQN_NAMED))
        val getCall = if (implQualifier != null) {
            "container.get<$implType>(\"$implQualifier\")"
        } else {
            "container.get<$implType>()"
        }
        sb.appendLine("                return $getCall")
        sb.appendLine("            }")
        sb.appendLine("        }))")
        sb.appendLine()
    }

    private fun validateDependencies(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>,
        resolver: com.google.devtools.ksp.processing.Resolver
    ) {
        val providedKeys = mutableSetOf<String>()
        injectClasses.forEach { classDecl ->
            classDecl.qualifiedName?.asString()?.let { providedKeys.add(it) }
        }
        moduleClasses.forEach { moduleDecl ->
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { func ->
                when {
                    func.hasAnnotation(FQN_PROVIDES) -> {
                        func.returnType?.resolve()?.declaration?.qualifiedName?.asString()?.let { providedKeys.add(it) }
                    }
                    func.hasAnnotation(FQN_BINDS) -> {
                        func.returnType?.resolve()?.declaration?.qualifiedName?.asString()?.let { providedKeys.add(it) }
                    }
                }
            }
        }

        val requiredTypes = mutableListOf<Pair<String, String>>()
        injectClasses.forEach { classDecl ->
            classDecl.primaryConstructor?.parameters?.forEach { param ->
                val (typeName, _, _) = resolveParameterType(param)
                if (typeName != "Any") {
                    requiredTypes.add(typeName to (classDecl.qualifiedName?.asString() ?: "?"))
                }
            }
        }
        moduleClasses.forEach { moduleDecl ->
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation(FQN_PROVIDES) }
                .forEach { func ->
                    func.parameters.forEach { param ->
                        val (typeName, _, _) = resolveParameterType(param)
                        if (typeName != "Any") {
                            requiredTypes.add(typeName to (func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: "?"))
                        }
                    }
                }
        }

        requiredTypes.forEach { (required, requester) ->
            if (!providedKeys.contains(required) && !isSkippedType(required)) {
                logger.error(
                    "[Anchor DI] Missing binding for $required (required by $requester). " +
                        "Add @Inject constructor, @Provides in a module, or @Binds.",
                    null
                )
            }
        }
    }

    private fun validateNoCycles(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>,
        resolver: com.google.devtools.ksp.processing.Resolver
    ) {
        val graph = mutableMapOf<String, MutableSet<String>>()
        fun addEdge(from: String, to: String) {
            graph.getOrPut(from) { mutableSetOf() }.add(to)
        }
        injectClasses.forEach { classDecl ->
            val from = classDecl.qualifiedName?.asString() ?: return@forEach
            classDecl.primaryConstructor?.parameters?.forEach { param ->
                val (to, _, _) = resolveParameterType(param)
                if (to != "Any" && !isSkippedType(to)) addEdge(from, to)
            }
        }
        moduleClasses.forEach { moduleDecl ->
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { func ->
                when {
                    func.hasAnnotation(FQN_PROVIDES) -> {
                        val from = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return@forEach
                        func.parameters.forEach { param ->
                            val (to, _, _) = resolveParameterType(param)
                            if (to != "Any" && !isSkippedType(to)) addEdge(from, to)
                        }
                    }
                    func.hasAnnotation(FQN_BINDS) -> {
                        if (func.parameters.size != 1) return@forEach
                        val from = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return@forEach
                        val to = func.parameters.single().type.resolve().declaration.qualifiedName?.asString() ?: return@forEach
                        addEdge(from, to)
                    }
                }
            }
        }
        val visited = mutableSetOf<String>()
        val path = mutableSetOf<String>()
        val pathOrder = mutableListOf<String>()
        fun dfs(node: String): Boolean {
            if (path.contains(node)) {
                val cycleStart = pathOrder.indexOf(node)
                val cycle = pathOrder.drop(cycleStart) + node
                val suggestType = cycle.firstOrNull() ?: node
                logger.error(
                    "[Anchor DI] Circular dependency: ${cycle.joinToString(" -> ")}. " +
                        "Break the cycle by using Lazy<$suggestType> or AnchorProvider<$suggestType> for one of the dependencies.",
                    null
                )
                return true
            }
            if (visited.contains(node)) return false
            visited.add(node)
            path.add(node)
            pathOrder.add(node)
            var hasCycle = false
            graph[node]?.forEach { dep ->
                if (dfs(dep)) hasCycle = true
            }
            path.remove(node)
            pathOrder.removeAt(pathOrder.lastIndex)
            return hasCycle
        }
        graph.keys.forEach { if (dfs(it)) return }
    }

    private fun isSkippedType(typeName: String): Boolean = typeName in setOf(
        "kotlin.Lazy", "com.debdut.anchordi.runtime.AnchorProvider",
        "kotlin.String", "kotlin.Int", "kotlin.Long", "kotlin.Boolean", "kotlin.Float", "kotlin.Double"
    )

    private fun KSAnnotated.hasAnnotation(fqn: String): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }

    private fun KSAnnotated.findAnnotation(fqn: String): com.google.devtools.ksp.symbol.KSAnnotation? =
        annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }

    private fun KSFunctionDeclaration.isConstructor(): Boolean =
        parent is KSClassDeclaration && (parent as KSClassDeclaration).primaryConstructor == this

    private fun getAnnotationStringValue(annotation: com.google.devtools.ksp.symbol.KSAnnotation?, argIndex: Int = 0): String? {
        val arg = annotation?.arguments?.getOrNull(argIndex) ?: return null
        val str = arg.value?.toString() ?: return null
        return str.trim('"')
    }
}
