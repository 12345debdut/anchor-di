package com.debdut.simpletemplate.di

import com.debdut.anchordi.generated.AnchorGenerated_composeapp
import com.debdut.anchordi.runtime.ComponentBindingContributor

actual fun getAnchorContributors(): Array<ComponentBindingContributor> =
    arrayOf(AnchorGenerated_composeapp)
