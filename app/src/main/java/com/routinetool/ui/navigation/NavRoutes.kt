package com.routinetool.ui.navigation

/**
 * Navigation route definitions for the app.
 * Centralized route constants for type-safe navigation.
 */
object NavRoutes {
    const val TASK_LIST = "task_list"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task/{taskId}"
    const val FOCUS_VIEW = "focus_view"

    /**
     * Build the edit task route with a specific task ID.
     */
    fun editTask(taskId: String) = "edit_task/$taskId"
}
