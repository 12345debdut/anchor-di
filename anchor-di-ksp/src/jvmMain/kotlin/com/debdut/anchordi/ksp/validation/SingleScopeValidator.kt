package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.hasAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * Validates that each binding has at most one scope annotation.
 * A type or method must not be annotated with more than one of: @Singleton, @ViewModelScoped, @NavigationScoped, @Scoped(...).
 */
object SingleScopeValidator {
    private val SCOPE_ANNOTATIONS =
        setOf(
            "com.debdut.anchordi.Singleton",
            "com.debdut.anchordi.ViewModelScoped",
            "com.debdut.anchordi.NavigationScoped",
            "com.debdut.anchordi.Scoped",
        )

    fun validate(
        injectClasses: List<KSClassDeclaration>,
        moduleClasses: List<KSClassDeclaration>,
        reporter: ValidationReporter,
    ) {
        injectClasses.forEach { classDecl ->
            val scopeAnnotations = countScopeAnnotations(classDecl)
            if (scopeAnnotations > 1) {
                val fqn = classDecl.qualifiedName?.asString() ?: "?"
                reporter.error(
                    ValidationMessageFormat.formatError(
                        summary = "Multiple scope annotations on $fqn.",
                        detail =
                            "A binding may have only one scope: @Singleton, @ViewModelScoped, " +
                                "@NavigationScoped, or @Scoped(YourScope::class).",
                        fix = "Remove the extra scope annotation(s) so the binding has a single, clear lifetime.",
                    ),
                    classDecl,
                )
            }
        }
        moduleClasses.forEach { moduleDecl ->
            val moduleName = moduleDecl.qualifiedName?.asString() ?: "?"
            moduleDecl.declarations
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation("com.debdut.anchordi.Provides") || it.hasAnnotation("com.debdut.anchordi.Binds") }
                .forEach { func ->
                    val scopeAnnotations = countScopeAnnotations(func)
                    if (scopeAnnotations > 1) {
                        val member = "$moduleName.${func.simpleName.asString()}"
                        reporter.error(
                            ValidationMessageFormat.formatError(
                                summary = "Multiple scope annotations on $member.",
                                detail =
                                    "A @Provides or @Binds method may have only one scope: " +
                                        "@Singleton, @ViewModelScoped, @NavigationScoped, or @Scoped(YourScope::class).",
                                fix = "Remove the extra scope annotation(s) so the binding has a single, clear lifetime.",
                            ),
                            func,
                        )
                    }
                }
        }
    }

    private fun countScopeAnnotations(annotated: com.google.devtools.ksp.symbol.KSAnnotated): Int =
        SCOPE_ANNOTATIONS.count { fqn ->
            annotated.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn
            }
        }
}
