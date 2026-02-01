package com.debdut.anchordi.runtime

/**
 * iOS: single-threaded by default (main thread). No lock needed; block runs directly.
 * For multi-threaded iOS usage, consider ensuring Anchor/container access from one thread.
 */
actual class SyncLock actual constructor() {
    actual fun <T> withLock(block: () -> T): T = block()
}
