package com.routinetool.ui.screens.addtask

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.routinetool.domain.model.Subtask
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Screen for adding a new task or editing an existing one.
 * Implements progressive disclosure with expandable Notes and Deadlines sections.
 * Expansion state persists across sessions via DataStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onNavigateBack: () -> Unit,
    taskId: String? = null,
    viewModel: AddTaskViewModel = koinViewModel { parametersOf(taskId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val notesExpanded by viewModel.notesExpanded.collectAsState()
    val deadlinesExpanded by viewModel.deadlinesExpanded.collectAsState()
    val subtasksExpanded by viewModel.subtasksExpanded.collectAsState()
    val subtasks by viewModel.subtasks.collectAsState()
    val pendingSubtasks by viewModel.pendingSubtasks.collectAsState()
    val showSubtaskLimitWarning by viewModel.isSubtaskLimitWarningVisible.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Listen for save event and navigate back
    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { success ->
            if (success) {
                onNavigateBack()
            }
        }
    }

    // Auto-focus title field when screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit task" else "New task")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.saveTask()
                        },
                        enabled = uiState.title.isNotBlank() && !uiState.isSaving
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title field - always visible, auto-focused
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("What needs doing?") },
                placeholder = { Text("Enter task title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Notes section - expandable
            ExpandableSection(
                title = "Notes",
                expanded = notesExpanded,
                onToggle = { viewModel.toggleNotesExpanded() }
            ) {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Additional details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Default
                    )
                )
            }

            // Deadlines section - expandable
            ExpandableSection(
                title = "Deadlines",
                expanded = deadlinesExpanded,
                onToggle = { viewModel.toggleDeadlinesExpanded() }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Soft deadline picker
                    DeadlinePicker(
                        label = "Reminder date",
                        icon = Icons.Filled.Schedule,
                        selectedDate = uiState.softDeadline,
                        onDateSelected = { viewModel.setSoftDeadline(it) }
                    )

                    // Hard deadline picker
                    DeadlinePicker(
                        label = "Due date",
                        icon = Icons.Filled.Bookmark,
                        selectedDate = uiState.hardDeadline,
                        onDateSelected = { viewModel.setHardDeadline(it) }
                    )
                }
            }

            // Subtasks section - expandable with reorderable list
            ExpandableSection(
                title = "Subtasks",
                expanded = subtasksExpanded,
                onToggle = { viewModel.toggleSubtasksExpanded() }
            ) {
                SubtasksList(
                    subtasks = subtasks,
                    pendingSubtasks = pendingSubtasks,
                    isEditMode = uiState.isEditMode,
                    onAddSubtask = { viewModel.addSubtask(it) },
                    onDeleteSubtask = { viewModel.deleteSubtask(it) },
                    onDeletePendingSubtask = { viewModel.deletePendingSubtask(it) },
                    onReorder = { from, to -> viewModel.reorderSubtask(from, to) },
                    showLimitWarning = showSubtaskLimitWarning
                )
            }
        }
    }
}

/**
 * Expandable section with animated visibility.
 * Shows a header that can be tapped to expand/collapse content.
 */
@Composable
private fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            content()
        }
    }
}

/**
 * Composable for selecting a deadline date with optional clear button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlinePicker(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { showDatePicker = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = selectedDate?.toString() ?: "None",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (selectedDate != null) {
            IconButton(onClick = { onDateSelected(null) }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Material 3 DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convert millis to LocalDate
                            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                            onDateSelected(localDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Composable for managing subtasks with reorderable list.
 * Supports both edit mode (persisted subtasks) and add mode (pending subtasks).
 */
@Composable
private fun SubtasksList(
    subtasks: List<Subtask>,
    pendingSubtasks: List<String>,
    isEditMode: Boolean,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (String) -> Unit,
    onDeletePendingSubtask: (Int) -> Unit,
    onReorder: (Int, Int) -> Unit,
    showLimitWarning: Boolean,
    modifier: Modifier = Modifier
) {
    var newSubtaskTitle by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Existing subtasks (edit mode) - reorderable
        if (isEditMode && subtasks.isNotEmpty()) {
            val lazyListState = rememberLazyListState()
            val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                onReorder(from.index, to.index)
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(subtasks, key = { it.id }) { subtask ->
                    ReorderableItem(reorderableState, key = subtask.id) { isDragging ->
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "elevation"
                        )

                        // Haptic feedback on drag start/end
                        LaunchedEffect(isDragging) {
                            if (isDragging) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }

                        SubtaskRow(
                            title = subtask.title,
                            isCompleted = subtask.isCompleted,
                            onDelete = { onDeleteSubtask(subtask.id) },
                            reorderableState = reorderableState,
                            modifier = Modifier.shadow(elevation, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        // Pending subtasks (add mode) - not reorderable yet
        if (!isEditMode && pendingSubtasks.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pendingSubtasks.forEachIndexed { index, title ->
                    SubtaskRow(
                        title = title,
                        isCompleted = false,
                        onDelete = { onDeletePendingSubtask(index) },
                        reorderableState = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Add new subtask field
        OutlinedTextField(
            value = newSubtaskTitle,
            onValueChange = { newSubtaskTitle = it },
            placeholder = { Text("Add subtask") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newSubtaskTitle.isNotBlank()) {
                        onAddSubtask(newSubtaskTitle.trim())
                        newSubtaskTitle = ""
                    }
                }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Soft limit warning
        if (showLimitWarning) {
            Text(
                text = "This task has many steps. Consider breaking it into separate tasks for better focus.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Row displaying a single subtask with delete button.
 * When reorderableState is provided, the entire row is draggable via long-press.
 */
@Composable
private fun SubtaskRow(
    title: String,
    isCompleted: Boolean,
    onDelete: () -> Unit,
    reorderableState: ReorderableLazyListState?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for visual indication (not interactive here)
            Checkbox(
                checked = isCompleted,
                onCheckedChange = null,
                enabled = false
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete subtask",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
