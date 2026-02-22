package com.example.app.feature.notes.domain.usecases

import com.example.app.core.network.models.RepositoryResult
import com.example.app.feature.notes.data.repositories.NotesRepository
import com.example.app.feature.notes.domain.models.Note
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(title: String, content: String): RepositoryResult<Note> {
        return notesRepository.createNote(title, content)
    }
}
