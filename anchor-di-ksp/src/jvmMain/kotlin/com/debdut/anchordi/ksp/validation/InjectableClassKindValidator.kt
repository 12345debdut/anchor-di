package com.debdut.anchordi.ksp.validation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Validates that @Inject is only used on concrete classes: not abstract, interface, object, or enum.
 */
object InjectableClassKindValidator {

    fun validate(injectClasses: List<KSClassDeclaration>, reporter: ValidationReporter) {
        injectClasses.forEach { classDecl ->
            val fqn = classDecl.qualifiedName?.asString() ?: "?"
            when (classDecl.classKind) {
                ClassKind.INTERFACE -> reporter.error(
                    "[Anchor DI] $fqn is an interface. @Inject is not allowed on interfaces. Use @Binds in a module to bind an implementation.",
                    classDecl
                )
                ClassKind.OBJECT -> reporter.error(
                    "[Anchor DI] $fqn is an object. @Inject is not allowed on objects. Use @Provides in a module or reference the object directly.",
                    classDecl
                )
                ClassKind.ENUM_CLASS -> reporter.error(
                    "[Anchor DI] $fqn is an enum. @Inject is not allowed on enum classes. Use @Provides in a module.",
                    classDecl
                )
                ClassKind.CLASS -> {
                    if (Modifier.ABSTRACT in classDecl.modifiers) {
                        reporter.error(
                            "[Anchor DI] $fqn is abstract. @Inject is not allowed on abstract classes. Use @Binds in a module to bind a concrete implementation.",
                            classDecl
                        )
                    }
                }
                else -> reporter.error(
                    "[Anchor DI] $fqn has unsupported class kind (${classDecl.classKind}). @Inject is only allowed on concrete classes.",
                    classDecl
                )
            }
        }
    }
}
