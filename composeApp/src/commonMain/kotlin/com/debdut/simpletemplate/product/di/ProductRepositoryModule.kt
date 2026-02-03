package com.debdut.simpletemplate.product.di

import com.debdut.anchordi.Binds
import com.debdut.anchordi.InstallIn
import com.debdut.anchordi.Module
import com.debdut.anchordi.SingletonComponent
import com.debdut.anchordi.ViewModelComponent
import com.debdut.simpletemplate.product.domain.ProductRepository
import com.debdut.simpletemplate.product.domain.ProductRepositoryImpl

/**
 * Binds [ProductRepository] to [ProductRepositoryImpl] in [SingletonComponent].
 * One shared repository for the app; list and details ViewModels inject it.
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class ProductRepositoryModule {
    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
}
