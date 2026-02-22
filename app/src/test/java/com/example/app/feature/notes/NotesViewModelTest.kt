package com.example.app.feature.notes

import app.cash.turbine.test
import com.example.app.app.navigation.NavigationManager
import com.example.app.core.network.models.RepositoryResult
import com.example.app.core.ui.UiState
import com.example.app.feature.notes.domain.models.Note
import com.example.app.feature.notes.domain.usecases.CreateNoteUseCase
import com.example.app.feature.notes.domain.usecases.DeleteNoteUseCase
import com.example.app.feature.notes.domain.usecases.GetNotesUseCase
import com.example.app.feature.notes.presentation.viewmodels.NotesViewModel
import com.example.app.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getNotesUseCase: GetNotesUseCase = mockk()
    private val createNoteUseCase: CreateNoteUseCase = mockk()
    private val deleteNoteUseCase: DeleteNoteUseCase = mockk()
    private val navigationManager: NavigationManager = mockk(relaxed = true)

    private val sampleNotes = listOf(
        Note(id = "1", title = "First Note", content = "Content 1", createdAt = "2026-01-01"),
        Note(id = "2", title = "Second Note", content = "Content 2", createdAt = "2026-01-02")
    )

    private fun createViewModel(): NotesViewModel {
        return NotesViewModel(
            getNotesUseCase = getNotesUseCase,
            createNoteUseCase = createNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            navigationManager = navigationManager
        )
    }

    @Test
    fun `loadNotes success updates state to Success`() = runTest {
        coEvery { getNotesUseCase() } returns RepositoryResult.Success(sampleNotes)

        val viewModel = createViewModel()

        viewModel.notesState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(sampleNotes, (state as UiState.Success).data)
        }
    }

    @Test
    fun `loadNotes error updates state to Error`() = runTest {
        val errorMessage = "Failed to fetch notes"
        coEvery { getNotesUseCase() } returns RepositoryResult.Error(errorMessage)

        val viewModel = createViewModel()

        viewModel.notesState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(errorMessage, (state as UiState.Error).message)
        }
    }

    @Test
    fun `createNote calls use case and reloads`() = runTest {
        val newNote = Note(id = "3", title = "New Note", content = "New Content", createdAt = "2026-01-03")
        coEvery { getNotesUseCase() } returns RepositoryResult.Success(sampleNotes)
        coEvery { createNoteUseCase("New Note", "New Content") } returns RepositoryResult.Success(newNote)

        val viewModel = createViewModel()

        viewModel.notesState.test {
            // Consume initial load result
            awaitItem()

            // Now set up the mock to return updated list for reload
            val updatedNotes = sampleNotes + newNote
            coEvery { getNotesUseCase() } returns RepositoryResult.Success(updatedNotes)

            viewModel.createNote("New Note", "New Content")

            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(updatedNotes, (state as UiState.Success).data)
        }

        coVerify { createNoteUseCase("New Note", "New Content") }
        // init load + reload after create
        coVerify(atLeast = 2) { getNotesUseCase() }
    }

    @Test
    fun `deleteNote calls use case and reloads`() = runTest {
        coEvery { getNotesUseCase() } returns RepositoryResult.Success(sampleNotes)
        coEvery { deleteNoteUseCase("1") } returns RepositoryResult.Success(
            com.example.app.core.network.models.GenericSuccessResponse(success = true)
        )

        val viewModel = createViewModel()

        viewModel.notesState.test {
            // Consume initial load result
            awaitItem()

            // Set up the mock to return filtered list for reload
            val remainingNotes = sampleNotes.filter { it.id != "1" }
            coEvery { getNotesUseCase() } returns RepositoryResult.Success(remainingNotes)

            viewModel.deleteNote("1")

            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(remainingNotes, (state as UiState.Success).data)
        }

        coVerify { deleteNoteUseCase("1") }
        // init load + reload after delete
        coVerify(atLeast = 2) { getNotesUseCase() }
    }
}
