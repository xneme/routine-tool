package com.routinetool.domain.model

data class TaskWithSubtasks(
    val task: Task,
    val subtasks: List<Subtask>
) {
    val completedSubtaskCount: Int get() = subtasks.count { it.isCompleted }
    val totalSubtaskCount: Int get() = subtasks.size
    val hasSubtasks: Boolean get() = subtasks.isNotEmpty()
}
