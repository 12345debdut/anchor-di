package com.debdut.anchordi.ksp.test

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.symbol.*

// Minimal fakes for KSP interfaces needed by Anchor tests

open class FakeKSNode : KSNode {
    override val location: Location get() = NonExistLocation
    override val origin: Origin get() = Origin.KOTLIN
    override val parent: KSNode? get() = null
    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R = error("Not implemented")
}

class FakeKSName(private val name: String) : KSName {
    override fun asString(): String = name
    override fun getQualifier(): String = ""
    override fun getShortName(): String = name
}

class FakeKSType(private val decl: KSDeclaration, private val args: List<KSTypeArgument> = emptyList()) : KSType {
    override val declaration: KSDeclaration get() = decl
    override val arguments: List<KSTypeArgument> get() = args
    override val annotations: Sequence<KSAnnotation> get() = emptySequence()
    override val isError: Boolean get() = false
    override val isFunctionType: Boolean get() = false
    override val isMarkedNullable: Boolean get() = false
    override val isSuspendFunctionType: Boolean get() = false
    override val nullability: Nullability get() = Nullability.NOT_NULL
    override fun isAssignableFrom(that: KSType): Boolean = false
    override fun isCovarianceFlexible(): Boolean = false
    override fun isMutabilityFlexible(): Boolean = false
    override fun makeNotNullable(): KSType = this
    override fun makeNullable(): KSType = this
    override fun replace(arguments: List<KSTypeArgument>): KSType = this
    override fun starProjection(): KSType = this
}

class FakeKSTypeReference(private val type: KSType) : KSTypeReference, FakeKSNode() {
    override val element: KSReferenceElement? get() = null
    override val annotations: Sequence<KSAnnotation> get() = emptySequence()
    override val modifiers: Set<Modifier> get() = emptySet()
    override fun resolve(): KSType = type
}

class FakeKSAnnotation(
    val fqn: String,
    override val arguments: List<KSValueArgument> = emptyList()
) : KSAnnotation, FakeKSNode() {
    override val annotationType: KSTypeReference = FakeKSTypeReference(
        FakeKSType(FakeKSClassDeclaration(fqn, ""))
    )
    override val shortName: KSName = FakeKSName(fqn.substringAfterLast("."))
    override val useSiteTarget: AnnotationUseSiteTarget? = null
    override val defaultArguments: List<KSValueArgument> = emptyList()
}

class FakeKSValueArgument(
    override val name: KSName?,
    override val value: Any?
) : KSValueArgument, FakeKSNode() {
    override val isSpread: Boolean = false
    override val annotations: Sequence<KSAnnotation> get() = emptySequence()
}

open class FakeKSDeclaration(
    val fqn: String,
    val simple: String
) : KSDeclaration, FakeKSNode() {
    override val qualifiedName: KSName? = FakeKSName(fqn)
    override val simpleName: KSName = FakeKSName(simple)
    override val annotations: Sequence<KSAnnotation> get() = _annotations.asSequence()
    open override val modifiers: Set<Modifier> = emptySet()
    override val containingFile: KSFile? = null
    override val docString: String? = null
    override val packageName: KSName = FakeKSName(fqn.substringBeforeLast("."))
    override val typeParameters: List<KSTypeParameter> = emptyList()
    override val parentDeclaration: KSDeclaration? = null
    override val isActual: Boolean = false
    override val isExpect: Boolean = false
    override fun findActuals(): Sequence<KSDeclaration> = emptySequence()
    override fun findExpects(): Sequence<KSDeclaration> = emptySequence()

    val _annotations = mutableListOf<KSAnnotation>()
    fun addAnnotation(fqn: String, args: List<KSValueArgument> = emptyList()) {
        _annotations.add(FakeKSAnnotation(fqn, args))
    }
}

// Minimal fake for KSBuiltIns (Resolver.builtIns) - matches KSBuiltIns interface
object FakeKSBuiltIns : KSBuiltIns {
    private val anyDecl = FakeKSClassDeclaration("kotlin.Any", "Any")
    override val anyType: KSType get() = FakeKSType(anyDecl)
    override val nothingType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Nothing", "Nothing"))
    override val unitType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Unit", "Unit"))
    override val numberType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Number", "Number"))
    override val byteType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Byte", "Byte"))
    override val shortType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Short", "Short"))
    override val intType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Int", "Int"))
    override val longType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Long", "Long"))
    override val floatType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Float", "Float"))
    override val doubleType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Double", "Double"))
    override val charType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Char", "Char"))
    override val booleanType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Boolean", "Boolean"))
    override val stringType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.String", "String"))
    override val iterableType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.collections.Iterable", "Iterable"))
    override val annotationType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Annotation", "Annotation"))
    override val arrayType: KSType get() = FakeKSType(FakeKSClassDeclaration("kotlin.Array", "Array"))
}

// Minimal fake for KSTypeArgument (Resolver.getTypeArgument) - KSTypeArgument extends KSAnnotated
class FakeKSTypeArgument(private val typeRef: KSTypeReference, private val varianceValue: Variance) : KSTypeArgument, FakeKSNode() {
    override val type: KSTypeReference? get() = typeRef
    override val variance: Variance get() = varianceValue
    override val annotations: Sequence<KSAnnotation> get() = emptySequence()
}

class FakeResolver : com.google.devtools.ksp.processing.Resolver {
    val symbols = mutableMapOf<String, List<KSAnnotated>>()

    override fun getSymbolsWithAnnotation(annotationName: String, inDepth: Boolean): Sequence<KSAnnotated> {
        return symbols[annotationName]?.asSequence() ?: emptySequence()
    }

    override fun getNewFiles(): Sequence<KSFile> = emptySequence()
    override fun getAllFiles(): Sequence<KSFile> = emptySequence()
    override fun getClassDeclarationByName(name: KSName): KSClassDeclaration? = null
    override fun getFunctionDeclarationsByName(name: KSName, includeTopLevel: Boolean): Sequence<KSFunctionDeclaration> = emptySequence()
    override fun getPropertyDeclarationByName(name: KSName, includeTopLevel: Boolean): KSPropertyDeclaration? = null
    override fun getTypeArgument(typeRef: KSTypeReference, variance: Variance): KSTypeArgument =
        FakeKSTypeArgument(typeRef, variance)
    override fun getKSNameFromString(name: String): KSName = FakeKSName(name)
    override fun createKSTypeReferenceFromKSType(type: KSType): KSTypeReference = FakeKSTypeReference(type)
    override val builtIns: KSBuiltIns get() = FakeKSBuiltIns
    @KspExperimental
    override fun mapToJvmSignature(declaration: KSDeclaration): String? = null
    override fun overrides(overrider: KSDeclaration, overridee: KSDeclaration): Boolean = false
    override fun overrides(overrider: KSDeclaration, overridee: KSDeclaration, containingClass: KSClassDeclaration): Boolean = false
    @KspExperimental
    override fun getJvmName(declaration: KSFunctionDeclaration): String? = null
    @KspExperimental
    override fun getJvmName(accessor: KSPropertyAccessor): String? = null
    @KspExperimental
    override fun getOwnerJvmClassName(declaration: KSPropertyDeclaration): String? = null
    @KspExperimental
    override fun getOwnerJvmClassName(declaration: KSFunctionDeclaration): String? = null
    @KspExperimental
    override fun getJvmCheckedException(function: KSFunctionDeclaration): Sequence<KSType> = emptySequence()
    @KspExperimental
    override fun getJvmCheckedException(accessor: KSPropertyAccessor): Sequence<KSType> = emptySequence()
    @KspExperimental
    override fun getDeclarationsFromPackage(packageName: String): Sequence<KSDeclaration> = emptySequence()
    @KspExperimental
    override fun mapJavaNameToKotlin(javaName: KSName): KSName? = null
    @KspExperimental
    override fun mapKotlinNameToJava(kotlinName: KSName): KSName? = null
    @KspExperimental
    override fun getDeclarationsInSourceOrder(container: KSDeclarationContainer): Sequence<KSDeclaration> = emptySequence()
    @KspExperimental
    override fun effectiveJavaModifiers(declaration: KSDeclaration): Set<Modifier> = emptySet()
    @KspExperimental
    override fun getJavaWildcard(reference: KSTypeReference): KSTypeReference = reference
    @KspExperimental
    override fun isJavaRawType(type: KSType): Boolean = false
    @KspExperimental
    override fun getPackageAnnotations(packageName: String): Sequence<KSAnnotation> = emptySequence()
    @KspExperimental
    override fun getPackagesWithAnnotation(annotationName: String): Sequence<String> = emptySequence()
    @KspExperimental
    override fun getModuleName(): KSName = FakeKSName("test")
}

class FakeKSClassDeclaration(
    fqn: String,
    simple: String,
    override val classKind: ClassKind = ClassKind.CLASS
) : FakeKSDeclaration(fqn, simple), KSClassDeclaration {
    override val primaryConstructor: KSFunctionDeclaration? get() = _primaryConstructor
    override val superTypes: Sequence<KSTypeReference> get() = emptySequence()
    override val declarations: Sequence<KSDeclaration> get() = _declarations.asSequence()
    override val modifiers: Set<Modifier> get() = _modifiers

    // Test helpers
    var _primaryConstructor: KSFunctionDeclaration? = null
    val _declarations = mutableListOf<KSDeclaration>()
    val _modifiers = mutableSetOf<Modifier>()

    override fun asStarProjectedType(): KSType = FakeKSType(this)
    override fun asType(typeArguments: List<KSTypeArgument>): KSType = FakeKSType(this)

    override fun getAllFunctions(): Sequence<KSFunctionDeclaration> = _declarations.filterIsInstance<KSFunctionDeclaration>().asSequence()
    override fun getAllProperties(): Sequence<KSPropertyDeclaration> = _declarations.filterIsInstance<KSPropertyDeclaration>().asSequence()
    override fun getSealedSubclasses(): Sequence<KSClassDeclaration> = emptySequence()
    override val isActual: Boolean = false
    override val isCompanionObject: Boolean = false
    override val isExpect: Boolean = false
}

// KSFunction is the return type of KSFunctionDeclaration.asMemberOf(containing)
private class FakeKSFunction(
    override val returnType: KSType?,
    override val parameterTypes: List<KSType?>,
    override val typeParameters: List<KSTypeParameter>,
    override val extensionReceiverType: KSType?,
    override val isError: Boolean = false
) : KSFunction

class FakeKSFunctionDeclaration(
    fqn: String,
    simple: String,
) : FakeKSDeclaration(fqn, simple), KSFunctionDeclaration {
    override val declarations: Sequence<KSDeclaration> = sequenceOf()
    override val functionKind: FunctionKind = FunctionKind.MEMBER
    override val isAbstract: Boolean = false
    override val extensionReceiver: KSTypeReference? = null
    override val returnType: KSTypeReference? get() = _returnType
    override val parameters: List<KSValueParameter> get() = _parameters
    override val modifiers: Set<Modifier> get() = _modifiers
    override fun findOverridee(): KSDeclaration? = null
    override fun asMemberOf(containing: KSType): KSFunction = FakeKSFunction(
        returnType = _returnType?.resolve(),
        parameterTypes = _parameters.map { it.type.resolve() } as List<KSType?>,
        typeParameters = typeParameters,
        extensionReceiverType = extensionReceiver?.resolve(),
        isError = false
    )

    // Test helpers
    var _returnType: KSTypeReference? = null
    val _parameters = mutableListOf<KSValueParameter>()
    val _modifiers = mutableSetOf<Modifier>()
    
    fun addParameter(name: String, typeFqn: String) {
        _parameters.add(FakeKSValueParameter(name, typeFqn))
    }

    fun addParameter(name: String, typeDeclaration: KSDeclaration) {
        _parameters.add(FakeKSValueParameter(name, typeDeclaration.qualifiedName?.asString() ?: "?", typeDeclaration))
    }
}

class FakeKSValueParameter(
    val argName: String,
    typeFqn: String,
    typeDeclaration: KSDeclaration? = null
) : KSValueParameter, FakeKSNode() {
    override val name: KSName = FakeKSName(argName)
    override val type: KSTypeReference = if (typeDeclaration != null) {
        FakeKSTypeReference(FakeKSType(typeDeclaration))
    } else {
        FakeKSTypeReference(FakeKSType(FakeKSClassDeclaration(typeFqn, typeFqn.substringAfterLast("."))))
    }
    override val isVararg: Boolean = false
    override val isNoInline: Boolean = false
    override val isCrossInline: Boolean = false
    override val isVal: Boolean = false
    override val isVar: Boolean = false
    override val hasDefault: Boolean = false
    override val annotations: Sequence<KSAnnotation> get() = _annotations.asSequence()
    
    val _annotations = mutableListOf<KSAnnotation>()
    fun addAnnotation(fqn: String, args: List<KSValueArgument> = emptyList()) {
        _annotations.add(FakeKSAnnotation(fqn, args))
    }
}
