package com.example.app.core.network.di

import com.example.app.BuildConfig
import com.example.app.core.auth.SessionManager
import com.example.app.core.network.interceptors.AuthInterceptor
import com.example.app.shared.auth.data.remote.AuthApi
import com.example.app.shared.user.data.remote.UserApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // --- 0. JSON Serialization ---

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    // --- 1. Clients ---

    @Provides
    @Singleton
    @Named("AuthOkHttpClient")
    fun provideAuthOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("StandardOkHttpClient")
    fun provideStandardOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // --- 2. Retrofit Instances ---

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@Named("AuthOkHttpClient") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("standard")
    fun provideStandardRetrofit(@Named("StandardOkHttpClient") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- 3. Interceptors ---

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        sessionManager: SessionManager,
        authApi: AuthApi
    ): AuthInterceptor {
        return AuthInterceptor(sessionManager, authApi)
    }

    // --- 4. APIs ---

    @Provides
    @Singleton
    fun provideAuthApi(@Named("auth") retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(@Named("standard") retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

}
