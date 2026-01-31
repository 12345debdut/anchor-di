package com.debdut.anchordi.ksp.analysis

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.BindsMethodDescriptor
import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import com.debdut.anchordi.ksp.model.ModuleDescriptor
import com.debdut.anchordi.ksp.validation.InjectClassDescriptor
import com.debdut.anchordi.ksp.validation.ValidationConstants
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * analyzing KSP symbols and building the Anchor DI model.
 */
class AnchorDiModelBuilder(private val resolver: Resolver) {

    companion object {
        private const val FQN_INJECT = "com.debdut.anchordi.Inject"
        private const val FQN_INSTALL_IN = "com.debdut.anchordi.InstallIn"
        private const val FQN_PROVIDES = "com.debdut.anchordi.Provides"
        private const val FQN_BINDS = "com.debdut.anchordi.Binds"
        private const val FQN_NAMED = "com.debdut.anchordi.Named"
        private const val FQN_SCOPED = "com.debdut.anchordi.Scoped"
        private const val FQN_COMPONENT = "com.debdut.anchordi.Component"
    }

    fun buildComponents(): Map<String, ComponentDescriptor> {
        val components = mutableMapOf<String, ComponentDescriptor>()
        ValidationConstants.BUILT_IN_COMPONENTS.forEach { fqn ->
            components[fqn] = ComponentDescriptor(fqn = fqn, dependencies = emptySet())
        }
        resolver.getSymbolsWithAnnotation(FQN_COMPONENT)
            .filterIsInstance<KSClassDeclaration>()
            .forEach { component ->
                val fqn = component.qualifiedName?.asString() ?: return@forEach
                components[fqn] = ComponentDescriptor(fqn = fqn, dependencies = emptySet())
            }
        return components
    }

    fun buildBindings(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>
    ): List<BindingDescriptor> {
        val bindings = mutableListOf<BindingDescriptor>()

        // Infer component for @Inject classes used only as @Binds impl: map impl FQN -> component from module's @InstallIn
        val implFqnToComponent = buildBindsImplComponentMap(moduleClasses)

        // Bindings from @Inject classes
        injectClasses.forEach { classDecl ->
            val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEach
            val hasViewModelScoped = classDecl.primaryConstructor?.hasAnnotation(ValidationConstants.FQN_VIEW_MODEL_SCOPED) == true
                || classDecl.hasAnnotation(ValidationConstants.FQN_VIEW_MODEL_SCOPED)
            val hasNavigationScoped = classDecl.primaryConstructor?.hasAnnotation(ValidationConstants.FQN_NAVIGATION_SCOPED) == true
                || classDecl.hasAnnotation(ValidationConstants.FQN_NAVIGATION_SCOPED)
            val scopedAnnotation = classDecl.primaryConstructor?.findAnnotation(FQN_SCOPED)
                ?: classDecl.findAnnotation(FQN_SCOPED)
            val hasSingleton = classDecl.primaryConstructor?.hasAnnotation(ValidationConstants.FQN_SINGLETON) == true
                || classDecl.hasAnnotation(ValidationConstants.FQN_SINGLETON)
            val key = qualifiedName
            val (component, scope) = when {
                hasViewModelScoped -> ValidationConstants.FQN_VIEW_MODEL_COMPONENT to ValidationConstants.FQN_VIEW_MODEL_SCOPED
                hasNavigationScoped -> ValidationConstants.FQN_NAVIGATION_COMPONENT to ValidationConstants.FQN_NAVIGATION_SCOPED
                scopedAnnotation != null -> {
                    val scopeClass = getScopedClassName(scopedAnnotation)
                    scopeClass to scopeClass
                }
                hasSingleton -> ValidationConstants.FQN_SINGLETON_COMPONENT to ValidationConstants.FQN_SINGLETON
                else -> {
                    // Unscoped: inherit component from the module that @Binds this type, if exactly one
                    val inheritedComponent = implFqnToComponent[qualifiedName]
                    (inheritedComponent ?: ValidationConstants.FQN_SINGLETON_COMPONENT) to null
                }
            }
            bindings.add(
                BindingDescriptor(
                    key = key,
                    qualifier = null,
                    component = component.orEmpty(),
                    scope = scope,
                    source = qualifiedName
                )
            )
        }

        // Bindings from modules
        moduleClasses.forEach { moduleDecl ->
            val moduleName = moduleDecl.qualifiedName?.asString() ?: return@forEach
            val installIn = moduleDecl.findAnnotation(FQN_INSTALL_IN) ?: return@forEach
            val componentScopeId = getComponentScopeIdFromInstallIn(installIn)
                ?: getComponentScopeIdFromInstallInFallback(installIn)
                ?: return@forEach
            
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { func ->
                if (func.hasAnnotation(FQN_PROVIDES)) {
                    val returnType = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return@forEach
                    val qualifier = getAnnotationStringValue(func.findAnnotation(FQN_NAMED))
                    val hasViewModelScoped = func.hasAnnotation(ValidationConstants.FQN_VIEW_MODEL_SCOPED)
                    val hasNavigationScoped = func.hasAnnotation(ValidationConstants.FQN_NAVIGATION_SCOPED)
                    val scopedAnnotation = func.findAnnotation(FQN_SCOPED)
                    val hasSingleton = func.hasAnnotation(ValidationConstants.FQN_SINGLETON)
                    
                    val (scope, component) = when {
                        hasViewModelScoped -> ValidationConstants.FQN_VIEW_MODEL_SCOPED to ValidationConstants.FQN_VIEW_MODEL_COMPONENT
                        hasNavigationScoped -> ValidationConstants.FQN_NAVIGATION_SCOPED to ValidationConstants.FQN_NAVIGATION_COMPONENT
                        scopedAnnotation != null -> {
                            val scopeClass = getScopedClassName(scopedAnnotation)
                            scopeClass to componentScopeId
                        }
                        hasSingleton -> ValidationConstants.FQN_SINGLETON to ValidationConstants.FQN_SINGLETON_COMPONENT
                        else -> null to componentScopeId
                    }
                    
                    bindings.add(
                        BindingDescriptor(
                            key = returnType,
                            qualifier = qualifier,
                            component = component,
                            scope = scope,
                            source = "$moduleName.${func.simpleName.asString()}"
                        )
                    )
                } else if (func.hasAnnotation(FQN_BINDS)) {
                    if (func.parameters.size != 1) return@forEach
                    val returnType = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return@forEach
                    val qualifier = getAnnotationStringValue(func.findAnnotation(FQN_NAMED))
                    val hasSingleton = func.hasAnnotation(ValidationConstants.FQN_SINGLETON)
                    val hasViewModelScoped = func.hasAnnotation(ValidationConstants.FQN_VIEW_MODEL_SCOPED)
                    val hasNavigationScoped = func.hasAnnotation(ValidationConstants.FQN_NAVIGATION_SCOPED)
                    val scopedAnnotation = func.findAnnotation(FQN_SCOPED)
                    
                    val (scope, component) = when {
                        hasViewModelScoped -> ValidationConstants.FQN_VIEW_MODEL_SCOPED to ValidationConstants.FQN_VIEW_MODEL_COMPONENT
                        hasNavigationScoped -> ValidationConstants.FQN_NAVIGATION_SCOPED to ValidationConstants.FQN_NAVIGATION_COMPONENT
                        scopedAnnotation != null -> {
                            val scopeClass = getScopedClassName(scopedAnnotation)
                            scopeClass to componentScopeId
                        }
                        hasSingleton -> ValidationConstants.FQN_SINGLETON to ValidationConstants.FQN_SINGLETON_COMPONENT
                        else -> null to componentScopeId
                    }
                    
                    bindings.add(
                        BindingDescriptor(
                            key = returnType,
                            qualifier = qualifier,
                            component = component,
                            scope = scope,
                            source = "$moduleName.${func.simpleName.asString()}"
                        )
                    )
                }
            }
        }
        
        return bindings
    }

    /**
     * For each @Inject class that is the implementation type of a @Binds in exactly one
     * non-Singleton module, returns that module's component. Used so unscoped @Inject classes
     * inherit the component from the module that binds them (e.g. GreetingRepositoryImpl
     * in RepositoryModule @InstallIn(ViewModelComponent::class) → ViewModelComponent).
     * If an impl appears in multiple components or only in SingletonComponent, it is omitted
     * (so it defaults to SingletonComponent).
     */
    fun getBindsImplComponentMap(moduleClasses: List<KSClassDeclaration>): Map<String, String> {
        return buildBindsImplComponentMap(moduleClasses)
    }

    /**
     * For each @Inject class that is the implementation type of a @Binds in exactly one module,
     * returns that module's simple name. Used to group generated code by module (put @Inject
     * registration in the same file as the module that binds it).
     */
    fun getBindsImplModuleMap(moduleClasses: List<KSClassDeclaration>): Map<String, String> {
        val implToModules = mutableMapOf<String, MutableSet<String>>()
        moduleClasses.forEach { moduleDecl ->
            val moduleSimpleName = moduleDecl.simpleName.asString()
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation(FQN_BINDS) && it.parameters.size == 1 }
                .forEach { func ->
                    val implFqn = func.parameters.single().type.resolve().declaration.qualifiedName?.asString()
                        ?: return@forEach
                    implToModules.getOrPut(implFqn) { mutableSetOf() }.add(moduleSimpleName)
                }
        }
        return implToModules
            .filter { (_, modules) -> modules.size == 1 }
            .mapValues { (_, modules) -> modules.single() }
    }

    private fun buildBindsImplComponentMap(moduleClasses: List<KSClassDeclaration>): Map<String, String> {
        val implToComponents = mutableMapOf<String, MutableSet<String>>()
        moduleClasses.forEach { moduleDecl ->
            val installIn = moduleDecl.findAnnotation(FQN_INSTALL_IN) ?: return@forEach
            val componentScopeId = getComponentScopeIdFromInstallIn(installIn)
                ?: getComponentScopeIdFromInstallInFallback(installIn)
                ?: return@forEach
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation(FQN_BINDS) && it.parameters.size == 1 }
                .forEach { func ->
                    val implFqn = func.parameters.single().type.resolve().declaration.qualifiedName?.asString()
                        ?: return@forEach
                    implToComponents.getOrPut(implFqn) { mutableSetOf() }.add(componentScopeId)
                }
        }
        return implToComponents
            .filter { (_, components) ->
                components.size == 1 && components.single() != ValidationConstants.FQN_SINGLETON_COMPONENT
            }
            .mapValues { (_, components) -> components.single() }
    }

    fun buildInjectClassDescriptors(injectClasses: List<KSClassDeclaration>): List<InjectClassDescriptor> {
        return injectClasses.map { classDecl ->
            val hasViewModelScoped = classDecl.primaryConstructor?.hasAnnotation(ValidationConstants.FQN_VIEW_MODEL_SCOPED) == true
                || classDecl.hasAnnotation(ValidationConstants.FQN_VIEW_MODEL_SCOPED)
            val component = if (hasViewModelScoped) ValidationConstants.FQN_VIEW_MODEL_COMPONENT else "" // Simple logic, sufficient for this validator
            
            InjectClassDescriptor(
                simpleName = classDecl.simpleName.asString(),
                hasAnchorViewModel = classDecl.hasAnnotation("com.debdut.anchordi.AnchorViewModel"),
                hasViewModelScoped = hasViewModelScoped,
                component = component,
                hasInjectConstructor = classDecl.primaryConstructor?.hasAnnotation(FQN_INJECT) == true
            )
        }
    }

    fun buildModuleDescriptors(moduleClasses: List<KSClassDeclaration>): List<ModuleDescriptor> {
        return moduleClasses.mapNotNull { moduleDecl ->
            val moduleName = moduleDecl.qualifiedName?.asString() ?: return@mapNotNull null
            val installIn = moduleDecl.findAnnotation(FQN_INSTALL_IN)
            val installInFqn = installIn?.let { getComponentScopeIdFromInstallIn(it) ?: getComponentScopeIdFromInstallInFallback(it) }
            val functions = moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>()
            var hasProvidesOrBinds = false
            val bindsMethods = mutableListOf<BindsMethodDescriptor>()
            functions.forEach { func ->
                when {
                    func.hasAnnotation(FQN_BINDS) -> {
                        hasProvidesOrBinds = true
                        bindsMethods.add(
                            BindsMethodDescriptor(
                                moduleName = moduleName,
                                methodName = func.simpleName.asString(),
                                parameterCount = func.parameters.size
                            )
                        )
                    }
                    func.hasAnnotation(FQN_PROVIDES) -> hasProvidesOrBinds = true
                }
            }
            ModuleDescriptor(
                moduleName = moduleName,
                installInComponentFqn = installInFqn,
                hasProvidesOrBinds = hasProvidesOrBinds,
                bindsMethods = bindsMethods
            )
        }
    }

    fun buildProvidedKeysAndRequirements(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>
    ): Pair<Set<String>, List<DependencyRequirement>> {
        val providedKeys = mutableSetOf<String>()
        injectClasses.forEach { classDecl ->
            classDecl.qualifiedName?.asString()?.let { providedKeys.add(it) }
        }
        moduleClasses.forEach { moduleDecl ->
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { func ->
                when {
                    func.hasAnnotation(FQN_PROVIDES) ->
                        func.returnType?.resolve()?.declaration?.qualifiedName?.asString()?.let { providedKeys.add(it) }
                    func.hasAnnotation(FQN_BINDS) ->
                        func.returnType?.resolve()?.declaration?.qualifiedName?.asString()?.let { providedKeys.add(it) }
                }
            }
        }
        val requirements = mutableListOf<DependencyRequirement>()
        injectClasses.forEach { classDecl ->
            classDecl.primaryConstructor?.parameters?.forEach { param ->
                val (typeName, _, _) = resolveParameterType(param)
                if (typeName != "Any") {
                    requirements.add(
                        DependencyRequirement(
                            requiredType = typeName,
                            requester = classDecl.qualifiedName?.asString() ?: "?"
                        )
                    )
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
                            requirements.add(
                                DependencyRequirement(
                                    requiredType = typeName,
                                    requester = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: "?"
                                )
                            )
                        }
                    }
                }
        }
        return providedKeys to requirements
    }

    fun buildDependencyGraph(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>
    ): Map<String, Set<String>> {
        val graph = mutableMapOf<String, MutableSet<String>>()
        fun addEdge(from: String, to: String) {
            graph.getOrPut(from) { mutableSetOf() }.add(to)
        }
        injectClasses.forEach { classDecl ->
            val from = classDecl.qualifiedName?.asString() ?: return@forEach
            classDecl.primaryConstructor?.parameters?.forEach { param ->
                val (to, _, _) = resolveParameterType(param)
                if (to != "Any" && to !in ValidationConstants.SKIPPED_TYPES) addEdge(from, to)
            }
        }
        moduleClasses.forEach { moduleDecl ->
            moduleDecl.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { func ->
                when {
                    func.hasAnnotation(FQN_PROVIDES) -> {
                        val from = func.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: return@forEach
                        func.parameters.forEach { param ->
                            val (to, _, _) = resolveParameterType(param)
                            if (to != "Any" && to !in ValidationConstants.SKIPPED_TYPES) addEdge(from, to)
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
        return graph
    }

    // Helper methods

    fun resolveParameterType(param: KSValueParameter): Triple<String, Boolean, Boolean> {
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

    fun getComponentScopeIdFromInstallIn(installIn: KSAnnotation): String? {
        val value = installIn.arguments.firstOrNull()?.value ?: return null
        return when (value) {
            is KSType -> value.declaration.qualifiedName?.asString()
            is KSClassDeclaration -> value.qualifiedName?.asString()
            else -> null
        }
    }

    fun getComponentScopeIdFromInstallInFallback(installIn: KSAnnotation): String? {
        val str = installIn.arguments.firstOrNull()?.value?.toString() ?: return null
        return when {
            str.contains("SingletonComponent") -> ValidationConstants.FQN_SINGLETON_COMPONENT
            str.contains("ViewModelComponent") -> ValidationConstants.FQN_VIEW_MODEL_COMPONENT
            str.contains("NavigationComponent") -> ValidationConstants.FQN_NAVIGATION_COMPONENT
            else -> null
        }
    }

    fun getAnnotationStringValue(annotation: KSAnnotation?, argIndex: Int = 0): String? {
        val arg = annotation?.arguments?.getOrNull(argIndex) ?: return null
        val str = arg.value?.toString() ?: return null
        return str.trim('"')
    }

    private fun getScopedClassName(annotation: KSAnnotation): String? {
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

    private fun com.google.devtools.ksp.symbol.KSAnnotated.hasAnnotation(fqn: String): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }

    private fun com.google.devtools.ksp.symbol.KSAnnotated.findAnnotation(fqn: String): KSAnnotation? =
        annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }
}
