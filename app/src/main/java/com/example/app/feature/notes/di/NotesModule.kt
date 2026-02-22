package com.example.app.feature.notes.di

import com.example.app.feature.notes.data.remote.NotesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotesModule {

    @Provides
    @Singleton
    fun provideNotesApi(@Named("standard") retrofit: Retrofit): NotesApi {
        return retrofit.create(NotesApi::class.java)
    }
}
