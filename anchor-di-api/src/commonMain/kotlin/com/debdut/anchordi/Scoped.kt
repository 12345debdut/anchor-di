package com.debdut.anchordi

import kotlin.reflect.KClass

/**
 * Scopes a binding to a custom scope.
 *
 * When applied to a [Provides] method or [Inject] constructor, the binding
 * will be scoped to the given scope class. One instance per scope instance.
 *
 * Resolve scoped types only inside Anchor.withScope(scopeClass) { ... } or via a container
 * returned by Anchor.scopedContainer(scopeClass) (from anchor-di-runtime).
 *
 * @param value The scope class (e.g. a top-level object or class used with [InstallIn])
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Scoped(val value: KClass<*>)
