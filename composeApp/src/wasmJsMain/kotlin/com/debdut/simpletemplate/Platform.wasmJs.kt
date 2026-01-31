package com.debdut.simpletemplate

private val platform = object : Platform {
    override val name: String
        get() = "Web (Kotlin/Wasm)"
}

actual fun getPlatform(): Platform = platform
