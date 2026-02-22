package com.example.app.feature.notes.domain.usecases

import com.example.app.core.network.models.GenericSuccessResponse
import com.example.app.core.network.models.RepositoryResult
import com.example.app.feature.notes.data.repositories.NotesRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(id: String): RepositoryResult<GenericSuccessResponse> {
        return notesRepository.deleteNote(id)
    }
}
