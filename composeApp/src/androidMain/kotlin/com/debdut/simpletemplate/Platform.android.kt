package com.debdut.simpletemplate

import android.os.Build

/** Android implementation of [Platform]; name includes SDK version. */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
