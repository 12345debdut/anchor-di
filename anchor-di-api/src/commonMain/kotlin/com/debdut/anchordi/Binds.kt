package com.debdut.anchordi

/**
 * Marks a method in a [Module] as a binding from an interface/abstract class
 * to its implementation.
 *
 * The method must be abstract (in an abstract module) or have a single parameter
 * of the implementation type and return the interface type. The implementation
 * is typically passed as a parameter and returned.
 *
 * Example (abstract module):
 * ```
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class ApiModule {
 *     @Binds
 *     @Singleton
 *     abstract fun bindApi(impl: ApiImpl): Api
 * }
 * ```
 *
 * @see Module
 * @see Provides
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Binds
