package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Validates that the @Inject constructor is public (not private or protected).
 */
object ConstructorAccessibilityValidator {

    private const val FQN_INJECT = "com.debdut.anchordi.Inject"

    fun validate(injectClasses: List<KSClassDeclaration>, reporter: ValidationReporter) {
        injectClasses.forEach { classDecl ->
            val constructor = getInjectConstructor(classDecl) ?: return@forEach
            val modifiers = constructor.modifiers
            when {
                Modifier.PRIVATE in modifiers -> {
                    val fqn = classDecl.qualifiedName?.asString() ?: "?"
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "@Inject constructor of $fqn must be public, not private.",
                            fix = "Make the constructor public so the DI container can instantiate it."
                        ),
                        classDecl
                    )
                }
                Modifier.PROTECTED in modifiers -> {
                    val fqn = classDecl.qualifiedName?.asString() ?: "?"
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "@Inject constructor of $fqn must be public, not protected.",
                            fix = "Make the constructor public so the DI container can instantiate it."
                        ),
                        classDecl
                    )
                }
            }
        }
    }

    private fun getInjectConstructor(classDecl: KSClassDeclaration): KSFunctionDeclaration? {
        classDecl.primaryConstructor?.takeIf { it.hasAnnotation(FQN_INJECT) }?.let { return it }
        return classDecl.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it != classDecl.primaryConstructor && it.simpleName.asString() == "<init>" }
            .firstOrNull { it.hasAnnotation(FQN_INJECT) }
    }

    private fun KSFunctionDeclaration.hasAnnotation(fqn: String): Boolean =
        annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }
}
