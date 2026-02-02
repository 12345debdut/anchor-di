package com.debdut.simpletemplate

import platform.UIKit.UIDevice

/** iOS implementation of [Platform]; name includes system name and version. */
class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
