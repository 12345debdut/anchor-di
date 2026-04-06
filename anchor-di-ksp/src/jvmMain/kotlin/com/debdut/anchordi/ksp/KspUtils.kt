package com.debdut.anchordi.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference

/**
 * Shared KSP utility functions for annotation processing.
 */
object KspUtils {
    /**
     * Extracts the scope class name from a @Scoped annotation.
     *
     * Handles both KSTypeReference (preferred) and fallback string parsing.
     *
     * @param annotation The @Scoped annotation
     * @return The fully qualified class name of the scope, or null if not found
     */
    fun getScopedClassName(annotation: KSAnnotation): String? {
        val arg = annotation.arguments.firstOrNull() ?: return null
        val value = arg.value
        if (value is KSTypeReference) {
            val decl = value.resolve().declaration
            if (decl is KSClassDeclaration) return decl.qualifiedName?.asString()
        }
        // Fallback: string parsing (for edge cases where KSTypeReference isn't available)
        val str = value?.toString() ?: return null
        val clean = str.replace("class ", "").substringBefore(" ").substringBefore("\n").trim()
        return clean.takeIf { it.isNotBlank() }
    }

    /**
     * Extracts a string value from an annotation argument.
     *
     * @param annotation The annotation to extract from
     * @param argIndex The index of the argument (default: 0)
     * @return The string value, trimmed of quotes, or null if not found
     */
    fun getAnnotationStringValue(
        annotation: KSAnnotation?,
        argIndex: Int = 0,
    ): String? {
        val arg = annotation?.arguments?.getOrNull(argIndex) ?: return null
        val str = arg.value?.toString() ?: return null
        return str.trim('"')
    }
}

/**
 * Checks if this annotated element has an annotation with the given fully qualified name.
 */
fun KSAnnotated.hasAnnotation(fqn: String): Boolean =
    annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }

/**
 * Finds an annotation with the given fully qualified name on this annotated element.
 */
fun KSAnnotated.findAnnotation(fqn: String): KSAnnotation? =
    annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn }

/**
 * Resolves the qualifier string for an annotated element.
 *
 * Checks in order:
 * 1. `@Named("value")` — returns the string value
 * 2. Any annotation that is itself annotated with `@Qualifier` — returns
 *    `"AnnotationFqn(value)"` if it has a `value` parameter, or `"AnnotationFqn"` for markers.
 *
 * @return The qualifier string, or null if no qualifier annotation is present.
 */
fun KSAnnotated.resolveQualifier(): String? {
    // 1. Check for @Named first (most common)
    val named = findAnnotation("com.debdut.anchordi.Named")
    if (named != null) {
        return KspUtils.getAnnotationStringValue(named)
    }

    // 2. Check for any custom @Qualifier-annotated annotation
    for (annotation in annotations) {
        val annotationDecl = annotation.annotationType.resolve().declaration
        if (annotationDecl is KSClassDeclaration &&
            annotationDecl.hasAnnotation("com.debdut.anchordi.Qualifier")
        ) {
            val fqn = annotationDecl.qualifiedName?.asString() ?: continue
            // If the annotation has a "value" argument, include it
            val valueArg = annotation.arguments.firstOrNull { it.name?.asString() == "value" }
            return if (valueArg?.value != null) {
                "$fqn(${valueArg.value.toString().trim('"')})"
            } else {
                fqn
            }
        }
    }

    return null
}
