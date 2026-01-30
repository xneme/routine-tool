package com.routinetool.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routinetool.domain.model.Task
import com.routinetool.domain.model.TaskWithSubtasks
import kotlinx.datetime.DatePeriod
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Expandable task card with collapse/expand animation.
 * Supports completion, quick rescheduling, editing, and subtask toggle.
 */
@Composable
fun TaskCard(
    taskWithSubtasks: TaskWithSubtasks,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit,
    onDismissOverdue: () -> Unit,
    onReschedule: (LocalDate) -> Unit,
    onEditTask: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    isOverdue: Boolean,
    isLongOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val task = taskWithSubtasks.task
    val view = LocalView.current
    var isRescheduleExpanded by remember { mutableStateOf(false) }

    // Use onSurface for all text since we're using subtle custom tints
    val textColor = MaterialTheme.colorScheme.onSurface

    // Subtle custom tints over surface color
    val surfaceColor = MaterialTheme.colorScheme.surface
    val containerColor = when {
        // Subtle green tint for completed tasks
        task.isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.12f).compositeOver(surfaceColor)
        // Very subtle red tint for overdue tasks
        isOverdue -> Color(0xFFE53935).copy(alpha = 0.08f).compositeOver(surfaceColor)
        // Lighter surface for active tasks (more distinct from background)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .clickable { onExpandToggle() }
                .padding(16.dp)
        ) {
            // Collapsed content (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: title and subtask progress indicator
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )
                    if (taskWithSubtasks.hasSubtasks) {
                        SubtaskProgressIndicator(
                            completedCount = taskWithSubtasks.completedSubtaskCount,
                            totalCount = taskWithSubtasks.totalSubtaskCount,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Right: Column with deadline badge on top, checkbox below
                Column(horizontalAlignment = Alignment.End) {
                    if (task.softDeadline != null || task.hardDeadline != null) {
                        DeadlineBadge(
                            softDeadline = task.softDeadline,
                            hardDeadline = task.hardDeadline
                        )
                    }
                    if (task.isCompleted) {
                        // Bright green checkmark icon (no box) for completed tasks
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onUncomplete()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Completed - tap to undo",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        // Regular checkbox for incomplete tasks
                        Checkbox(
                            checked = false,
                            onCheckedChange = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onComplete()
                            },
                            colors = CheckboxDefaults.colors(
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
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
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Subtask checklist
                if (taskWithSubtasks.subtasks.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Subtasks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        taskWithSubtasks.subtasks.forEach { subtask ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (subtask.isCompleted) {
                                    IconButton(onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        onToggleSubtask(subtask.id)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Checkbox(
                                        checked = false,
                                        onCheckedChange = {
                                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                            onToggleSubtask(subtask.id)
                                        }
                                    )
                                }
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Deadline details
                if (task.softDeadline != null || task.hardDeadline != null) {
                    Column {
                        if (task.softDeadline != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = "Soft deadline",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDeadline(task.softDeadline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (task.hardDeadline != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Hard deadline",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDeadline(task.hardDeadline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Overdue handling with collapsible reschedule options
                if (isOverdue && !task.isCompleted) {
                    Column {
                        if (isLongOverdue) {
                            Text(
                                text = "This has been waiting a while",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Collapsible reschedule button
                        TextButton(onClick = { isRescheduleExpanded = !isRescheduleExpanded }) {
                            Text("Reschedule")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isRescheduleExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isRescheduleExpanded) "Collapse" else "Expand"
                            )
                        }

                        // Animated reschedule options
                        AnimatedVisibility(
                            visible = isRescheduleExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
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
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Edit button
                Row {
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
@Suppress("DEPRECATION")
private fun formatDeadline(instant: Instant): String {
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.monthNumber}/${date.dayOfMonth}/${date.year}"
}
