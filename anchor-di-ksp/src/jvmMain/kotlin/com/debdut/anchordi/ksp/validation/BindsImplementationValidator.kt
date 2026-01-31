package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Validates that @Binds implementation type is a concrete class (not interface, abstract, object, or enum).
 */
object BindsImplementationValidator {

    private const val FQN_BINDS = "com.debdut.anchordi.Binds"

    fun validate(moduleClasses: List<KSClassDeclaration>, reporter: ValidationReporter) {
        moduleClasses.forEach { moduleDecl ->
            val moduleName = moduleDecl.qualifiedName?.asString() ?: "?"
            moduleDecl.declarations
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation(FQN_BINDS) && it.parameters.size == 1 }
                .forEach { func ->
                    val implType = func.parameters.single().type.resolve().declaration
                    if (implType is KSClassDeclaration) {
                        val implFqn = implType.qualifiedName?.asString() ?: "?"
                        when (implType.classKind) {
                            ClassKind.INTERFACE -> reporter.error(
                                "[Anchor DI] @Binds in $moduleName binds to interface $implFqn. " +
                                    "Implementation type must be a concrete class, not an interface.",
                                func
                            )
                            ClassKind.OBJECT -> reporter.error(
                                "[Anchor DI] @Binds in $moduleName binds to object $implFqn. " +
                                    "Implementation type must be a concrete class, not an object.",
                                func
                            )
                            ClassKind.ENUM_CLASS -> reporter.error(
                                "[Anchor DI] @Binds in $moduleName binds to enum $implFqn. " +
                                    "Implementation type must be a concrete class, not an enum.",
                                func
                            )
                            ClassKind.CLASS -> {
                                if (Modifier.ABSTRACT in implType.modifiers) {
                                    reporter.error(
                                        "[Anchor DI] @Binds in $moduleName binds to abstract class $implFqn. " +
                                            "Implementation type must be a concrete class, not abstract.",
                                        func
                                    )
                                }
                            }
                            else -> reporter.error(
                                "[Anchor DI] @Binds in $moduleName binds to unsupported type $implFqn (${implType.classKind}). " +
                                    "Implementation type must be a concrete class.",
                                func
                            )
                        }
                    }
                }
        }
    }

    private fun KSFunctionDeclaration.hasAnnotation(fqn: String): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }
}
