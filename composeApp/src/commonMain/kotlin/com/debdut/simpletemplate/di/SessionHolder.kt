package com.debdut.simpletemplate.di

import com.debdut.anchordi.runtime.Anchor
import com.debdut.anchordi.runtime.AnchorContainer

/**
 * Holds the current [SessionComponent] scoped container. Call [init] after [Anchor.init];
 * call [logout] when the user logs out to create a new session scope.
 *
 * All types bound in [SessionComponent] (e.g. [SessionState]) live until a new component
 * is created — i.e. until [logout] is called. See docs/SESSION_AND_LOGOUT.md in the repo for disposal semantics.
 */
object SessionHolder {
    private lateinit var container: AnchorContainer

    /** Call once after [Anchor.init] (e.g. in [App][com.debdut.simpletemplate.App] or platform entry). */
    fun init() {
        require(Anchor.isInitialized()) { "Anchor must be initialized before SessionHolder.init()." }
        container = Anchor.scopedContainer(SessionComponent::class)
    }

    /** Creates a new session scope; the previous scope's instances become unreachable. Call on logout. */
    fun logout() {
        container = Anchor.scopedContainer(SessionComponent::class)
    }

    fun getSessionState(): SessionState = container.get()
}
