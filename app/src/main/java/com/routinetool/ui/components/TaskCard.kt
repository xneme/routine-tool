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
    var isRescheduleExpanded by remember { mutableStateOf(false) }

    // Determine text color based on task state
    val textColor = when {
        task.isCompleted -> MaterialTheme.colorScheme.onTertiaryContainer
        isOverdue -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                task.isCompleted -> MaterialTheme.colorScheme.tertiaryContainer
                isOverdue -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: title with weight
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Right: Column with deadline badge on top, checkbox below
                Column(horizontalAlignment = Alignment.End) {
                    if (task.softDeadline != null || task.hardDeadline != null) {
                        DeadlineBadge(
                            softDeadline = task.softDeadline,
                            hardDeadline = task.hardDeadline
                        )
                    }
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
private fun formatDeadline(instant: kotlinx.datetime.Instant): String {
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.monthNumber}/${date.dayOfMonth}/${date.year}"
}
