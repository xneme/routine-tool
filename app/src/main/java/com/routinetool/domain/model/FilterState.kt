package com.routinetool.domain.model

/**
 * Filter state for task list.
 * All filters default to false (unselected).
 * When no filters are selected, all tasks are shown.
 * When any filter is selected, only matching tasks are shown.
 */
data class FilterState(
    val showActive: Boolean = false,
    val showOverdue: Boolean = false,
    val showDone: Boolean = false,
    val showSoftDeadline: Boolean = false,
    val showHardDeadline: Boolean = false,
    val showNoDeadline: Boolean = false
) {
    /** True when no filters are selected (show all tasks) */
    val isDefault: Boolean
        get() = !showActive && !showOverdue && !showDone && !showSoftDeadline && !showHardDeadline && !showNoDeadline

    /** True when any status filter is selected */
    val hasStatusFilter: Boolean
        get() = showActive || showOverdue || showDone

    /** True when any deadline type filter is selected */
    val hasDeadlineFilter: Boolean
        get() = showSoftDeadline || showHardDeadline || showNoDeadline

    /** Count of active filters for badge display */
    val activeFilterCount: Int
        get() = listOf(showActive, showOverdue, showDone, showSoftDeadline, showHardDeadline, showNoDeadline).count { it }
}
