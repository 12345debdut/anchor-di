package com.debdut.anchordi

/**
 * Marks a constructor as eligible for dependency injection.
 *
 * The class will be instantiated by the DI container using its dependencies.
 * All constructor parameters must be resolvable by the container.
 *
 * Only constructor injection is supported; field and method injection are not.
 *
 * @see Module
 * @see Provides
 * @see Binds
 */
@Target(AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Inject
