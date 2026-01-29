package com.routinetool.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routinetool.data.preferences.PreferencesDataStore
import com.routinetool.data.repository.TaskRepository
import com.routinetool.domain.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Focus View screen.
 * Manages hybrid task selection: pinned tasks first, then auto-selected by urgency.
 */
class FocusViewModel(
    private val repository: TaskRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val taskLimit = preferencesDataStore.focusTaskLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    private val pinnedTaskIds = preferencesDataStore.focusPinnedTaskIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val uiState: StateFlow<FocusUiState> = combine(
        repository.observeActiveTasks(),
        taskLimit,
        pinnedTaskIds
    ) { activeTasks, limit, pinned ->
        val focusTasks = selectFocusTasks(activeTasks, limit, pinned)
        FocusUiState(
            focusTasks = focusTasks,
            taskLimit = limit,
            pinnedTaskIds = pinned,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusUiState(isLoading = true)
    )

    /**
     * Hybrid task selection algorithm.
     * Pinned tasks always appear first, remaining slots filled by urgency.
     */
    private fun selectFocusTasks(
        allTasks: List<Task>,
        limit: Int,
        pinnedIds: Set<String>
    ): List<Task> {
        // Get pinned tasks that still exist and are active
        val pinnedTasks = allTasks.filter { it.id in pinnedIds }
        val remainingSlots = (limit - pinnedTasks.size).coerceAtLeast(0)

        // Fill remaining slots with non-pinned tasks by urgency
        val autoSelected = if (remainingSlots > 0) {
            allTasks
                .filterNot { it.id in pinnedIds }
                .sortedWith(urgencyComparator())
                .take(remainingSlots)
        } else {
            emptyList()
        }

        // Pinned first, then auto-selected
        return (pinnedTasks + autoSelected).take(limit)
    }

    /**
     * Urgency comparator: overdue first, then nearest deadline, then no deadline.
     */
    private fun urgencyComparator(): Comparator<Task> {
        val now = System.currentTimeMillis()
        return compareBy(
            { task ->
                val deadline = listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull()
                if (deadline != null && deadline.toEpochMilliseconds() < now) 0 else 1
            },
            { task ->
                listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull()?.toEpochMilliseconds() ?: Long.MAX_VALUE
            }
        )
    }

    fun togglePin(taskId: String) {
        viewModelScope.launch {
            val current = pinnedTaskIds.value
            val updated = if (taskId in current) current - taskId else current + taskId
            preferencesDataStore.saveFocusPinnedTaskIds(updated)
        }
    }

    fun setTaskLimit(limit: Int) {
        viewModelScope.launch {
            preferencesDataStore.saveFocusTaskLimit(limit.coerceIn(1, 10))
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            repository.completeTask(taskId)
        }
    }
}

/**
 * UI state for Focus View screen.
 */
data class FocusUiState(
    val focusTasks: List<Task> = emptyList(),
    val taskLimit: Int = 5,
    val pinnedTaskIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)
