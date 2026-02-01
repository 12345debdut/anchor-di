package com.debdut.anchordi

/**
 * Specifies the string key for a map entry when using [IntoMap] multibinding.
 *
 * Use on a [Provides] method together with [IntoMap]:
 * ```
 * @IntoMap
 * @StringKey("firebase")
 * @Provides
 * fun provideFirebaseTracker(): Tracker = FirebaseTracker()
 * ```
 *
 * @see IntoMap
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class StringKey(val value: String)
