package com.routinetool.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model for a task, separate from Room entity.
 * Uses kotlinx-datetime Instant for type-safe timestamp handling.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val softDeadline: Instant?,
    val hardDeadline: Instant?,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val createdAt: Instant,
    val taskType: String
)
