package com.debdut.anchordi

/**
 * Marks a method in a [Module] as a binding provider.
 *
 * The method's return type is the type that gets bound. The method can have
 * parameters, which will be resolved from the container as dependencies.
 *
 * Example:
 * ```
 * @Provides
 * @Singleton
 * fun provideApi(config: Config): Api = ApiImpl(config)
 * ```
 *
 * @see Module
 * @see Binds
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Provides
