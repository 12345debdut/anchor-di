package com.debdut.simpletemplate.di

import com.debdut.anchordi.runtime.ComponentBindingContributor

/**
 * Returns the KSP-generated binding contributors for this module.
 * Implemented via expect/actual so commonMain doesn't need to reference generated code.
 */
expect fun getAnchorContributors(): Array<ComponentBindingContributor>
