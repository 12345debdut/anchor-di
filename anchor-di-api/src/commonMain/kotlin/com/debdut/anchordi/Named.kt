package com.debdut.anchordi

/**
 * A built-in qualifier that uses a string to disambiguate bindings.
 *
 * Example:
 * ```
 * @Provides
 * @Named("baseUrl")
 * fun provideBaseUrl(): String = "https://api.example.com"
 *
 * @Inject
 * @Named("baseUrl")
 * lateinit var baseUrl: String
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Named(val value: String)
