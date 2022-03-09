package com.dev.podo.core.di

import com.dev.podo.core.repository.CoreRepository
import com.dev.podo.core.repository.CoreRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class CoreModule {
    @Binds
    abstract fun provideCoreRepository(coreRepository: CoreRepositoryImpl): CoreRepository
}
