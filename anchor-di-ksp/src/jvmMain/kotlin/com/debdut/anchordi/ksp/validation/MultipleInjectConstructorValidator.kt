package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * Validates that each class has at most one constructor annotated with @Inject.
 */
object MultipleInjectConstructorValidator {

    private const val FQN_INJECT = "com.debdut.anchordi.Inject"

    fun validate(injectClasses: List<KSClassDeclaration>, reporter: ValidationReporter) {
        injectClasses.forEach { classDecl ->
            val injectConstructors = getInjectConstructors(classDecl)
            if (injectConstructors.size > 1) {
                val fqn = classDecl.qualifiedName?.asString() ?: "?"
                reporter.error(
                    "[Anchor DI] Multiple @Inject constructors in $fqn. " +
                        "A class may have at most one constructor annotated with @Inject.",
                    classDecl
                )
            }
        }
    }

    private fun getInjectConstructors(classDecl: KSClassDeclaration): List<KSFunctionDeclaration> {
        val primary = classDecl.primaryConstructor?.takeIf { it.hasAnnotation(FQN_INJECT) }
        val secondaries = classDecl.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it != classDecl.primaryConstructor && it.simpleName.asString() == "<init>" && it.hasAnnotation(FQN_INJECT) }
        return listOfNotNull(primary) + secondaries
    }

    private fun KSFunctionDeclaration.hasAnnotation(fqn: String): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }
}
