package com.example.app.feature.notes.data.repositories

import com.example.app.core.network.models.GenericSuccessResponse
import com.example.app.core.network.models.RepositoryResult
import com.example.app.core.network.models.safeApiCall
import com.example.app.feature.notes.data.local.NoteDao
import com.example.app.feature.notes.data.local.NoteEntity
import com.example.app.feature.notes.data.remote.NotesApi
import com.example.app.feature.notes.domain.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val notesApi: NotesApi,
    private val noteDao: NoteDao
) {
    suspend fun getNotes(): RepositoryResult<List<Note>> {
        val result = safeApiCall { notesApi.getNotes() }
        if (result is RepositoryResult.Success) {
            result.data.forEach { note ->
                noteDao.insert(note.toEntity())
            }
        }
        return result
    }

    suspend fun createNote(title: String, content: String): RepositoryResult<Note> {
        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            createdAt = System.currentTimeMillis().toString()
        )
        val result = safeApiCall { notesApi.createNote(note) }
        if (result is RepositoryResult.Success) {
            noteDao.insert(result.data.toEntity())
        }
        return result
    }

    suspend fun deleteNote(id: String): RepositoryResult<GenericSuccessResponse> {
        val result = safeApiCall { notesApi.deleteNote(id) }
        if (result is RepositoryResult.Success) {
            noteDao.deleteById(id)
        }
        return result
    }

    fun getLocalNotes(): Flow<List<Note>> {
        return noteDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt
        )
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt
        )
    }
}
