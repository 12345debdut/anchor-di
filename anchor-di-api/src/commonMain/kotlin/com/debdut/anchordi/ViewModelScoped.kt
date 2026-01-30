package com.debdut.anchordi

/**
 * Scopes a binding to [ViewModelComponent]: one instance per ViewModel.
 *
 * When applied to a [Provides] method or [Inject] constructor, the binding
 * will be scoped to the ViewModel that requests it. The instance persists
 * for the lifetime of that ViewModel (until it is cleared).
 *
 * Only ViewModels obtained via `viewModelAnchor()` run inside the ViewModel scope;
 * other call sites will get a runtime error if they request a ViewModel-scoped type without that scope.
 *
 * Example:
 * ```
 * @ViewModelScoped
 * @Inject constructor()
 * class ScreenState { ... }
 *
 * @AnchorViewModel
 * class MyViewModel @Inject constructor(
 *     private val screenState: ScreenState  // same instance for this ViewModel
 * ) : ViewModel()
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ViewModelScoped
