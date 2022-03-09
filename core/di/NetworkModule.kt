package com.dev.podo.core.di

import com.dev.podo.BuildConfig
import com.dev.podo.auth.datasource.AuthApi
import com.dev.podo.common.datasource.CityApi
import com.dev.podo.common.datasource.MediaApi
import com.dev.podo.core.repository.ExceptionHandler
import com.dev.podo.event.datasource.ChatApi
import com.dev.podo.home.datasource.HomeApi
import com.dev.podo.podoNow.datasource.PodoNowApi
import com.dev.podo.podoplus.datasource.PurchaseApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    fun providesBaseUrl(): String = BuildConfig.BACK_HOST

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(headerInterceptor())
        .build()

    fun headerInterceptor() = Interceptor { chain ->
        val request: Request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept-Language", "ru")
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideExceptionHandler(moshi: Moshi): ExceptionHandler = ExceptionHandler(moshi)

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .build()

    @Provides
    @Singleton
    fun provideMainService(retrofit: Retrofit): HomeApi = retrofit.create(HomeApi::class.java)

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCityService(retrofit: Retrofit): CityApi = retrofit.create(CityApi::class.java)

    @Provides
    @Singleton
    fun provideEventService(retrofit: Retrofit): EventApi = retrofit.create(EventApi::class.java)

    @Provides
    @Singleton
    fun provideChatService(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun providePodoNowService(retrofit: Retrofit): PodoNowApi =
        retrofit.create(PodoNowApi::class.java)

    @Provides
    @Singleton
    fun provideMediaService(retrofit: Retrofit): MediaApi = retrofit.create(MediaApi::class.java)

    @Provides
    @Singleton
    fun providePurchaseService(retrofit: Retrofit): PurchaseApi =
        retrofit.create(PurchaseApi::class.java)
}
