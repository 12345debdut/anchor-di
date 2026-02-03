package com.debdut.simpletemplate.logger

import com.debdut.anchordi.Binds
import com.debdut.anchordi.InstallIn
import com.debdut.anchordi.Module
import com.debdut.anchordi.Singleton
import com.debdut.anchordi.SingletonComponent

/**
 * Binds [Logger] to [LoggerImpl] in [SingletonComponent].
 * One shared logger instance for the whole app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {
    @Binds
    @Singleton
    abstract fun bindLogger(impl: LoggerImpl): Logger
}
