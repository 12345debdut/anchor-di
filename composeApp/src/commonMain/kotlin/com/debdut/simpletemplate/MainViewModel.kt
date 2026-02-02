package com.debdut.simpletemplate

/**
 * Main screen ViewModel with injected [GreetingRepository].
 *
 * Marked with [AnchorViewModel] so it is created inside [ViewModelComponent] scope via [viewModelAnchor].
 * Toggles content visibility and fetches the greeting from the repository when first shown.
 */
import androidx.lifecycle.ViewModel
import com.debdut.anchordi.Inject
import com.debdut.anchordi.compose.AnchorViewModel
import com.debdut.simpletemplate.repository.GreetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.Lazy

@AnchorViewModel
class MainViewModel
    @Inject
    constructor(
        private val greetingRepository: Lazy<GreetingRepository>,
    ) : ViewModel() {
        private val _uiState: MutableStateFlow<MainUIState> = MutableStateFlow(MainUIState("", false))
        val uiState = _uiState.asStateFlow()

        private fun getGreeting(): String = greetingRepository.value.greet()

        fun toggleContent() {
            val isShowing = _uiState.value.isContentVisible
            val message =
                if (!isShowing) {
                    getGreeting()
                } else {
                    _uiState.value.message
                }
            _uiState.update {
                it.copy(
                    isContentVisible = !it.isContentVisible,
                    message = message,
                )
            }
        }
    }
