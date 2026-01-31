package com.debdut.simpletemplate

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
