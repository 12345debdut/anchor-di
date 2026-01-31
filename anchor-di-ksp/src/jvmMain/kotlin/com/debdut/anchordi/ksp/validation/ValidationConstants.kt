package com.debdut.anchordi.ksp.validation

/**
 * FQN constants used by validators. Align with anchor-di-api and anchor-di-compose.
 */
object ValidationConstants {
    const val FQN_SINGLETON_COMPONENT = "com.debdut.anchordi.SingletonComponent"
    const val FQN_VIEW_MODEL_COMPONENT = "com.debdut.anchordi.ViewModelComponent"
    const val FQN_NAVIGATION_COMPONENT = "com.debdut.anchordi.NavigationComponent"
    const val FQN_SINGLETON = "com.debdut.anchordi.Singleton"
    const val FQN_VIEW_MODEL_SCOPED = "com.debdut.anchordi.ViewModelScoped"
    const val FQN_NAVIGATION_SCOPED = "com.debdut.anchordi.NavigationScoped"
    /** @AnchorViewModel lives in anchor-di-compose; classes with it are bound in ViewModelComponent. */
    const val FQN_ANCHOR_VIEW_MODEL = "com.debdut.anchordi.compose.AnchorViewModel"

    val BUILT_IN_COMPONENTS = setOf(
        FQN_SINGLETON_COMPONENT,
        FQN_VIEW_MODEL_COMPONENT,
        FQN_NAVIGATION_COMPONENT
    )

    val SKIPPED_TYPES = setOf(
        "kotlin.Lazy",
        "com.debdut.anchordi.runtime.AnchorProvider",
        "kotlin.String", "kotlin.Int", "kotlin.Long", "kotlin.Boolean", "kotlin.Float", "kotlin.Double"
    )
}
