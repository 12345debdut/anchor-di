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
 * @param key Optional key to control cache invalidation. When the key changes, the dependency
 *            is re-resolved. Use when you need a new instance based on some input. Defaults to
 *            `Unit` (never invalidates within the same composition).
 *
 * Example:
 * ```
 * @Composable
 * fun MyScreen(repository: MyRepository = anchorInject()) {
 *     Text(repository.getData())
 * }
 *
 * // With key for cache invalidation:
 * @Composable
 * fun UserScreen(userId: String) {
 *     val userRepo = anchorInject<UserRepository>(key = userId)
 *     // userRepo is re-resolved when userId changes
 * }
 * ```
 */
@Composable
inline fun <reified T : Any> anchorInject(key: Any? = null): T = remember(key) { Anchor.inject<T>() }

/**
 * Resolves and remembers a qualified dependency from Anchor. Use in @Composable functions.
 *
 * The dependency is resolved once per composition and cached for that composition's lifetime.
 * Use with [@Named][com.debdut.anchordi.Named] bindings.
 *
 * @param qualifier The qualifier string (e.g. from @Named("qualifier"))
 * @param key Optional key to control cache invalidation
 *
 * Example:
 * ```
 * @Composable
 * fun MyScreen() {
 *     val apiClient = anchorInject<ApiClient>("production")
 * }
 * ```
 */
@Composable
inline fun <reified T : Any> anchorInject(
    qualifier: String,
    key: Any? = null,
): T = remember(key, qualifier) { Anchor.inject<T>(qualifier) }
