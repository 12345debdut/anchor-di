package com.debdut.simpletemplate.di

/** Session-scoped state provided by [SessionModule]; one instance per [SessionComponent] scope. */
data class SessionState(val sessionId: String)
