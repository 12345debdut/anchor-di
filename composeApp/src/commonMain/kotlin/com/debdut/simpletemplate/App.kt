package com.debdut.simpletemplate

/**
 * Root Compose UI for the sample app.
 *
 * Uses [viewModelAnchor] to obtain [MainViewModel] with injected dependencies (e.g. [GreetingRepository]).
 * UI state is collected via [collectAsStateWithLifecycle]. The "Click me!" button toggles visibility
 * of the greeting message and image. Shared across Android, Desktop (JVM), and Web (wasmJs).
 */
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import anchor_di.composeapp.generated.resources.Res
import anchor_di.composeapp.generated.resources.compose_multiplatform
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.debdut.anchordi.compose.viewModelAnchor

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel = viewModelAnchor<MainViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { viewModel.toggleContent() }) {
                Text("Click me!")
            }
            AnimatedVisibility(uiState.isContentVisible) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: ${uiState.message}")
                }
            }
        }
    }
}