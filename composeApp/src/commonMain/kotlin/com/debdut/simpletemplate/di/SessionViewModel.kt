package com.debdut.simpletemplate.di

import androidx.lifecycle.ViewModel
import com.debdut.anchordi.Inject
import com.debdut.anchordi.compose.AnchorViewModel

/**
 * ViewModel that owns session lifecycle. Exposes [getSessionState] and [logout].
 *
 * When [logout] is called, the current session scope is disposed (a new
 * [SessionComponent] scoped container is created). All objects that were in the
 * session component live until that point; new requests get the new scope.
 *
 * See docs/SESSION_AND_LOGOUT.md in the repo for disposal semantics.
 */
@AnchorViewModel
class SessionViewModel
    @Inject
    constructor() : ViewModel() {
        fun getSessionState(): SessionState = SessionHolder.getSessionState()

        /** Disposes the current session scope and creates a new one. Call when the user logs out. */
        fun logout() {
            SessionHolder.logout()
        }
    }
