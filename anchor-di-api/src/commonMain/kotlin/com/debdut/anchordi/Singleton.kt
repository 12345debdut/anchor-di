package com.debdut.anchordi

/**
 * Scopes a binding to a single instance per [SingletonComponent].
 *
 * When applied to a [Provides] method or [Inject] constructor, the container
 * will create at most one instance and reuse it for all requests.
 *
 * @see SingletonComponent
 * @see Scoped
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Singleton
