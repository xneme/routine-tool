package com.routinetool.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routinetool.domain.model.Task
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue

/**
 * Expandable task card with collapse/expand animation.
 * Supports completion, quick rescheduling, and editing.
 */
@Composable
fun TaskCard(
    task: Task,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit,
    onDismissOverdue: () -> Unit,
    onReschedule: (LocalDate) -> Unit,
    onEditTask: () -> Unit,
    isOverdue: Boolean,
    isLongOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .clickable { onExpandToggle() }
                .padding(16.dp)
        ) {
            // Collapsed content (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { checked ->
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        if (checked) {
                            onComplete()
                        } else {
                            onUncomplete()
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (task.softDeadline != null || task.hardDeadline != null) {
                    DeadlineBadge(
                        softDeadline = task.softDeadline,
                        hardDeadline = task.hardDeadline
                    )
                }
            }

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Description
                if (!task.description.isNullOrBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Deadline details
                if (task.softDeadline != null || task.hardDeadline != null) {
                    Column(
                        modifier = Modifier.padding(start = 56.dp)
                    ) {
                        if (task.softDeadline != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = "Soft deadline",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDeadline(task.softDeadline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (task.hardDeadline != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Hard deadline",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDeadline(task.hardDeadline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Overdue handling
                if (isOverdue && !task.isCompleted) {
                    Column(
                        modifier = Modifier.padding(start = 56.dp)
                    ) {
                        if (isLongOverdue) {
                            Text(
                                text = "This has been waiting a while",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val tomorrow = today.plus(DatePeriod(days = 1))
                            val oneWeekLater = today.plus(DatePeriod(days = 7))

                            TextButton(onClick = { onReschedule(tomorrow) }) {
                                Text("Tomorrow")
                            }
                            TextButton(onClick = { onReschedule(oneWeekLater) }) {
                                Text("+1 week")
                            }
                            TextButton(onClick = { onDismissOverdue() }) {
                                Text("Remove deadline")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Edit button
                Row(
                    modifier = Modifier.padding(start = 56.dp)
                ) {
                    TextButton(onClick = onEditTask) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

/**
 * Format an Instant as a readable date string.
 */
private fun formatDeadline(instant: kotlinx.datetime.Instant): String {
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.monthNumber}/${date.dayOfMonth}/${date.year}"
}
