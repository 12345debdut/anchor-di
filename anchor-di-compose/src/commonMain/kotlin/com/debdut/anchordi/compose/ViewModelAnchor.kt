package com.debdut.anchordi.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.debdut.anchordi.ViewModelComponent
import com.debdut.anchordi.runtime.Anchor

/**
 * Returns an [AnchorViewModel]-annotated ViewModel with injected dependencies.
 *
 * Requires a [ViewModelStoreOwner] in composition scope (e.g. Compose UI under an Activity,
 * Fragment, or NavBackStackEntry). Uses the KMP lifecycle ViewModel API. Works in commonMain
 * for Compose Multiplatform (Android, iOS, Desktop). The ViewModel is scoped to the current
 * [ViewModelStoreOwner].
 *
 * ViewModel creation runs inside [ViewModelComponent] scope, so the ViewModel can inject
 * [ViewModelComponent]-scoped dependencies (e.g. [ViewModelScoped] or modules with
 * [InstallIn][com.debdut.anchordi.InstallIn](ViewModelComponent::class)). Those
 * instances persist for the lifetime of this ViewModel and are cleared when the ViewModel is cleared.
 *
 * Example:
 * ```
 * @AnchorViewModel
 * class MyViewModel @Inject constructor(repo: Repo) : ViewModel()
 *
 * @Composable
 * fun Screen(viewModel: MyViewModel = viewModelAnchor()) { ... }
 * ```
 */
@Composable
inline fun <reified T : ViewModel> viewModelAnchor(): T =
    viewModel { Anchor.withScope(ViewModelComponent.SCOPE_ID) { it.get<T>() } }
