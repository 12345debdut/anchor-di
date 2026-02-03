package com.debdut.simpletemplate

/**
 * UI state for the main screen.
 *
 * @param message Greeting text shown when content is visible.
 * @param isContentVisible Whether the greeting and image are shown (toggled by "Click me!").
 */
data class MainUIState(
    val message: String,
    val isContentVisible: Boolean,
)
