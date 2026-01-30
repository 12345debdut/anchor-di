package com.debdut.simpletemplate

import androidx.compose.ui.window.ComposeUIViewController
import com.debdut.anchordi.runtime.Anchor
import com.debdut.simpletemplate.di.getAnchorContributors
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController { App() }
}