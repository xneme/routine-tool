# Phase 1: Foundation & Quick Capture - Research

**Researched:** 2026-01-28
**Domain:** Android task management app with Jetpack Compose Material 3
**Confidence:** HIGH

## Summary

This phase establishes a greenfield Android app using Jetpack Compose with Material Design 3, Room database for local storage, and modern Android architecture (ViewModel + StateFlow + UDF). The technical domain is well-established with official Google support and extensive documentation. The implementation involves building a FAB-initiated task capture flow, expandable card-based task list, deadline management, and shame-free UX specifically designed for neurodivergent users.

**Key technical challenges:**
- Implementing expandable card animations with clean state management
- Setting up Room database with proper migration strategy from the start
- Creating shame-free, calm UI with minimal visual noise
- Handling deadline proximity ordering with reactive updates

**Primary recommendation:** Use the standard Google-recommended stack (Kotlin, Jetpack Compose Material 3, Room, ViewModel + StateFlow, Koin for DI) with a focus on simple, testable architecture. Prioritize expandable card patterns and haptic feedback for tactile satisfaction. Design database schema to support future task types and relationships from the start.

## Standard Stack

The established libraries/tools for Android task management apps with Compose:

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.3.0+ | Language | Google's official Android language; multiplatform-ready |
| Jetpack Compose BOM | 2024.12+ | UI Framework | Declarative UI, official Android toolkit |
| Material 3 Compose | via BOM | Design System | Material You dynamic theming, accessibility built-in |
| Room | 2.7+ | Local Database | Google-recommended ORM with compile-time SQL verification |
| Navigation Compose | 2.8+ | Navigation | Type-safe single-activity navigation |
| ViewModel + Lifecycle | 2.8+ | State Management | Configuration-change survival, official architecture component |
| Kotlin Coroutines | 1.9+ | Async | Standard for Android async operations |
| StateFlow / Flow | via Coroutines | Reactive Streams | Room integration, reactive UI updates |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlinx-datetime | 0.7.1+ | Date/Time | Deadline calculations, multiplatform-compatible |
| kotlinx-serialization | 1.7+ | Serialization | Navigation args, future data export |
| Koin | 4.0+ | Dependency Injection | Lightweight, KMP-compatible alternative to Hilt |
| Compose UI Test | via BOM | UI Testing | Testing composables and interactions |
| JUnit 5 + MockK | Latest | Unit Testing | Kotlin-friendly testing with coroutine support |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Koin | Hilt/Dagger | Hilt has more boilerplate but better compile-time safety; overkill for this scale |
| Room | Realm | Room has better Compose/Flow integration and Google support |
| kotlinx-datetime | Java Time API | kotlinx-datetime is multiplatform-ready for future expansion |
| Material 3 | Custom Design System | M3 provides accessibility, dynamic theming, and established patterns |

**Installation:**
```bash
# Use Gradle Version Catalog (recommended)
# Add to libs.versions.toml:
[versions]
kotlin = "2.3.0"
composeBom = "2024.12.01"
room = "2.7.0"
kotlinx-datetime = "0.7.1"
koin = "4.0.0"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
# ... (see STACK.md for complete list)
```

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/example/routinetool/
├── data/
│   ├── local/
│   │   ├── database/      # Room database, DAOs
│   │   └── entities/      # Room entities
│   └── repository/        # Repository pattern implementations
├── domain/                # Optional for Phase 1, future use cases
│   └── model/            # Domain models (separate from entities)
├── ui/
│   ├── screens/          # Screen-level composables
│   │   ├── tasklist/     # Task list screen + ViewModel
│   │   └── taskdetail/   # Task detail/edit screen + ViewModel
│   ├── components/       # Reusable composables (TaskCard, etc.)
│   └── theme/           # Material 3 theme, colors, typography
└── di/                  # Koin modules
```

### Pattern 1: Unidirectional Data Flow (UDF) with ViewModel + StateFlow

**What:** Single StateFlow exposes immutable UI state; events flow up to ViewModel

**When to use:** All screens with dynamic data

**Example:**
```kotlin
// ViewModel
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Loading)
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.observeTasks().collect { tasks ->
                _uiState.value = TaskListUiState.Success(tasks)
            }
        }
    }

    fun onTaskCompleted(taskId: String) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId)
        }
    }
}

// UI State (sealed class for type safety)
sealed interface TaskListUiState {
    object Loading : TaskListUiState
    data class Success(val tasks: List<Task>) : TaskListUiState
    data class Error(val message: String) : TaskListUiState
}

// Composable
@Composable
fun TaskListScreen(viewModel: TaskListViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is TaskListUiState.Loading -> LoadingIndicator()
        is TaskListUiState.Success -> TaskList(
            tasks = state.tasks,
            onTaskCompleted = viewModel::onTaskCompleted
        )
        is TaskListUiState.Error -> ErrorMessage(state.message)
    }
}
```

**Source:** [Android Architecture Guide](https://developer.android.com/topic/architecture)

### Pattern 2: Expandable Card with animateContentSize

**What:** Card that expands/collapses with smooth animation using state and animateContentSize()

**When to use:** Task cards that show summary when collapsed, full details when expanded

**Example:**
```kotlin
@Composable
fun ExpandableTaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize() // Key modifier for smooth animation
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Always visible
            TaskTitle(task.title)

            if (task.deadline != null) {
                DeadlineIndicator(task.deadline, task.isHardDeadline)
            }

            // Conditionally visible
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                TaskDetails(task.description)
                TaskActions(onEdit = {}, onDelete = {})
            }
        }
    }
}
```

**Source:** [Jetpack Compose Animation Modifiers](https://developer.android.com/develop/ui/compose/animation/composables-modifiers)

### Pattern 3: Room Database with Foreign Keys and Relationships

**What:** Structured entities with foreign key constraints for data integrity

**When to use:** Task entities that will later support subtasks, dependencies, or related data

**Example:**
```kotlin
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["id"], unique = true)]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val deadline: Instant? = null,
    val isHardDeadline: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Instant? = null,
    val createdAt: Instant = Clock.System.now(),
    val taskType: String = "ONE_TIME" // Future-proofing for Phase 4
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadline ASC")
    fun observeActiveTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :timestamp WHERE id = :taskId")
    suspend fun completeTask(taskId: String, timestamp: Instant = Clock.System.now())
}

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true // Important: enables schema versioning
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
```

**Source:** [Room Database Guide](https://developer.android.com/training/data-storage/room)

### Pattern 4: LazyColumn with Keys for Performance

**What:** Use stable keys in LazyColumn to prevent unnecessary recompositions

**When to use:** Task list rendering

**Example:**
```kotlin
@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = tasks,
            key = { task -> task.id } // Stable key prevents recomposition
        ) { task ->
            ExpandableTaskCard(
                task = task,
                onTaskClick = { onTaskClick(task) }
            )
        }
    }
}
```

**Source:** [Jetpack Compose Performance Best Practices](https://developer.android.com/develop/ui/compose/performance/bestpractices)

### Pattern 5: Haptic Feedback on User Actions

**What:** Use View-based performHapticFeedback() for tactile confirmation

**When to use:** Task completion, button presses, important interactions

**Example:**
```kotlin
@Composable
fun TaskCheckbox(
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Checkbox(
        checked = isCompleted,
        onCheckedChange = { checked ->
            // Perform haptic feedback before state change
            view.performHapticFeedback(
                if (checked) HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.VIRTUAL_KEY
            )
            onCheckedChange(checked)
        },
        modifier = modifier
    )
}
```

**Source:** [Add haptic feedback to events](https://developer.android.com/develop/ui/views/haptics/haptic-feedback)

### Anti-Patterns to Avoid

- **Backwards writes:** Never modify state that has already been read in the same composition (causes infinite recomposition loops)
- **Heavy computation in composables:** Use `remember` or move to ViewModel; composables recompose frequently
- **LazyColumn without keys:** Causes unnecessary recomposition when items move or update
- **Reading state too early:** Use lambda modifiers (`offset {}` not `offset()`) to defer state reads to later phases
- **Foreign keys without indices:** Room requires parent indices; also create child indices to avoid full table scans
- **Destructive migrations in production:** Only use `fallbackToDestructiveMigration()` during development

## Don't Hand-Roll

Problems that look simple but have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Date/time calculations | Custom deadline math | kotlinx-datetime with `DateTimeUnit.DateBased` | Handles DST transitions, leap years, time zone edge cases |
| Dependency injection | Manual singleton patterns | Koin | Handles lifecycle, testing, scoping automatically |
| Navigation state | Custom back stack | Navigation Compose | Type-safe args, deep links, saved state, back handling |
| Database migrations | Manual SQL scripts without testing | Room Migration + testing helpers | Compile-time verification, migration testing support |
| Task ordering | Custom sorting logic in UI | Room queries with ORDER BY, derived state | Database-level sorting is faster and reactive |
| Empty state detection | Manual isEmpty checks | derivedStateOf with Flow operators | Prevents recomposition on every item change |
| Haptic feedback patterns | Vibrator.vibrate() with timings | HapticFeedbackConstants | System-consistent, respects user settings, no permission needed |

**Key insight:** Android's official libraries handle edge cases you won't think of until production (configuration changes, process death, large datasets, accessibility). Use them from the start rather than discovering why you need them later.

## Common Pitfalls

### Pitfall 1: Not Planning for Database Migrations from Version 1

**What goes wrong:** Apps crash when schema changes, or worse, use `fallbackToDestructiveMigration()` and delete user data

**Why it happens:** Developers assume they can add migrations "later" but Room enforces version changes immediately

**How to avoid:**
1. Set `exportSchema = true` in `@Database` annotation from the start
2. Create a `schemas/` directory in your project to store schema JSON files
3. Write migration tests even for v1→v2 (first migration is where most bugs happen)
4. Never use `fallbackToDestructiveMigration()` in production builds

**Warning signs:**
- No schema export directory configured
- Room database version is still 1 after adding features
- Test database uses `fallbackToDestructiveMigration()`

**Code example:**
```kotlin
// In build.gradle.kts
android {
    defaultConfig {
        // Export Room schemas for version tracking
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}

// In database definition
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true // Critical: enables schema versioning
)
abstract class AppDatabase : RoomDatabase()

// When adding migration later (example)
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN taskType TEXT NOT NULL DEFAULT 'ONE_TIME'")
    }
}
```

**Source:** [Room Database Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)

### Pitfall 2: Task Ordering Logic in UI Instead of Database

**What goes wrong:** Performance degrades with large task lists; ordering breaks when using Flow updates; UI becomes complex and hard to test

**Why it happens:** It's easier to sort a list in Kotlin than write SQL ORDER BY clauses

**How to avoid:**
1. Use Room queries with ORDER BY for all list ordering
2. Let database handle deadline proximity calculations
3. Use CASE WHEN for complex ordering (e.g., overdue first, then by deadline)
4. Expose ordering as a Flow that updates reactively

**Warning signs:**
- `.sortedWith()` or `.sortedBy()` in composables or UI layer
- Task list doesn't update order when deadline approaches
- Sorting logic duplicated across screens

**Code example:**
```kotlin
// ❌ WRONG: Sorting in ViewModel or UI
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    fun observeActiveTasks(): Flow<List<TaskEntity>>
}

class TaskListViewModel {
    val tasks = taskRepository.observeActiveTasks()
        .map { tasks ->
            // Don't do this! Runs on every emission
            tasks.sortedWith(compareBy({ it.deadline }))
        }
}

// ✅ CORRECT: Sorting in database query
@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
        ORDER BY
            CASE
                WHEN deadline IS NULL THEN 1
                ELSE 0
            END,
            deadline ASC
    """)
    fun observeActiveTasks(): Flow<List<TaskEntity>>
}
```

**Source:** Based on Android performance best practices

### Pitfall 3: Not Preserving State Through Configuration Changes

**What goes wrong:** Expanded cards collapse on rotation; scroll position resets; in-progress edits are lost

**Why it happens:** Composables recreate on configuration changes without proper state preservation

**How to avoid:**
1. Use ViewModel for all screen-level state (survives config changes)
2. Use `rememberSaveable` for simple UI state that should survive process death
3. Use `rememberLazyListState()` for scroll position and pass to LazyColumn
4. Save expanded card IDs in ViewModel, not local state

**Warning signs:**
- User complains about losing data on rotation
- Scroll position jumps to top after rotation
- Expanded state or selections reset on screen resize (foldables)

**Code example:**
```kotlin
// ❌ WRONG: Local state doesn't survive config changes
@Composable
fun TaskListScreen() {
    var expandedTaskId by remember { mutableStateOf<String?>(null) }
    // Lost on rotation!
}

// ✅ CORRECT: Store in ViewModel
class TaskListViewModel : ViewModel() {
    private val _expandedTaskId = MutableStateFlow<String?>(null)
    val expandedTaskId: StateFlow<String?> = _expandedTaskId.asStateFlow()

    fun toggleExpanded(taskId: String) {
        _expandedTaskId.value = if (_expandedTaskId.value == taskId) null else taskId
    }
}

// ✅ ALSO CORRECT: Use rememberSaveable for simple cases
@Composable
fun TaskListScreen() {
    var expandedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    // Survives config changes AND process death
}
```

**Source:** [ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state)

### Pitfall 4: Shame-Inducing Language in Task Management UX

**What goes wrong:** Neurodivergent users abandon app due to guilt and anxiety from accusatory messaging ("You missed X tasks!", "Failed", "Overdue!")

**Why it happens:** Traditional productivity apps use punishment/urgency language to motivate action, but this triggers RSD (rejection sensitive dysphoria) in ADHD users

**How to avoid:**
1. Use neutral, factual language: "Task from yesterday" not "Overdue!"
2. Frame actions as choices: "Reschedule?" not "Try again?"
3. Avoid red/urgent colors for overdue tasks; use subtle badges
4. Provide gentle suggestions: "Would you like to move this to today?" not "This task is late!"
5. Empty states should be calm: "Nothing here yet" not "You have no tasks"

**Warning signs:**
- Copy uses words like "failed", "missed", "behind", "late"
- Heavy use of red color or warning icons
- Streak counters or "days since last completion" metrics
- Celebratory language that implies past failures ("Finally finished!")

**Example:**
```kotlin
// ❌ WRONG: Shame-inducing language
@Composable
fun OverdueTaskBadge() {
    Badge(
        containerColor = MaterialTheme.colorScheme.error
    ) {
        Icon(Icons.Filled.Warning, contentDescription = "Overdue!")
        Text("OVERDUE - You're behind!")
    }
}

// ✅ CORRECT: Neutral, factual language
@Composable
fun DeadlineBadge(deadline: Instant) {
    val daysAgo = (Clock.System.now() - deadline).inWholeDays
    Badge(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = when {
                daysAgo == 0L -> "Today"
                daysAgo == 1L -> "Yesterday"
                else -> "$daysAgo days ago"
            },
            style = MaterialTheme.typography.labelSmall
        )
    }
}
```

**Sources:**
- [ADHD Task Management Apps 2026](https://affine.pro/blog/adhd-task-management-apps)
- [Top ADHD Apps for Focus](https://elephas.app/blog/best-productivity-apps-adhd)

### Pitfall 5: Inadequate Scaffold Content Padding with FAB

**What goes wrong:** FAB overlaps the last item in LazyColumn, making it unclickable; content scrolls under the FAB

**Why it happens:** Forgetting to pass Scaffold's padding to LazyColumn's contentPadding

**How to avoid:**
1. Always accept `PaddingValues` parameter in content lambda
2. Pass padding to LazyColumn's `contentPadding` parameter
3. For custom layouts, apply padding to the outermost container

**Warning signs:**
- Last list item is partially hidden behind FAB
- Can't click items near bottom of screen
- Content doesn't scroll far enough to reveal all items

**Code example:**
```kotlin
// ❌ WRONG: Ignoring Scaffold padding
@Composable
fun TaskListScreen() {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(...) } }
    ) { padding -> // padding parameter provided but not used!
        LazyColumn {
            items(tasks) { task ->
                TaskCard(task)
            }
        }
    }
}

// ✅ CORRECT: Applying Scaffold padding to LazyColumn
@Composable
fun TaskListScreen() {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(...) } }
    ) { padding ->
        LazyColumn(
            contentPadding = padding, // Key: pass padding here
            modifier = Modifier.fillMaxSize()
        ) {
            items(tasks) { task ->
                TaskCard(task)
            }
        }
    }
}
```

**Source:** [Using Scaffold in Jetpack Compose](https://medium.com/kotlin-android-chronicle/how-to-use-scaffold-in-jetpack-compose-like-a-real-app-should-033b0fd78dfa)

## Code Examples

Verified patterns from official sources:

### Scaffold with FAB and LazyColumn
```kotlin
@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onFabClick: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nothing here yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

**Source:** [Jetpack Compose Components - FAB](https://developer.android.com/develop/ui/compose/components/fab), [Scaffold Guide](https://medium.com/kotlin-android-chronicle/how-to-use-scaffold-in-jetpack-compose-like-a-real-app-should-033b0fd78dfa)

### Deadline Calculation with kotlinx-datetime
```kotlin
import kotlinx.datetime.*

data class DeadlineInfo(
    val deadline: Instant,
    val isOverdue: Boolean,
    val daysUntil: Int,
    val displayText: String
)

fun calculateDeadlineInfo(deadline: Instant, timeZone: TimeZone): DeadlineInfo {
    val now = Clock.System.now()
    val deadlineDate = deadline.toLocalDateTime(timeZone).date
    val todayDate = now.toLocalDateTime(timeZone).date

    val daysUntil = todayDate.daysUntil(deadlineDate)

    return DeadlineInfo(
        deadline = deadline,
        isOverdue = now > deadline,
        daysUntil = daysUntil,
        displayText = when {
            daysUntil < 0 -> "${-daysUntil} days ago"
            daysUntil == 0 -> "Today"
            daysUntil == 1 -> "Tomorrow"
            else -> "In $daysUntil days"
        }
    )
}

// Usage in ViewModel
class TaskListViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {
    val tasks = taskRepository.observeTasks()
        .map { taskList ->
            val timeZone = TimeZone.currentSystemDefault()
            taskList.map { task ->
                task.copy(
                    deadlineInfo = task.deadline?.let {
                        calculateDeadlineInfo(it, timeZone)
                    }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

**Source:** [kotlinx-datetime Guide](https://github.com/Kotlin/kotlinx-datetime)

### Single StateFlow UI State Pattern
```kotlin
// Define comprehensive UI state
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val expandedTaskId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel
class TaskListViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskListUiState(isLoading = true))
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.observeTasks()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks, isLoading = false, error = null) }
                }
        }
    }

    fun toggleTaskExpanded(taskId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                expandedTaskId = if (currentState.expandedTaskId == taskId) null else taskId
            )
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId)
        }
    }
}

// Composable
@Composable
fun TaskListScreen(viewModel: TaskListViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = { /* FAB */ }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(uiState.error!!)
            uiState.tasks.isEmpty() -> EmptyState()
            else -> LazyColumn(contentPadding = padding) {
                items(uiState.tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        isExpanded = task.id == uiState.expandedTaskId,
                        onExpandToggle = { viewModel.toggleTaskExpanded(task.id) },
                        onComplete = { viewModel.completeTask(task.id) }
                    )
                }
            }
        }
    }
}
```

**Source:** [State Management in Jetpack Compose](https://medium.com/@devarshbhalara3072/state-management-in-jetpack-compose-using-viewmodel-and-stateflow-basic-c4515eb5169d)

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| XML Views | Jetpack Compose | 2021 (stable 2021-07) | Declarative UI, less boilerplate, better performance |
| LiveData | StateFlow | 2020 (Coroutines 1.3.0) | Better structured concurrency, more operators, non-nullable |
| RxJava | Kotlin Coroutines + Flow | 2019 (Coroutines stable) | Native language support, simpler API, less overhead |
| Manual DI / Service Locator | Hilt/Koin | Hilt 2020, Koin 2018 | Compile-time safety, less boilerplate, testability |
| Java Time API (API 26+) | kotlinx-datetime | 2021 (multiplatform) | Multiplatform support, consistent API, better ergonomics |
| Material Design 2 | Material Design 3 | 2022 (stable 2023) | Dynamic color, tonal elevation, modern aesthetics |
| AsyncTask | Coroutines | Deprecated 2019 | Structured concurrency, cancellation, lifecycle awareness |

**Deprecated/outdated:**
- **AsyncTask:** Deprecated, removed in API 30. Use Coroutines.
- **XML Layout inflation:** Not deprecated but Compose is the modern standard for new apps
- **LiveData:** Still supported but StateFlow is preferred for new development
- **RxJava:** Community standard shifted to Coroutines for Android

## Open Questions

Things that couldn't be fully resolved:

1. **Overdue Threshold for Reschedule Suggestions**
   - What we know: CONTEXT.md leaves "overdue for a long time" threshold to Claude's discretion
   - What's unclear: User testing needed to determine optimal threshold (1 week? 2 weeks? 30 days?)
   - Recommendation: Start with 7 days, make configurable later. Test with target users.

2. **Details Field Behavior on New Task Screen**
   - What we know: CONTEXT.md says "Add details" reveals deadline/type options, but doesn't specify if description field is also behind this
   - What's unclear: Whether description field is visible by default or revealed with "Add details"
   - Recommendation: Make title always visible, everything else (including description) behind "Add details" expansion for maximum simplicity

3. **Exact Icon Set Choice**
   - What we know: CONTEXT.md leaves icon choices to Claude; Material 3 provides Material Symbols and Icons.Filled
   - What's unclear: Whether to use filled, outlined, or rounded style; specific icon mappings
   - Recommendation: Use Material Icons Filled for consistency with M3 defaults. Soft deadline = Icons.Filled.Schedule, hard deadline = Icons.Filled.Flag

4. **Archived Task Cleanup Timing**
   - What we know: CONTEXT.md says "Single-time completed tasks clear from Done section after 24 hours"
   - What's unclear: Whether this means database deletion or just hiding from UI; what happens after 24 hours
   - Recommendation: Keep in database with `archivedAt` timestamp, hide from Done section UI after 24 hours. Allows future "View Archive" feature.

## Sources

### Primary (HIGH confidence)

- [Jetpack Compose Components - FAB](https://developer.android.com/develop/ui/compose/components/fab) - FAB variants and usage
- [Jetpack Compose Components - Card](https://developer.android.com/develop/ui/compose/components/card) - Card implementation
- [Jetpack Compose Performance Best Practices](https://developer.android.com/develop/ui/compose/performance/bestpractices) - LazyColumn optimization, state management
- [Android Architecture Guide](https://developer.android.com/topic/architecture) - ViewModel, UDF, repository pattern
- [Room Database Guide](https://developer.android.com/training/data-storage/room) - Room setup and patterns
- [Room Database Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions) - Migration strategy
- [Add haptic feedback to events](https://developer.android.com/develop/ui/views/haptics/haptic-feedback) - Haptic implementation
- [Jetpack Compose Animation Modifiers](https://developer.android.com/develop/ui/compose/animation/composables-modifiers) - animateContentSize usage
- [kotlinx-datetime GitHub](https://github.com/Kotlin/kotlinx-datetime) - Date/time library documentation

### Secondary (MEDIUM confidence)

- [State Management in Jetpack Compose](https://medium.com/@devarshbhalara3072/state-management-in-jetpack-compose-using-viewmodel-and-stateflow-basic-c4515eb5169d) - StateFlow patterns
- [Using Scaffold in Jetpack Compose](https://medium.com/kotlin-android-chronicle/how-to-use-scaffold-in-jetpack-compose-like-a-real-app-should-033b0fd78dfa) - Scaffold and FAB positioning
- [Understanding Room Migrations](https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929) - Migration best practices
- [Expandable Card in Jetpack Compose](https://www.c-sharpcorner.com/article/expandable-card-with-animation-in-jetpack-compose/) - Expandable card implementation
- [ADHD Task Management Apps 2026](https://affine.pro/blog/adhd-task-management-apps) - Shame-free design principles
- [Best Productivity Apps for ADHD in 2026](https://elephas.app/blog/best-productivity-apps-adhd) - Neurodivergent UX patterns
- [Empty State UX Examples](https://www.eleken.co/blog-posts/empty-state-ux) - Empty state design principles

### Tertiary (LOW confidence)

- [Task Management Apps: 10 Common Mistakes](https://captaintime.com/task-management-apps-10-common-mistakes/) - User-side task app mistakes
- [Empty State UI Design](https://www.setproduct.com/blog/empty-state-ui-design) - Visual empty state patterns

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All recommendations from official Google documentation and established Kotlin libraries
- Architecture: HIGH - Official Android architecture guide and verified community patterns
- Pitfalls: HIGH - Based on official documentation and verified developer experience

**Research date:** 2026-01-28
**Valid until:** ~2026-03-28 (60 days) - Stable domain with quarterly Compose BOM updates; revalidate if Compose or Room major versions release
