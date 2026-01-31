package com.debdut.simpletemplate

/**
 * Desktop (JVM) entry point for the sample app.
 * Initializes Anchor DI with KSP-generated contributors, then launches a Compose Desktop window
 * with [App] as content. Run with: ./gradlew :composeApp:run
 */
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.debdut.anchordi.runtime.Anchor
import com.debdut.simpletemplate.di.getAnchorContributors

fun main() {
    Anchor.init(*getAnchorContributors())
    application {
        val state = rememberWindowState(size = DpSize(400.dp, 600.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = "Anchor DI - Desktop"
        ) {
            App()
        }
    }
}
