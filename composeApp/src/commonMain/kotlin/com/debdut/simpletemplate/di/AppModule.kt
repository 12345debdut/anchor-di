package com.debdut.simpletemplate.di

import com.debdut.anchordi.Binds
import com.debdut.anchordi.InstallIn
import com.debdut.anchordi.Module
import com.debdut.anchordi.Provides
import com.debdut.anchordi.Singleton
import com.debdut.anchordi.SingletonComponent
import com.debdut.anchordi.ViewModelComponent
import com.debdut.simpletemplate.Platform
import com.debdut.simpletemplate.getPlatform
import com.debdut.simpletemplate.repository.GreetingRepository
import com.debdut.simpletemplate.repository.GreetingRepositoryImpl

/**
 * App-wide singletons (e.g. platform, config).
 * Installed in [SingletonComponent]; bindings are created once and shared.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePlatform(): Platform = getPlatform()
}

/**
 * ViewModel-scoped bindings: one instance per ViewModel.
 * [GreetingRepository] is created when a ViewModel that needs it is created (e.g. [MainViewModel]).
 */
@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {
    @Binds
    fun bindGreetingRepository(repository: GreetingRepositoryImpl): GreetingRepository
}
