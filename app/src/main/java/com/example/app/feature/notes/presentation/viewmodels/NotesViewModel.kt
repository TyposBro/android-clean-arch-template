package com.example.app.feature.notes.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.app.navigation.AppDestinations
import com.example.app.app.navigation.NavigationManager
import com.example.app.core.network.models.RepositoryResult
import com.example.app.core.ui.UiState
import com.example.app.feature.notes.domain.models.Note
import com.example.app.feature.notes.domain.usecases.CreateNoteUseCase
import com.example.app.feature.notes.domain.usecases.DeleteNoteUseCase
import com.example.app.feature.notes.domain.usecases.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _notesState = MutableStateFlow<UiState<List<Note>>>(UiState.Idle)
    val notesState: StateFlow<UiState<List<Note>>> = _notesState.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _notesState.value = UiState.Loading
            when (val result = getNotesUseCase()) {
                is RepositoryResult.Success -> {
                    _notesState.value = UiState.Success(result.data)
                }
                is RepositoryResult.Error -> {
                    _notesState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            when (val result = createNoteUseCase(title, content)) {
                is RepositoryResult.Success -> {
                    loadNotes()
                }
                is RepositoryResult.Error -> {
                    _notesState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            when (val result = deleteNoteUseCase(id)) {
                is RepositoryResult.Success -> {
                    loadNotes()
                }
                is RepositoryResult.Error -> {
                    _notesState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun navigateToDetail(noteId: String) {
        navigationManager.navigate(AppDestinations.noteDetail(noteId))
    }

    fun navigateBack() {
        navigationManager.popBackStack()
    }
}
