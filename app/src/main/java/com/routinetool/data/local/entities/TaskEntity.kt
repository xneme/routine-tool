package com.routinetool.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val softDeadline: Long? = null,    // epoch millis for Room compatibility
    val hardDeadline: Long? = null,    // epoch millis
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,     // epoch millis
    val archivedAt: Long? = null,      // epoch millis — set 24h after completion for Done section hiding
    val createdAt: Long = System.currentTimeMillis(),
    val taskType: String = "ONE_TIME"  // future-proofing for Phase 4
)
