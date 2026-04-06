package com.debdut.anchordi.runtime

import com.debdut.anchordi.InternalAnchorApi
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Multiplatform lock for thread-safe critical sections.
 *
 * Uses [kotlinx.atomicfu.locks.SynchronizedObject] under the hood, which maps to:
 * - **JVM/Android:** `synchronized` (monitor-based locking)
 * - **Kotlin/Native (iOS):** platform mutex (real thread safety under the new memory model)
 * - **JS/WasmJs:** no-op (single-threaded runtime)
 *
 * This ensures correct behavior on all platforms, including Kotlin/Native with the
 * new memory model where coroutines can run on multiple threads.
 *
 * This is an internal implementation detail and should not be used by library consumers.
 */
@InternalAnchorApi
class SyncLock : SynchronizedObject() {
    fun <T> withLock(block: () -> T): T = synchronized(this) { block() }
}
