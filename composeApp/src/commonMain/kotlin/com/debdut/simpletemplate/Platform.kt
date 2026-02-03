package com.debdut.simpletemplate

/**
 * Platform abstraction for the sample app.
 * Implemented per target (Android, JVM/Desktop, wasmJs, iOS) via [getPlatform].
 */
interface Platform {
    /** Human-readable platform name (e.g. "Android 34", "Desktop JVM (Mac OS X)", "Web (Kotlin/Wasm)"). */
    val name: String
}

/** Returns the platform-specific [Platform] instance. Implemented in androidMain, jvmMain, wasmJsMain, ios*Main. */
expect fun getPlatform(): Platform
