package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Validates that @Inject is only used on concrete classes: not abstract, interface, object, or enum.
 */
object InjectableClassKindValidator {
    fun validate(
        injectClasses: List<KSClassDeclaration>,
        reporter: ValidationReporter,
    ) {
        injectClasses.forEach { classDecl ->
            val fqn = classDecl.qualifiedName?.asString() ?: "?"
            when (classDecl.classKind) {
                ClassKind.INTERFACE ->
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "$fqn is an interface; @Inject is not allowed on interfaces.",
                            fix = "Use @Binds in a module to bind an implementation.",
                        ),
                        classDecl,
                    )
                ClassKind.OBJECT ->
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "$fqn is an object; @Inject is not allowed on objects.",
                            fix = "Use @Provides in a module or reference the object directly.",
                        ),
                        classDecl,
                    )
                ClassKind.ENUM_CLASS ->
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "$fqn is an enum; @Inject is not allowed on enum classes.",
                            fix = "Use @Provides in a module.",
                        ),
                        classDecl,
                    )
                ClassKind.CLASS -> {
                    if (Modifier.ABSTRACT in classDecl.modifiers) {
                        reporter.error(
                            ValidationMessageFormat.formatError(
                                summary =
                                    "$fqn is abstract; " +
                                        "@Inject is not allowed on abstract classes.",
                                fix = "Use @Binds in a module to bind a concrete implementation.",
                            ),
                            classDecl,
                        )
                    }
                }
                else ->
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary =
                                "$fqn has unsupported class kind (${classDecl.classKind}); " +
                                    "@Inject is only allowed on concrete classes.",
                        ),
                        classDecl,
                    )
            }
        }
    }
}
