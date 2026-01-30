package com.routinetool.ui.screens.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routinetool.data.preferences.PreferencesDataStore
import com.routinetool.data.repository.TaskRepository
import com.routinetool.domain.model.FilterState
import com.routinetool.domain.model.SortOption
import com.routinetool.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for the main task list screen.
 * Implements Unidirectional Data Flow (UDF) pattern with single StateFlow.
 * Supports sorting and filtering of tasks.
 */
class TaskListViewModel(
    private val repository: TaskRepository,
    private val dataStore: PreferencesDataStore
) : ViewModel() {

    companion object {
        const val LONG_OVERDUE_DAYS = 7
    }

    // Filter state - does NOT persist, resets to all-on when app restarts
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Sort preference - persists via DataStore
    val sortOption: StateFlow<SortOption> = dataStore.sortPreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortOption.URGENCY
        )

    /**
     * Combined UI state for the task list screen.
     * Applies filtering and sorting to tasks.
     * Sections are mutually exclusive - each task appears in exactly one section.
     */
    val uiState: StateFlow<TaskListUiState> = combine(
        repository.observeActiveTasks(),
        repository.observeRecentlyCompleted(),
        _filterState,
        sortOption
    ) { activeTasks, completedTasks, filter, sort ->
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        // Start of today - tasks are only overdue if deadline is BEFORE today (not today itself)
        val startOfToday = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            .atStartOfDayIn(TimeZone.currentSystemDefault())

        // Split active tasks into overdue and active sections
        val (overdue, active) = activeTasks.partition { task ->
            val nearestDeadline = listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull()
            nearestDeadline != null && nearestDeadline < startOfToday
        }

        // Apply filters to each section
        val filteredOverdue = overdue
            .filter { task -> matchesFilter(task, filter, isOverdue = true, isCompleted = false) }
            .sortedWith(sort.comparator())

        val filteredActive = active
            .filter { task -> matchesFilter(task, filter, isOverdue = false, isCompleted = false) }
            .sortedWith(sort.comparator())

        val filteredDone = completedTasks
            .filter { task -> matchesFilter(task, filter, isOverdue = false, isCompleted = true) }
            .sortedWith(sort.comparator())

        TaskListUiState(
            overdueTasks = filteredOverdue,
            activeTasks = filteredActive,
            doneTasks = filteredDone,
            currentSort = sort,
            expandedTaskId = null,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUiState(isLoading = true)
    )

    /**
     * Check if a task matches the current filter state.
     *
     * Filter logic:
     * - No filters selected (isDefault) = show all tasks
     * - Any filter selected = show only tasks matching selected filters
     * - Status and deadline type filters are independent dimensions
     */
    private fun matchesFilter(
        task: Task,
        filter: FilterState,
        isOverdue: Boolean,
        isCompleted: Boolean
    ): Boolean {
        // If no filters selected, show all tasks
        if (filter.isDefault) return true

        // Check status filter (if any status filter is active)
        val statusMatches = if (filter.hasStatusFilter) {
            when {
                isCompleted -> filter.showDone
                isOverdue -> filter.showOverdue
                else -> filter.showActive
            }
        } else {
            // No status filters selected = all statuses pass
            true
        }

        // Check deadline type filter (if any deadline filter is active)
        val deadlineTypeMatches = if (filter.hasDeadlineFilter) {
            val hasSoftDeadline = task.softDeadline != null
            val hasHardDeadline = task.hardDeadline != null
            val hasNoDeadline = !hasSoftDeadline && !hasHardDeadline

            when {
                hasNoDeadline -> filter.showNoDeadline
                else -> {
                    // Task may have soft, hard, or both - match if any applicable filter is on
                    (hasSoftDeadline && filter.showSoftDeadline) ||
                    (hasHardDeadline && filter.showHardDeadline)
                }
            }
        } else {
            // No deadline filters selected = all deadline types pass
            true
        }

        return statusMatches && deadlineTypeMatches
    }

    /**
     * Update filter state.
     */
    fun updateFilter(update: FilterState.() -> FilterState) {
        _filterState.value = _filterState.value.update()
    }

    /**
     * Set sort option and persist to DataStore.
     */
    fun setSortOption(option: SortOption) {
        viewModelScope.launch {
            dataStore.saveSortPreference(option)
        }
    }

    /**
     * Toggle expansion state for a task card.
     * Only one task can be expanded at a time.
     */
    fun toggleExpanded(taskId: String) {
        // State update will be handled by recomposition when task card checks isExpanded
        // For now, we'll track this in a private mutable state if needed
        // Since we're using stateless composables, expansion state is actually managed by the composable
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
    val currentSort: SortOption = SortOption.URGENCY,
    val expandedTaskId: String? = null,
    val isLoading: Boolean = true
)
