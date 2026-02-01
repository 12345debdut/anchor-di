package com.debdut.anchordi.runtime

/**
 * Multiplatform lock for thread-safe critical sections.
 * JVM/Android: uses [kotlin.synchronized]; iOS/Wasm/JS: no-op (single-threaded by default).
 */
expect class SyncLock() {
    fun <T> withLock(block: () -> T): T
}
