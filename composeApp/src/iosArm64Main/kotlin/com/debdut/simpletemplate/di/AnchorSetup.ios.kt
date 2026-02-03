package com.debdut.simpletemplate.di

import com.debdut.anchordi.generated.AnchorGenerated_composeapp
import com.debdut.anchordi.runtime.ComponentBindingContributor

/**
 * iOS arm64: returns the KSP-generated contributor for this module.
 *
 * KSP generates AnchorGenerated_composeapp into the iosArm64Main source set,
 * so this actual lives here (not in iosMain).
 */
actual fun getAnchorContributors(): Array<ComponentBindingContributor> = arrayOf(AnchorGenerated_composeapp)
