package com.routinetool.ui.screens.addtask

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Screen for adding a new task or editing an existing one.
 * Implements progressive disclosure - details are hidden by default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onNavigateBack: () -> Unit,
    taskId: String? = null,
    viewModel: AddTaskViewModel = koinViewModel { parametersOf(taskId) }
) {
    val uiState by viewModel.uiState.collectAsState()
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            // Toggle details button
            TextButton(
                onClick = { viewModel.toggleDetails() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.showDetails) "Hide details" else "Add details",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Details section - shown when expanded
            if (uiState.showDetails) {
                // Description field
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
