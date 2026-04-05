package com.debdut.anchordi

/**
 * Marks an Anchor DI API as experimental.
 *
 * Experimental APIs may change in future releases without following semantic versioning
 * guarantees. Use with caution in production code.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This Anchor DI API is experimental. It may change or be removed in future releases.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.ANNOTATION_CLASS,
)
annotation class ExperimentalAnchorApi

/**
 * Marks an Anchor DI API as internal to the library.
 *
 * Internal APIs are not intended for external use and may change at any time.
 * Using them outside the Anchor DI library modules is unsupported.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal Anchor DI API. It is not intended for external use.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
annotation class InternalAnchorApi
