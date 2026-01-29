package com.routinetool.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue

/**
 * Badge showing deadline proximity in a calm, neutral style.
 * Never uses error/red colors, even for overdue tasks.
 */
@Composable
fun DeadlineBadge(
    softDeadline: Instant?,
    hardDeadline: Instant?,
    modifier: Modifier = Modifier
) {
    val nearestDeadline = listOfNotNull(softDeadline, hardDeadline).minOrNull() ?: return
    val isHardDeadline = nearestDeadline == hardDeadline

    val timeZone = TimeZone.currentSystemDefault()
    val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())

    val deadlineDate = nearestDeadline.toLocalDateTime(timeZone).date
    val todayDate = now.toLocalDateTime(timeZone).date

    val daysUntil = (deadlineDate.toEpochDays() - todayDate.toEpochDays())

    val text = when {
        daysUntil == 0L -> "Today"
        daysUntil == 1L -> "Tomorrow"
        daysUntil == -1L -> "Yesterday"
        daysUntil > 0 -> "In $daysUntil days"
        else -> "${daysUntil.absoluteValue} days ago"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isHardDeadline) Icons.Filled.Bookmark else Icons.Filled.Schedule,
            contentDescription = if (isHardDeadline) "Hard deadline" else "Soft deadline",
            tint = if (isHardDeadline)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
