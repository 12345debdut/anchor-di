package com.debdut.anchordi.ksp.test

import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSVisitor

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
    override fun isMutuallyAssignable(that: KSType): Boolean = false
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
    override val modifiers: Set<Modifier> = emptySet()
    override val containingFile: KSFile? = null
    override val docString: String? = null
    override val packageName: KSName = FakeKSName(fqn.substringBeforeLast("."))
    override val typeParameters: List<KSTypeParameter> = emptyList()
    override fun findActuals(): Sequence<KSDeclaration> = emptySequence()
    override fun findExpects(): Sequence<KSDeclaration> = emptySequence()

    val _annotations = mutableListOf<KSAnnotation>()
    fun addAnnotation(fqn: String, args: List<KSValueArgument> = emptyList()) {
        _annotations.add(FakeKSAnnotation(fqn, args))
    }
}

class FakeResolver : com.google.devtools.ksp.processing.Resolver {
    val symbols = mutableMapOf<String, List<KSAnnotated>>()

    override fun getSymbolsWithAnnotation(annotationName: String, inDepth: Boolean): Sequence<KSAnnotated> {
        return symbols[annotationName]?.asSequence() ?: emptySequence()
    }

    override fun getClassDeclarationByName(name: KSName): KSClassDeclaration? = null
    override fun getDeclarationsFromPackage(packageName: String): Sequence<KSDeclaration> = emptySequence()
    override fun getDeclarationsInSourceClosure(files: Collection<KSFile>): Sequence<KSDeclaration> = emptySequence()
    override fun getJvmName(declaration: KSFunctionDeclaration): String? = null
    override fun getKSNameFromString(name: String): KSName = FakeKSName(name)
    override fun getNewFiles(): Sequence<KSFile> = emptySequence()
    override fun mapToJvmSignature(declaration: KSDeclaration): String? = null
    override fun overrides(overrider: KSFunctionDeclaration, overridee: KSFunctionDeclaration): Boolean = false
    override fun overrides(overrider: KSPropertyDeclaration, overridee: KSPropertyDeclaration): Boolean = false
    override fun createKSTypeReferenceFromKSType(type: KSType): KSTypeReference = FakeKSTypeReference(type)
    override fun effectiveJavaModifiers(declaration: KSDeclaration): Set<Modifier> = emptySet()
}

class FakeKSClassDeclaration(
    fqn: String,
    simple: String,
    override val classKind: ClassKind = ClassKind.CLASS
) : FakeKSDeclaration(fqn, simple), KSClassDeclaration {
    override val primaryConstructor: KSFunctionDeclaration? get() = _primaryConstructor
    override val superTypes: Sequence<KSTypeReference> get() = emptySequence()
    override val declarations: Sequence<KSDeclaration> get() = _declarations.asSequence()
    
    // Test helpers
    var _primaryConstructor: KSFunctionDeclaration? = null
    val _declarations = mutableListOf<KSDeclaration>()

    override fun asStarProjectedType(): KSType = FakeKSType(this)
    override fun getAllFunctions(): Sequence<KSFunctionDeclaration> = _declarations.filterIsInstance<KSFunctionDeclaration>().asSequence()
    override fun getAllProperties(): Sequence<KSPropertyDeclaration> = _declarations.filterIsInstance<KSPropertyDeclaration>().asSequence()
    override fun getSealedSubclasses(): Sequence<KSClassDeclaration> = emptySequence()
    override val isActual: Boolean = false
    override val isCompanionObject: Boolean = false
    override val isExpect: Boolean = false
}

class FakeKSFunctionDeclaration(
    fqn: String,
    simple: String
) : FakeKSDeclaration(fqn, simple), KSFunctionDeclaration {
    override val functionKind: FunctionKind = FunctionKind.MEMBER
    override val isAbstract: Boolean = false
    override val extensionReceiver: KSTypeReference? = null
    override val returnType: KSTypeReference? get() = _returnType
    override val parameters: List<KSValueParameter> get() = _parameters
    override fun findOverridee(): KSDeclaration? = null
    override fun asMemberOf(containing: KSType): KSFunctionDeclaration = this

    // Test helpers
    var _returnType: KSTypeReference? = null
    val _parameters = mutableListOf<KSValueParameter>()
    
    fun addParameter(name: String, typeFqn: String) {
        _parameters.add(FakeKSValueParameter(name, typeFqn))
    }
}

class FakeKSValueParameter(
    val argName: String,
    val typeFqn: String
) : KSValueParameter, FakeKSNode() {
    override val name: KSName = FakeKSName(argName)
    override val type: KSTypeReference = FakeKSTypeReference(FakeKSType(FakeKSClassDeclaration(typeFqn, typeFqn.substringAfterLast("."))))
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
