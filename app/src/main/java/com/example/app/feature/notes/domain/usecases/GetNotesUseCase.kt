package com.example.app.feature.notes.domain.usecases

import com.example.app.core.network.models.RepositoryResult
import com.example.app.feature.notes.data.repositories.NotesRepository
import com.example.app.feature.notes.domain.models.Note
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(): RepositoryResult<List<Note>> {
        return notesRepository.getNotes()
    }
}
