package com.debdut.simpletemplate.product.di

import com.debdut.anchordi.Binds
import com.debdut.anchordi.InstallIn
import com.debdut.anchordi.Module
import com.debdut.anchordi.Singleton
import com.debdut.anchordi.SingletonComponent
import com.debdut.simpletemplate.product.domain.ProductRepository
import com.debdut.simpletemplate.product.domain.ProductRepositoryImpl

/**
 * Binds [ProductRepository] to [ProductRepositoryImpl] in [SingletonComponent].
 * One shared repository for the app; list and details ViewModels inject it.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProductRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
}
