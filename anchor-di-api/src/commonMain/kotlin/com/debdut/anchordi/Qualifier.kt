package com.debdut.anchordi

/**
 * Marks an annotation as a qualifier for disambiguating multiple bindings
 * of the same type.
 *
 * Create your own qualifiers:
 * ```
 * @Qualifier
 * @Retention(AnnotationRetention.BINARY)
 * @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
 * annotation class ApiKey(val value: String)
 *
 * @Provides @ApiKey("prod") fun provideProdApi(): Api = ProdApi()
 * @Inject constructor(@ApiKey("prod") api: Api)
 * ```
 *
 * Or use the built-in [Named]:
 * ```
 * @Provides @Named("api") fun provideApi(): Api = ApiImpl()
 * ```
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Qualifier
