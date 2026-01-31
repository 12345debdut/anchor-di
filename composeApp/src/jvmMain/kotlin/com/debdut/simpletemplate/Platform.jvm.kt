package com.debdut.simpletemplate

/** Desktop (JVM) implementation of [Platform]. */
class DesktopPlatform : Platform {
    override val name: String = "Desktop JVM (${System.getProperty("os.name")})"
}

actual fun getPlatform(): Platform = DesktopPlatform()
