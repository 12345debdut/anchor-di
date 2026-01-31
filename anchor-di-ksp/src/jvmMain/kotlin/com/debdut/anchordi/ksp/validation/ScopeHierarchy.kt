package com.debdut.anchordi.ksp.validation

/**
 * Scope hierarchy and lifetime ordering for validation.
 *
 * - **Component hierarchy**: SingletonComponent (root) → ViewModelComponent → NavigationComponent.
 *   Custom components are treated as children of the component they extend or are installed in.
 *
 * - **Scope longevity**: Higher value = longer lived. A longer-lived scope cannot depend on a
 *   shorter-lived scope (e.g. Singleton cannot depend on ViewModel-scoped).
 */
object ScopeHierarchy {

    /** Scope ID for SingletonComponent (application-wide). */
    const val SCOPE_ID_SINGLETON = ValidationConstants.FQN_SINGLETON_COMPONENT

    /** Scope ID for ViewModelComponent (per-ViewModel). */
    const val SCOPE_ID_VIEW_MODEL = ValidationConstants.FQN_VIEW_MODEL_COMPONENT

    /** Scope ID for NavigationComponent (per-destination). */
    const val SCOPE_ID_NAVIGATION = ValidationConstants.FQN_NAVIGATION_COMPONENT

    /**
     * Longevity rank: higher = lives longer. Singleton (2) > ViewModel (1) > Navigation (0).
     * Unscoped bindings use the component's scope for comparison; custom scopes use 1 (between ViewModel and Navigation) if unknown.
     */
    fun longevityRank(componentFqn: String, scopeFqn: String?): Int {
        val effectiveScope = scopeFqn ?: componentFqn
        return when (effectiveScope) {
            ValidationConstants.FQN_SINGLETON_COMPONENT,
            ValidationConstants.FQN_SINGLETON -> 2
            ValidationConstants.FQN_VIEW_MODEL_COMPONENT,
            ValidationConstants.FQN_VIEW_MODEL_SCOPED -> 1
            ValidationConstants.FQN_NAVIGATION_COMPONENT,
            ValidationConstants.FQN_NAVIGATION_SCOPED -> 0
            else -> 1 // custom scope: assume mid-tier for comparison
        }
    }

    /**
     * Returns the scope ID used for a component (for uniqueness in hierarchy).
     * Built-in components use their FQN; custom components use their class FQN.
     */
    fun scopeIdForComponent(componentFqn: String): String = componentFqn

    /**
     * Parent of each built-in component. Custom components have no parent in this map
     * (they are validated via InstallIn; we treat them as siblings or children of the InstallIn target).
     */
    fun parentOf(componentFqn: String): String? = when (componentFqn) {
        ValidationConstants.FQN_VIEW_MODEL_COMPONENT -> ValidationConstants.FQN_SINGLETON_COMPONENT
        ValidationConstants.FQN_NAVIGATION_COMPONENT -> ValidationConstants.FQN_VIEW_MODEL_COMPONENT
        else -> null // Singleton has no parent; custom components not in tree
    }

    /** Returns true if [ancestor] is the same as [component] or an ancestor of [component] in the hierarchy. */
    fun isAncestorOrSelf(ancestor: String, component: String): Boolean {
        var current: String? = component
        while (current != null) {
            if (current == ancestor) return true
            current = parentOf(current)
        }
        return false
    }

    /** Human-readable scope name for error messages. */
    fun scopeDisplayName(scopeFqn: String?): String = when (scopeFqn) {
        null -> "unscoped"
        ValidationConstants.FQN_SINGLETON,
        ValidationConstants.FQN_SINGLETON_COMPONENT -> "Singleton (application-wide)"
        ValidationConstants.FQN_VIEW_MODEL_SCOPED,
        ValidationConstants.FQN_VIEW_MODEL_COMPONENT -> "ViewModel-scoped"
        ValidationConstants.FQN_NAVIGATION_SCOPED,
        ValidationConstants.FQN_NAVIGATION_COMPONENT -> "Navigation-scoped"
        else -> scopeFqn.substringAfterLast('.')
    }
}
