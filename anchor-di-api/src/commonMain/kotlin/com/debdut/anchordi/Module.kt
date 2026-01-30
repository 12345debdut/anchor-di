package com.debdut.anchordi

/**
 * Marks a class or object as a DI module that contributes bindings to a component.
 *
 * Modules contain [Provides] or [Binds] methods. Use [InstallIn] to specify
 * which component(s) the module is installed in.
 *
 * Example:
 * ```
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object AppModule {
 *     @Provides
 *     @Singleton
 *     fun provideApi(): Api = ApiImpl()
 * }
 * ```
 *
 * @see InstallIn
 * @see Provides
 * @see Binds
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Module
