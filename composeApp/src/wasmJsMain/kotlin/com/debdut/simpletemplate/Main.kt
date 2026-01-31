package com.debdut.simpletemplate

/**
 * Web (Kotlin/Wasm) entry point for the sample app.
 * Initializes Anchor DI, then renders [App] into [document.body] via [ComposeViewport].
 * Run with: ./gradlew :composeApp:wasmJsBrowserProductionRun, then open http://localhost:8080/
 */
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.debdut.anchordi.runtime.Anchor
import com.debdut.simpletemplate.di.getAnchorContributors
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Anchor.init(*getAnchorContributors())
    ComposeViewport(viewportContainer = document.body!!) {
        App()
    }
}
