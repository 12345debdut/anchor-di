package com.debdut.anchordi.runtime

/**
 * Wasm/JS: single-threaded. No lock needed; block runs directly.
 */
actual class SyncLock actual constructor() {
    actual fun <T> withLock(block: () -> T): T = block()
}
