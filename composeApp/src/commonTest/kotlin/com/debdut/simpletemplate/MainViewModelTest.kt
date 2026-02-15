package com.debdut.simpletemplate

import com.debdut.simpletemplate.repository.GreetingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainViewModelTest {
    @Test
    fun toggleContentFetchesGreetingOnlyWhenShowing() {
        val repository = FakeGreetingRepository(listOf("Hello", "Welcome back"))
        val viewModel = MainViewModel(lazyOf(repository))

        val initialState = viewModel.uiState.value
        assertFalse(initialState.isContentVisible)
        assertEquals("", initialState.message)

        viewModel.toggleContent()
        val visibleState = viewModel.uiState.value
        assertTrue(visibleState.isContentVisible)
        assertEquals("Hello", visibleState.message)
        assertEquals(1, repository.calls)

        viewModel.toggleContent()
        val hiddenState = viewModel.uiState.value
        assertFalse(hiddenState.isContentVisible)
        assertEquals("Hello", hiddenState.message)
        assertEquals(1, repository.calls)

        viewModel.toggleContent()
        val visibleAgain = viewModel.uiState.value
        assertTrue(visibleAgain.isContentVisible)
        assertEquals("Welcome back", visibleAgain.message)
        assertEquals(2, repository.calls)
    }

    private class FakeGreetingRepository(private val messages: List<String>) : GreetingRepository {
        var calls: Int = 0
            private set

        override fun greet(): String {
            val index = calls
            calls += 1
            return messages.getOrNull(index) ?: "Hello"
        }
    }
}
