# Phase 02: Organization & Focus - Research

**Researched:** 2026-01-29
**Domain:** Jetpack Compose UI patterns, StateFlow filtering/sorting, DataStore preferences
**Confidence:** HIGH

## Summary

Phase 02 adds filtering, sorting, and focus view capabilities to the existing task list. The standard approach leverages Material 3 components (FilterChip, DropdownMenu), StateFlow transformations in the ViewModel for filtering/sorting logic, and Preferences DataStore for persisting sort preferences. Progressive disclosure uses AnimatedVisibility with expandVertically transitions. Focus view is implemented as a separate full-screen composable with custom task selection logic.

**Key architectural decisions:**
- Filtering/sorting logic belongs in the ViewModel, not the DAO layer (keeps Room queries simple)
- Use `combine()` operator to merge filter state with task data
- Sort preference persists via DataStore; filter state does NOT persist (per CONTEXT.md)
- Focus view uses hybrid selection: algorithm suggests tasks, user can pin/unpin overrides

**Primary recommendation:** Build filtering and sorting as StateFlow transformations in the ViewModel layer. Use Material 3 FilterChip and DropdownMenu components. Store only sort preference in DataStore. Implement focus view as separate screen with navigation.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Compose Material3 | 2024.12.01 (BOM) | FilterChip, DropdownMenu, ExtendedFAB | Official Material Design 3 components, actively maintained |
| DataStore Preferences | 1.2.0 | Persist sort preference | Modern replacement for SharedPreferences, coroutine-based |
| Kotlinx Coroutines | 1.9.0 | Flow operators (combine, map) | Essential for reactive data transformations |
| Room KTX | 2.7.0 | Database queries with Flow | Already in use, supports Flow observables |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Compose Animation | 2024.12.01 (BOM) | AnimatedVisibility, animateContentSize | Progressive disclosure, smooth transitions |
| Navigation Compose | 2.8.5 | Navigate to focus view | Already in use for multi-screen navigation |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| DataStore | SharedPreferences | DataStore is async/coroutine-based, no ANR risk, officially recommended replacement |
| StateFlow filtering | Room @RawQuery | @RawQuery adds SQL complexity; StateFlow filtering keeps business logic in ViewModel |
| FilterChip | Custom toggles | FilterChip provides Material 3 theming, accessibility, animations out-of-box |

**Installation:**
All dependencies already present in project. No new additions required.

## Architecture Patterns

### Recommended ViewModel Structure

```kotlin
class TaskListViewModel(
    private val repository: TaskRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    // Sort preference from DataStore
    private val sortPreference: StateFlow<SortOption> =
        preferencesDataStore.sortPreference
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOption.URGENCY)

    // Filter state (does NOT persist)
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Combine repository data with filters/sorting
    val uiState: StateFlow<TaskListUiState> = combine(
        repository.observeActiveTasks(),
        repository.observeRecentlyCompleted(),
        filterState,
        sortPreference
    ) { active, completed, filters, sort ->
        val filtered = applyFilters(active, filters)
        val sorted = applySort(filtered, sort)
        TaskListUiState(tasks = sorted, ...)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskListUiState())
}
```

**Why this pattern:**
- Single source of truth for UI state
- Reactive: UI updates automatically when any input changes
- Separation of concerns: filtering logic in ViewModel, not DAO
- Testable: pure functions for filtering/sorting

### Pattern 1: FilterChip for Multi-Select Filters

**What:** Horizontal scrollable row of selectable chips for filtering dimensions
**When to use:** Multiple independent filter categories (Status, Deadline Type)

**Example:**
```kotlin
// Source: https://developer.android.com/develop/ui/compose/components/chip
@Composable
fun FilterChipRow(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = filterState.showActive,
                onClick = { onFilterChange(filterState.copy(showActive = !filterState.showActive)) },
                label = { Text("Active") },
                leadingIcon = if (filterState.showActive) {
                    { Icon(Icons.Filled.Done, null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }
        // Repeat for other filters...
    }
}
```

### Pattern 2: DropdownMenu for Sort Options

**What:** Icon button triggers dropdown with sort options
**When to use:** Single-select from list of options (sort by Urgency, Deadline, Creation Date)

**Example:**
```kotlin
// Source: https://developer.android.com/develop/ui/compose/components/menu
@Composable
fun SortDropdown(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Sort, "Sort options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    },
                    leadingIcon = if (currentSort == option) {
                        { Icon(Icons.Filled.Check, null) }
                    } else null
                )
            }
        }
    }
}
```

### Pattern 3: DataStore for Sort Preference Persistence

**What:** Save/load single sort preference across app sessions
**When to use:** User preference that should survive app restart

**Example:**
```kotlin
// Source: https://developer.android.com/topic/libraries/architecture/datastore
class PreferencesDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val SORT_PREFERENCE_KEY = stringPreferencesKey("sort_preference")

    val sortPreference: Flow<SortOption> = context.dataStore.data.map { prefs ->
        prefs[SORT_PREFERENCE_KEY]?.let { SortOption.valueOf(it) } ?: SortOption.URGENCY
    }

    suspend fun saveSortPreference(option: SortOption) {
        context.dataStore.edit { prefs ->
            prefs[SORT_PREFERENCE_KEY] = option.name
        }
    }
}
```

### Pattern 4: Progressive Disclosure with AnimatedVisibility

**What:** Expandable sections in task edit screen (Notes, Deadline options)
**When to use:** Secondary content that users may not need every time

**Example:**
```kotlin
// Source: https://developer.android.com/develop/ui/compose/animation/composables-modifiers
@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
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
```

### Pattern 5: Focus View as Separate Screen

**What:** Full-screen focus mode showing limited priority tasks
**When to use:** User wants distraction-free view of top tasks

**Example:**
```kotlin
// Navigation setup
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = NavRoutes.TASK_LIST) {
        composable(NavRoutes.TASK_LIST) { TaskListScreen(onNavigateToFocus = { navController.navigate(NavRoutes.FOCUS_VIEW) }) }
        composable(NavRoutes.FOCUS_VIEW) { FocusViewScreen(onBack = { navController.popBackStack() }) }
    }
}

// FocusViewScreen composable
@Composable
fun FocusViewScreen(
    viewModel: FocusViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(uiState.focusTasks, key = { it.id }) { task ->
                FocusTaskCard(task)
            }
        }
    }
}
```

### Anti-Patterns to Avoid

- **Filtering in Room queries with @RawQuery:** Adds complexity, makes testing harder. Use StateFlow transformations instead.
- **Storing filter state in DataStore:** Per CONTEXT.md, filters reset on app open. Persisting creates unwanted behavior.
- **Nested scrollables (LazyRow in LazyColumn without fixed height):** Causes IllegalStateException. Use LazyRow for filters with explicit height.
- **Forgetting `key` parameter in LazyColumn items:** Breaks reordering animations, causes state loss.
- **Using mutable collections as StateFlow values:** Compose won't detect changes. Use immutable lists (List, not MutableList).

## Don't Hand-Roll

Problems that look simple but have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Preference storage | Custom file I/O, SharedPreferences wrapper | Preferences DataStore | Type-safe, async, no ANR risk, official replacement |
| Sort dropdown UI | Custom popup with positioning logic | Material 3 DropdownMenu | Auto-positioning, accessibility, theming, keyboard nav |
| Filter chip UI | Custom toggle buttons | Material 3 FilterChip | Built-in selected state, animations, icon sizing, a11y |
| Expandable sections | Custom height animations | AnimatedVisibility with expandVertically | Smooth transitions, interpolation, composition-aware |
| FAB with actions | Multi-button layout | ExtendedFloatingActionButton or custom menu | Material 3 spec compliance, predictable UX |

**Key insight:** Material 3 components handle edge cases (RTL layouts, accessibility, dark mode, dynamic color) that custom solutions miss. Use official components unless design requires truly custom behavior.

## Common Pitfalls

### Pitfall 1: StateFlow Deduplication Breaking Filter Updates

**What goes wrong:** StateFlow filters duplicate values with `equals()`. If filter state is a data class and you update a field then revert it, StateFlow won't emit the reverted value.

**Why it happens:** StateFlow's built-in deduplication compares new value to previous. If they're equal, no emission occurs.

**How to avoid:**
- Use distinct filter state objects (don't mutate, create new instances)
- If you need every update, use SharedFlow instead (but loses "latest value" behavior)
- For UI state, StateFlow deduplication is usually desired—prevents unnecessary recomposition

**Warning signs:** Filters seem "stuck" after toggling on/off multiple times

### Pitfall 2: Unstable Parameters Causing Full List Recomposition

**What goes wrong:** Passing `List<Task>` directly to composables causes every item to recompose when list changes, even if individual items are unchanged.

**Why it happens:** Compose marks List/Map/Set as unstable (potentially mutable). Unstable parameters skip smart recomposition.

**How to avoid:**
- Provide stable `key` parameter to `items()` in LazyColumn
- Use immutable collections (kotlinx.collections.immutable)
- Mark domain models as `@Stable` if they're truly immutable

**Example:**
```kotlin
// ✅ Good: Stable keys prevent unnecessary recomposition
LazyColumn {
    items(tasks, key = { it.id }) { task ->
        TaskCard(task)
    }
}

// ❌ Bad: No keys, all items recompose
LazyColumn {
    items(tasks) { task ->
        TaskCard(task)
    }
}
```

**Warning signs:** Laggy scrolling, all tasks flash when one changes, profiler shows excessive recomposition

### Pitfall 3: Forgetting to Convert Flow to StateFlow in combine()

**What goes wrong:** `combine()` waits for all flows to emit at least once. If a regular Flow never emits, combined StateFlow never updates.

**Why it happens:** Regular Flows are cold (don't emit until collected). StateFlow has an initial value.

**How to avoid:**
- Convert Flows to StateFlow with `.stateIn(scope, SharingStarted.WhileSubscribed(5000), initialValue)`
- Ensure all combined flows have initial values
- Use `SharingStarted.WhileSubscribed(5000)` to keep alive during config changes

**Example:**
```kotlin
// ✅ Good: All flows converted to StateFlow
val uiState = combine(
    repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()),
    filterState,
    sortState
) { tasks, filters, sort -> ... }

// ❌ Bad: Repository flow is cold, combine may not emit
val uiState = combine(
    repository.tasks,  // Cold Flow, might not emit immediately
    filterState,
    sortState
) { tasks, filters, sort -> ... }
```

**Warning signs:** UI state never updates, or updates only after user interaction

### Pitfall 4: DataStore Blocking Reads on Main Thread

**What goes wrong:** Calling `runBlocking { dataStore.data.first() }` on main thread causes ANR (Application Not Responding).

**Why it happens:** DataStore is async-first. Blocking reads defeat the purpose and freeze UI.

**How to avoid:**
- Always use Flow collection or suspend functions
- Preload data in ViewModel's `init` block with `viewModelScope.launch`
- Collect as StateFlow in ViewModel, expose to UI layer

**Example:**
```kotlin
// ✅ Good: Async preload in ViewModel
class TaskListViewModel(dataStore: PreferencesDataStore) : ViewModel() {
    val sortPreference = dataStore.sortPreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOption.URGENCY)
}

// ❌ Bad: Blocking read
val sortOption = runBlocking { dataStore.sortPreference.first() }  // ANR risk!
```

**Warning signs:** App freezes briefly on launch, strict mode warnings, ANR crash reports

### Pitfall 5: Expansion State Not Persisting Between Edits

**What goes wrong:** Per CONTEXT.md, expansion state should persist between edits. If state is only in composable, navigating away/back resets it.

**Why it happens:** Composable state (`remember { mutableStateOf() }`) is lost on navigation.

**How to avoid:**
- Store expansion state in DataStore for cross-session persistence
- Use `rememberSaveable` for configuration change survival only
- Load expansion state in ViewModel, expose as StateFlow

**Example:**
```kotlin
// ✅ Good: Persistent expansion state
class AddTaskViewModel(dataStore: PreferencesDataStore) : ViewModel() {
    val notesExpanded = dataStore.notesExpanded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleNotesExpanded() {
        viewModelScope.launch {
            dataStore.saveNotesExpanded(!notesExpanded.value)
        }
    }
}

// ❌ Bad: Resets on navigation
@Composable
fun AddTaskScreen() {
    var notesExpanded by remember { mutableStateOf(false) }
    // Lost when navigating away
}
```

**Warning signs:** User expands section, navigates away, comes back, section is collapsed again

## Code Examples

Verified patterns from official sources:

### Filtering Tasks with StateFlow

```kotlin
// Source: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    data class FilterState(
        val showActive: Boolean = true,
        val showDone: Boolean = true,
        val showOverdue: Boolean = true,
        val deadlineTypes: Set<DeadlineType> = setOf(DeadlineType.SOFT, DeadlineType.HARD, DeadlineType.NONE)
    )

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    fun updateFilter(update: FilterState.() -> FilterState) {
        _filterState.update { it.update() }
    }

    val filteredTasks: StateFlow<List<Task>> = combine(
        repository.observeAllTasks(),
        filterState
    ) { tasks, filters ->
        tasks.filter { task ->
            // Status filtering
            val matchesStatus = (filters.showActive && !task.isCompleted) ||
                               (filters.showDone && task.isCompleted) ||
                               (filters.showOverdue && task.isOverdue)

            // Deadline type filtering
            val deadlineType = when {
                task.hardDeadline != null -> DeadlineType.HARD
                task.softDeadline != null -> DeadlineType.SOFT
                else -> DeadlineType.NONE
            }
            val matchesDeadlineType = deadlineType in filters.deadlineTypes

            matchesStatus && matchesDeadlineType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

### Sorting with Enum and DataStore Persistence

```kotlin
// Source: Combined from DataStore and StateFlow docs
enum class SortOption(val displayName: String) {
    URGENCY("Urgency (Overdue First)"),
    DEADLINE("Deadline Proximity"),
    CREATED("Creation Date");

    fun comparator(): Comparator<Task> = when (this) {
        URGENCY -> compareBy(
            { task -> if (task.isOverdue) 0 else 1 },
            { task -> task.nearestDeadline ?: Long.MAX_VALUE }
        )
        DEADLINE -> compareBy { it.nearestDeadline ?: Long.MAX_VALUE }
        CREATED -> compareByDescending { it.createdAt }
    }
}

class TaskListViewModel(
    private val repository: TaskRepository,
    private val dataStore: PreferencesDataStore
) : ViewModel() {

    private val sortOption = dataStore.sortPreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOption.URGENCY)

    fun setSortOption(option: SortOption) {
        viewModelScope.launch {
            dataStore.saveSortPreference(option)
        }
    }

    val sortedTasks = combine(filteredTasks, sortOption) { tasks, sort ->
        tasks.sortedWith(sort.comparator())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

### Focus View Selection Algorithm

```kotlin
// Custom implementation (no official source, domain-specific)
class FocusViewModel(
    private val repository: TaskRepository,
    private val dataStore: PreferencesDataStore
) : ViewModel() {

    data class FocusConfig(
        val taskLimit: Int = 5,
        val pinnedTaskIds: Set<String> = emptySet()
    )

    private val focusConfig = dataStore.focusConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusConfig())

    val focusTasks: StateFlow<List<Task>> = combine(
        repository.observeActiveTasks(),
        focusConfig
    ) { allTasks, config ->
        val pinned = allTasks.filter { it.id in config.pinnedTaskIds }
        val remainingSlots = config.taskLimit - pinned.size

        if (remainingSlots > 0) {
            val autoSelected = allTasks
                .filterNot { it.id in config.pinnedTaskIds }
                .sortedWith(urgencyComparator())
                .take(remainingSlots)

            pinned + autoSelected
        } else {
            pinned.take(config.taskLimit)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePin(taskId: String) {
        viewModelScope.launch {
            val current = focusConfig.value.pinnedTaskIds
            val updated = if (taskId in current) {
                current - taskId
            } else {
                current + taskId
            }
            dataStore.saveFocusConfig(focusConfig.value.copy(pinnedTaskIds = updated))
        }
    }

    private fun urgencyComparator() = compareBy<Task>(
        { if (it.isOverdue) 0 else 1 },  // Overdue first
        { it.nearestDeadline ?: Long.MAX_VALUE }  // Then by deadline
    )
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| SharedPreferences | Preferences DataStore | 2020 (DataStore 1.0 released) | Async/await, no ANR risk, type-safe keys |
| Room dynamic queries with @RawQuery | StateFlow transformations in ViewModel | Ongoing shift | Cleaner separation, testable logic outside DAO |
| Custom filter UI | Material 3 FilterChip | 2022 (Material 3 stable) | Built-in animations, accessibility, theming |
| animateContentSize only | AnimatedVisibility + expandVertically | 2021 (Compose 1.0) | More control over enter/exit transitions |
| Manual FAB menu positioning | Material 3 Expressive FAB Menu | 2025 (Material 3 Expressive) | Official component, spec-compliant animations |

**Deprecated/outdated:**
- **SharedPreferences**: Still functional but official docs recommend DataStore for new code
- **Compose Animation API (1.0)**: Replaced by more powerful `AnimatedVisibility` with enter/exit specs
- **Manual DropdownMenu positioning**: Now auto-positioned by Material 3 components

## Open Questions

Things that couldn't be fully resolved:

1. **Material 3 Expressive FAB Menu availability**
   - What we know: Announced at Google I/O 2025, FloatingActionButtonMenu composable exists
   - What's unclear: Included in standard Compose BOM 2024.12.01 or requires separate dependency?
   - Recommendation: Check BOM release notes. If unavailable, use ExtendedFloatingActionButton + DropdownMenu as fallback. Custom FAB menu not needed—standard components suffice.

2. **Focus view task limit default**
   - What we know: CONTEXT.md says user can configure limit, but doesn't specify default
   - What's unclear: Ideal starting value for neurodivergent users (target audience)
   - Recommendation: Start with 5 tasks (typical human working memory limit). User can adjust. Research shows 5±2 is cognitive comfort zone.

3. **Filter chip visual styling preference**
   - What we know: CONTEXT.md marks as Claude's discretion
   - What's unclear: Use ElevatedFilterChip or standard FilterChip?
   - Recommendation: Use standard FilterChip (flat). Elevated version is visually heavier; flat aligns with "calm and minimal" UI philosophy from PROJECT.md.

## Sources

### Primary (HIGH confidence)
- [Chip | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/components/chip) - FilterChip API and examples (2026-01-26 UTC)
- [Menus | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/components/menu) - DropdownMenu patterns (2026-01-26 UTC)
- [DataStore | Android Developers](https://developer.android.com/topic/libraries/architecture/datastore) - Preferences DataStore setup and usage
- [Animation modifiers and composables | Jetpack Compose](https://developer.android.com/develop/ui/compose/animation/composables-modifiers) - AnimatedVisibility, expandVertically (2026-01-19 UTC)
- [Lists and grids | Jetpack Compose](https://developer.android.com/develop/ui/compose/lists) - LazyColumn best practices (2026-01-19 UTC)
- [Floating action button | Jetpack Compose](https://developer.android.com/develop/ui/compose/components/fab) - FAB types and implementation (2026-01-26 UTC)
- [StateFlow and SharedFlow | Android Developers](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) - StateFlow patterns

### Secondary (MEDIUM confidence)
- [Stop Using SharedPreferences: Mastering Jetpack DataStore in 2026](https://medium.com/@kemal_codes/stop-using-sharedpreferences-mastering-jetpack-datastore-in-2026-b88b2db50e91) - DataStore best practices
- [Sorting and Filtering Records Using Room Database and Kotlin Flow](https://medium.com/better-programming/sorting-and-filtering-records-using-room-database-and-kotlin-flow-c64ccdb39deb) - StateFlow filtering patterns
- [Implementing a Dynamic Query with Room](https://medium.com/@rowaido.game/implementing-a-dynamic-query-with-room-a8e052cff13b) - @RawQuery annotation usage

### Tertiary (LOW confidence)
- [Discovering Material 3 Expressive — FAB Menu](https://medium.com/@renaud.mathieu/discovering-material-3-expressive-fab-menu-ecfae766a946) - FAB Menu component (availability unconfirmed in BOM 2024.12.01)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All libraries verified in official docs with version numbers from project's gradle files
- Architecture: HIGH - Patterns sourced from official Android Developers docs updated Jan 2026
- Pitfalls: MEDIUM - Mix of official docs and community experience, cross-referenced with multiple sources

**Research date:** 2026-01-29
**Valid until:** 2026-02-28 (30 days for stable ecosystem)
