package com.dev.podo.core.di

import com.dev.podo.event.datasource.ChatMessageDataSource
import com.dev.podo.event.datasource.ChatMessageDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatSingletonModule {
    @Binds
    @Singleton
    abstract fun provideChatMessageDataSource(chatMessageDataSourceImpl: ChatMessageDataSourceImpl): ChatMessageDataSource
}
