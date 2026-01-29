package com.routinetool.ui.screens.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routinetool.data.local.entities.TaskEntity
import com.routinetool.data.preferences.PreferencesDataStore
import com.routinetool.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

/**
 * ViewModel for adding and editing tasks.
 * Handles both new task creation and existing task updates.
 * Manages persistent expansion state for Notes and Deadlines sections.
 */
class AddTaskViewModel(
    private val repository: TaskRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val taskId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    private val _savedEvent = MutableSharedFlow<Boolean>()
    val savedEvent: SharedFlow<Boolean> = _savedEvent.asSharedFlow()

    // Expansion state from DataStore (persists across sessions)
    val notesExpanded: StateFlow<Boolean> = preferencesDataStore.notesExpanded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val deadlinesExpanded: StateFlow<Boolean> = preferencesDataStore.deadlinesExpanded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        taskId?.let { loadTask(it) }
    }

    /**
     * Load an existing task for editing.
     */
    private fun loadTask(id: String) {
        viewModelScope.launch {
            val task = repository.getById(id) ?: return@launch

            // Convert Instant back to LocalDate for editing
            val softDate = task.softDeadline?.let { instant ->
                instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            val hardDate = task.hardDeadline?.let { instant ->
                instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            }

            _uiState.value = _uiState.value.copy(
                title = task.title,
                description = task.description ?: "",
                softDeadline = softDate,
                hardDeadline = hardDate,
                isEditMode = true,
                editTaskId = id
            )
        }
    }

    /**
     * Update the task title.
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    /**
     * Update the task description.
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    /**
     * Set or clear the soft deadline.
     */
    fun setSoftDeadline(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(softDeadline = date)
    }

    /**
     * Set or clear the hard deadline.
     */
    fun setHardDeadline(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(hardDeadline = date)
    }

    /**
     * Toggle the Notes section expansion and persist to DataStore.
     */
    fun toggleNotesExpanded() {
        viewModelScope.launch {
            preferencesDataStore.saveNotesExpanded(!notesExpanded.value)
        }
    }

    /**
     * Toggle the Deadlines section expansion and persist to DataStore.
     */
    fun toggleDeadlinesExpanded() {
        viewModelScope.launch {
            preferencesDataStore.saveDeadlinesExpanded(!deadlinesExpanded.value)
        }
    }

    /**
     * Save the task (insert new or update existing).
     * Validates that title is not blank.
     * Emits success event on completion.
     */
    fun saveTask() {
        val state = _uiState.value

        // Validate title
        if (state.title.isBlank()) {
            return
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                // Convert LocalDate to epoch millis for storage
                val softDeadlineMillis = state.softDeadline?.atStartOfDayIn(
                    TimeZone.currentSystemDefault()
                )?.toEpochMilliseconds()

                val hardDeadlineMillis = state.hardDeadline?.atStartOfDayIn(
                    TimeZone.currentSystemDefault()
                )?.toEpochMilliseconds()

                if (state.isEditMode && state.editTaskId != null) {
                    // Update existing task - fetch from DB to get entity
                    val existingTaskDomain = repository.getById(state.editTaskId) ?: return@launch
                    val updatedEntity = TaskEntity(
                        id = state.editTaskId,
                        title = state.title,
                        description = state.description.ifBlank { null },
                        softDeadline = softDeadlineMillis,
                        hardDeadline = hardDeadlineMillis,
                        isCompleted = existingTaskDomain.isCompleted,
                        completedAt = existingTaskDomain.completedAt?.toEpochMilliseconds(),
                        createdAt = existingTaskDomain.createdAt.toEpochMilliseconds(),
                        taskType = existingTaskDomain.taskType
                    )
                    repository.update(updatedEntity)
                } else {
                    // Insert new task
                    val newTask = TaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = state.title,
                        description = state.description.ifBlank { null },
                        softDeadline = softDeadlineMillis,
                        hardDeadline = hardDeadlineMillis
                    )
                    repository.insert(newTask)
                }

                _savedEvent.emit(true)
            } catch (e: Exception) {
                _savedEvent.emit(false)
            } finally {
                _uiState.value = state.copy(isSaving = false)
            }
        }
    }
}

/**
 * UI state for the add/edit task screen.
 * Note: Expansion state for Notes and Deadlines is managed separately via
 * persistent StateFlows (notesExpanded, deadlinesExpanded) from DataStore.
 */
data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val softDeadline: LocalDate? = null,
    val hardDeadline: LocalDate? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val editTaskId: String? = null
)
