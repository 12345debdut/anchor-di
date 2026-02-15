package com.debdut.simpletemplate.di

import com.debdut.anchordi.runtime.Anchor
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SessionHolderTest {
    @AfterTest
    fun tearDown() {
        if (Anchor.isInitialized()) {
            Anchor.reset()
        }
    }

    @Test
    fun initFailsWhenAnchorNotInitialized() {
        assertFailsWith<IllegalArgumentException> {
            SessionHolder.init()
        }
    }

    @Test
    fun sessionStateIsScopedAndRecreatedOnLogout() {
        Anchor.init(*getAnchorContributors())
        SessionHolder.init()

        val firstState = SessionHolder.getSessionState()
        val secondState = SessionHolder.getSessionState()
        assertTrue(firstState.sessionId.startsWith("session-"))
        assertSame(firstState, secondState)

        SessionHolder.logout()
        val afterLogout = SessionHolder.getSessionState()
        assertTrue(afterLogout.sessionId.startsWith("session-"))
        assertNotSame(firstState, afterLogout)
    }
}
