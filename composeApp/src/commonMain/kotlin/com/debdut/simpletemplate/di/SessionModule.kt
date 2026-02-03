package com.debdut.simpletemplate.di

import com.debdut.anchordi.InstallIn
import com.debdut.anchordi.Module
import com.debdut.anchordi.Provides
import kotlin.random.Random

@Module
@InstallIn(SessionComponent::class)
object SessionModule {
    @Provides
    fun provideSessionState(): SessionState = SessionState(sessionId = "session-${Random.nextLong().let { if (it < 0) -it else it }}")
}
