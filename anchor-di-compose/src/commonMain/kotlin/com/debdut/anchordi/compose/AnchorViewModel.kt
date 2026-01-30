package com.debdut.anchordi.compose

import com.debdut.anchordi.Inject

/**
 * Marks a ViewModel class for dependency injection.
 *
 * Use with [viewModelAnchor] in Compose to obtain an instance with
 * injected dependencies. The class must have an [Inject] constructor.
 *
 * Example:
 * ```
 * @AnchorViewModel
 * class MyViewModel @Inject constructor(
 *     private val repository: MyRepository
 * ) : ViewModel() { ... }
 *
 * @Composable
 * fun Screen(viewModel: MyViewModel = viewModelAnchor()) { ... }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class AnchorViewModel
