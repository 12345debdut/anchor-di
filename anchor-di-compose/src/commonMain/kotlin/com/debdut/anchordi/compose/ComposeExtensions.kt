package com.debdut.anchordi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.debdut.anchordi.runtime.Anchor

/**
 * Resolves and remembers a dependency from Anchor. Use in @Composable functions.
 *
 * The dependency is resolved once per composition and cached for that composition's lifetime
 * (same composition = same instance). Call [Anchor.init][com.debdut.anchordi.runtime.Anchor.init]
 * before the first composable that uses this.
 *
 * Works in commonMain for Compose Multiplatform (Android, iOS, Desktop).
 *
 * Example:
 * ```
 * @Composable
 * fun MyScreen(repository: MyRepository = anchorInject()) {
 *     Text(repository.getData())
 * }
 * ```
 */
@Composable
inline fun <reified T : Any> anchorInject(): T = remember { Anchor.inject<T>() }
