package com.debdut.anchordi

/**
 * Marks a [Provides] method as contributing one element to a multibound [Set].
 *
 * The method's return type is the element type; the bound key becomes `Set<T>`.
 * Multiple modules can contribute to the same set.
 *
 * Example:
 * ```
 * @IntoSet
 * @Provides
 * fun provideAnalyticsTracker(): Tracker = AnalyticsTracker()
 * ```
 *
 * @see IntoMap
 * @see Provides
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class IntoSet
