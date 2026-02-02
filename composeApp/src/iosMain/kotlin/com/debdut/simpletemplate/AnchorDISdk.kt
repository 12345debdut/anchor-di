package com.debdut.simpletemplate

import com.debdut.anchordi.runtime.Anchor
import com.debdut.simpletemplate.di.getAnchorContributors

object AnchorDISdk {
    fun init() {
        Anchor.init(*getAnchorContributors())
    }
}
