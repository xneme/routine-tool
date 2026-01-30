package com.routinetool.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation model for a Task with its Subtasks.
 * Used for querying tasks together with their subtasks in a single query.
 */
data class TaskWithSubtasksEntity(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity>
)
