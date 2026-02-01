package com.debdut.simpletemplate.product.di

import com.debdut.anchordi.Binds
import com.debdut.anchordi.InstallIn
import com.debdut.anchordi.Module
import com.debdut.anchordi.Provides
import com.debdut.anchordi.Singleton
import com.debdut.anchordi.SingletonComponent
import com.debdut.anchordi.ViewModelComponent
import com.debdut.anchordi.ViewModelScoped
import com.debdut.simpletemplate.product.data.ProductApi
import com.debdut.simpletemplate.product.data.ProductApiImpl
import com.debdut.simpletemplate.product.data.createHttpClient
import io.ktor.client.HttpClient

/**
 * Provides [HttpClient] and [ProductApi] in [SingletonComponent].
 * One shared HTTP client and API for the app; used by [ProductRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
object ProductApiModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = createHttpClient()
}

/**
 * Binds [ProductApi] to [ProductApiImpl] in [SingletonComponent].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProductApiBindsModule {

    @Binds
    @Singleton
    abstract fun bindProductApi(impl: ProductApiImpl): ProductApi
}
