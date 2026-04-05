package com.debdut.anchordi

/**
 * Marks an annotation as a qualifier for disambiguating multiple bindings
 * of the same type.
 *
 * **Note:** Custom qualifiers created with `@Qualifier` are experimental.
 * Currently, only the built-in [Named] qualifier is fully supported by the
 * KSP processor. Custom qualifier support is planned for a future release.
 *
 * For now, use the built-in [Named]:
 * ```
 * @Provides @Named("api") fun provideApi(): Api = ApiImpl()
 * ```
 */
@ExperimentalAnchorApi
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Qualifier
