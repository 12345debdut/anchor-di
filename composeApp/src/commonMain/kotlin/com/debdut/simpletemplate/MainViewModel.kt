package com.debdut.simpletemplate

import androidx.lifecycle.ViewModel
import com.debdut.anchordi.Inject
import com.debdut.anchordi.compose.AnchorViewModel
import com.debdut.simpletemplate.repository.GreetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@AnchorViewModel
class MainViewModel @Inject constructor(
    private val greetingRepository: GreetingRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUIState> = MutableStateFlow(MainUIState("", false))
    val uiState = _uiState.asStateFlow()
    private fun getGreeting(): String = greetingRepository.greet()

    fun toggleContent() {
        val isShowing = _uiState.value.isContentVisible
        val message = if (!isShowing) {
            getGreeting()
        } else {
            _uiState.value.message
        }
        _uiState.update {
            it.copy(
                isContentVisible = !it.isContentVisible,
                message = message
            )
        }
    }
}
