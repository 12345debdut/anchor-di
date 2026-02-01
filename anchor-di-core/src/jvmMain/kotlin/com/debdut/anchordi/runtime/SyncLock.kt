package com.debdut.anchordi.runtime

import kotlin.synchronized

actual class SyncLock actual constructor() {
    private val lock = Any()

    actual fun <T> withLock(block: () -> T): T = synchronized(lock, block)
}
