package com.debdut.anchordi

/**
 * Marks a [Provides] method as contributing one entry to a multibound [Map].
 *
 * The method's return type is the value type; the bound key becomes `Map<K, V>`.
 * You must specify the map key using [StringKey] (or a custom map-key annotation).
 * Multiple modules can contribute to the same map; keys must be unique.
 *
 * Example:
 * ```
 * @IntoMap
 * @StringKey("firebase")
 * @Provides
 * fun provideFirebaseTracker(): Tracker = FirebaseTracker()
 * ```
 *
 * @see IntoSet
 * @see StringKey
 * @see Provides
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class IntoMap
