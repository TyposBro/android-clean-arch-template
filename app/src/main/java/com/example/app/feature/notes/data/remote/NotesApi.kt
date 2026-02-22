package com.example.app.feature.notes.data.remote

import com.example.app.core.network.models.GenericSuccessResponse
import com.example.app.feature.notes.domain.models.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotesApi {
    @GET("notes")
    suspend fun getNotes(): Response<List<Note>>

    @POST("notes")
    suspend fun createNote(@Body note: Note): Response<Note>

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<GenericSuccessResponse>
}
