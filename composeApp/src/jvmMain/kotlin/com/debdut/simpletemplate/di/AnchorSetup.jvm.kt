package com.debdut.simpletemplate.di

import com.debdut.anchordi.generated.AnchorGenerated_composeapp
import com.debdut.anchordi.runtime.ComponentBindingContributor

/** Desktop (JVM): returns the KSP-generated contributor for this module. */
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(AnchorGenerated_composeapp)
