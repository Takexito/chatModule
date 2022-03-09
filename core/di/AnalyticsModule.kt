package com.dev.podo.core.di

import com.dev.podo.core.repository.analytics.AnalyticsRepository
import com.dev.podo.core.repository.analytics.AnalyticsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class AnalyticsModule {
    @Binds
    abstract fun provideAnalyticsRepository(analyticsRepositoryImpl: AnalyticsRepositoryImpl): AnalyticsRepository
}
