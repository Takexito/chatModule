package com.dev.podo.event.di

import com.dev.podo.event.repository.EventRepository
import com.dev.podo.event.repository.EventRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class EventDataModule {
    @Binds
    abstract fun provideEventRepository(eventRepositoryImpl: EventRepositoryImpl): EventRepository
}
