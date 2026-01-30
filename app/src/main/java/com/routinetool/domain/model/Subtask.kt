package com.routinetool.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model for a subtask, separate from Room entity.
 * Uses kotlinx-datetime Instant for type-safe timestamp handling.
 */
data class Subtask(
    val id: String,
    val taskId: String,
    val title: String,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val position: Float,
    val createdAt: Instant
)
