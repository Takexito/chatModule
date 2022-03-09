package com.dev.podo.event.di

import com.dev.podo.event.repository.ChatRepository
import com.dev.podo.event.repository.ChatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class ChatModule {
    @Binds
    abstract fun provideChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository
}
