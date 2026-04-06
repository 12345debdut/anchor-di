package com.debdut.anchordi

/**
 * A built-in qualifier that uses a string to disambiguate bindings.
 *
 * Use on constructor parameters (constructor injection only). Field injection is not supported.
 *
 * Example:
 * ```
 * @Provides
 * @Named("baseUrl")
 * fun provideBaseUrl(): String = "https://api.example.com"
 *
 * class MyApi @Inject constructor(
 *     @Named("baseUrl") private val baseUrl: String
 * )
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Named(val value: String)
