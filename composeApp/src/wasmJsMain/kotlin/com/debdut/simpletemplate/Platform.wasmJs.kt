package com.debdut.simpletemplate

/** Web (Kotlin/Wasm) implementation of [Platform]. */
private val platform =
    object : Platform {
        override val name: String
            get() = "Web (Kotlin/Wasm)"
    }

actual fun getPlatform(): Platform = platform
