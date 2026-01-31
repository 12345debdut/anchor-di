package com.debdut.anchordi

import kotlin.reflect.KClass

/**
 * Specifies which component a [Module] is installed in.
 *
 * The component determines the scope and lifetime of bindings from this module.
 *
 * Built-in components:
 * - [SingletonComponent]: application-wide singleton or unscoped
 * - [ViewModelComponent]: one instance per ViewModel (use with `viewModelAnchor()`)
 * - [NavigationComponent]: one instance per navigation destination (use anchor-di-navigation: NavigationScopedContent and navigationScopedInject())
 *
 * Custom components: use any top-level `object` or class as the component. Bindings are scoped
 * to that component. Enter the scope with `Anchor.withScope(MyScope::class) { ... }` or
 * `Anchor.scopedContainer(MyScope::class)` to hold and manage the scope yourself.
 *
 * @param value The component class (e.g. [SingletonComponent], [ViewModelComponent], or your custom scope)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class InstallIn(val value: KClass<*>)
