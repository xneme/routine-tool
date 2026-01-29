package com.routinetool.ui.screens.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routinetool.data.repository.TaskRepository
import com.routinetool.domain.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * ViewModel for the main task list screen.
 * Implements Unidirectional Data Flow (UDF) pattern with single StateFlow.
 */
class TaskListViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    companion object {
        const val LONG_OVERDUE_DAYS = 7
    }

    /**
     * Combined UI state for the task list screen.
     * Sections are mutually exclusive - each task appears in exactly one section.
     */
    val uiState: StateFlow<TaskListUiState> = combine(
        repository.observeActiveTasks(),
        repository.observeRecentlyCompleted()
    ) { activeTasks, completedTasks ->
        val now = Clock.System.now()

        // Split active tasks into overdue and active sections
        val (overdue, active) = activeTasks.partition { task ->
            val nearestDeadline = listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull()
            nearestDeadline != null && nearestDeadline < now
        }

        TaskListUiState(
            overdueTasks = overdue,
            activeTasks = active,
            doneTasks = completedTasks,
            expandedTaskId = null,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUiState(isLoading = true)
    )

    /**
     * Toggle expansion state for a task card.
     * Only one task can be expanded at a time.
     */
    fun toggleExpanded(taskId: String) {
        // State update will be handled by recomposition when task card checks isExpanded
        // For now, we'll track this in a private mutable state if needed
        // Since we're using stateless composables, expansion state can be tracked per-card
        // This is a placeholder - expansion state is actually managed by the composable
    }

    /**
     * Mark a task as completed with current timestamp.
     */
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            repository.completeTask(taskId)
        }
    }

    /**
     * Mark a task as incomplete (undo completion).
     */
    fun uncompleteTask(taskId: String) {
        viewModelScope.launch {
            repository.uncompleteTask(taskId)
        }
    }

    /**
     * Remove deadlines from an overdue task.
     */
    fun dismissOverdue(taskId: String) {
        viewModelScope.launch {
            repository.dismissOverdue(taskId)
        }
    }

    /**
     * Reschedule a task to a new date.
     * Updates the nearest deadline (soft or hard, whichever was overdue).
     */
    fun rescheduleTask(taskId: String, newDate: LocalDate) {
        viewModelScope.launch {
            repository.rescheduleTask(taskId, newDate)
        }
    }
}

/**
 * UI state for the task list screen.
 */
data class TaskListUiState(
    val overdueTasks: List<Task> = emptyList(),
    val activeTasks: List<Task> = emptyList(),
    val doneTasks: List<Task> = emptyList(),
    val expandedTaskId: String? = null,
    val isLoading: Boolean = true
)
