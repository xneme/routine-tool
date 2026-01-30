package com.routinetool.ui.screens.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routinetool.data.local.entities.TaskEntity
import com.routinetool.data.preferences.PreferencesDataStore
import com.routinetool.data.repository.TaskRepository
import com.routinetool.domain.model.Subtask
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    val subtasksExpanded: StateFlow<Boolean> = preferencesDataStore.subtasksExpanded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Subtask management
    private val _subtasks = MutableStateFlow<List<Subtask>>(emptyList())
    val subtasks: StateFlow<List<Subtask>> = _subtasks.asStateFlow()

    // For new tasks (not edit mode), collect subtask titles until task is saved
    private val _pendingSubtasks = MutableStateFlow<List<String>>(emptyList())
    val pendingSubtasks: StateFlow<List<String>> = _pendingSubtasks.asStateFlow()

    // Show warning when subtasks count reaches 20
    val isSubtaskLimitWarningVisible: StateFlow<Boolean> = combine(
        subtasks,
        pendingSubtasks
    ) { subtasks, pending ->
        (subtasks.size + pending.size) >= 20
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

            // Observe subtasks for this task
            repository.observeSubtasks(id).collect { subtaskList ->
                _subtasks.value = subtaskList
            }
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
     * Toggle the Subtasks section expansion and persist to DataStore.
     */
    fun toggleSubtasksExpanded() {
        viewModelScope.launch {
            preferencesDataStore.saveSubtasksExpanded(!subtasksExpanded.value)
        }
    }

    /**
     * Add a subtask. In edit mode, saves immediately. In add mode, adds to pending list.
     */
    fun addSubtask(title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val state = _uiState.value
        if (state.isEditMode && state.editTaskId != null) {
            // Edit mode: save immediately
            viewModelScope.launch {
                repository.addSubtask(state.editTaskId, trimmedTitle)
            }
        } else {
            // Add mode: collect in pending list
            _pendingSubtasks.value = _pendingSubtasks.value + trimmedTitle
        }
    }

    /**
     * Delete a subtask. Only works in edit mode.
     */
    fun deleteSubtask(subtaskId: String) {
        viewModelScope.launch {
            repository.deleteSubtask(subtaskId)
        }
    }

    /**
     * Reorder a subtask by moving from one index to another.
     */
    fun reorderSubtask(fromIndex: Int, toIndex: Int) {
        val currentSubtasks = _subtasks.value
        if (fromIndex < 0 || fromIndex >= currentSubtasks.size ||
            toIndex < 0 || toIndex >= currentSubtasks.size) {
            return
        }

        val subtaskToMove = currentSubtasks[fromIndex]
        viewModelScope.launch {
            repository.reorderSubtask(subtaskToMove.id, toIndex, currentSubtasks)
        }
    }

    /**
     * Delete a pending subtask (for new task mode only).
     */
    fun deletePendingSubtask(index: Int) {
        _pendingSubtasks.value = _pendingSubtasks.value.filterIndexed { i, _ -> i != index }
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
                    val newTaskId = UUID.randomUUID().toString()
                    val newTask = TaskEntity(
                        id = newTaskId,
                        title = state.title,
                        description = state.description.ifBlank { null },
                        softDeadline = softDeadlineMillis,
                        hardDeadline = hardDeadlineMillis
                    )
                    repository.insert(newTask)

                    // Save pending subtasks
                    _pendingSubtasks.value.forEach { subtaskTitle ->
                        repository.addSubtask(newTaskId, subtaskTitle)
                    }
                    _pendingSubtasks.value = emptyList()
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
