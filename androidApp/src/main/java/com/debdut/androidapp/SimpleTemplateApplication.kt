package com.debdut.androidapp

import android.app.Application
import com.debdut.anchordi.runtime.Anchor
import com.debdut.simpletemplate.di.getAnchorContributors

class SimpleTemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Anchor.init(*getAnchorContributors())
    }
}
