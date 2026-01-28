# Architecture Research

**Domain:** Neurodivergent-focused task/routine management Android app
**Researched:** 2026-01-28
**Confidence:** HIGH (based on official Android architecture guide and established patterns)

## Recommended Architecture

### Overall Pattern: MVVM + Clean Architecture

```
┌──────────────────────────────────────────┐
│                UI Layer                   │
│  Composables ← ViewModel ← UiState      │
├──────────────────────────────────────────┤
│              Domain Layer                │
│  UseCases (optional, for complex logic)  │
├──────────────────────────────────────────┤
│               Data Layer                 │
│  Repositories ← DAOs ← Room Database    │
└──────────────────────────────────────────┘
```

**Why MVVM + Clean:** Google-recommended for Android. Clear separation of concerns. ViewModel handles state, Repository handles data, Composables handle display. Domain layer is optional — add UseCases only when business logic becomes complex enough to warrant it.

### Component Boundaries

#### UI Layer
- **Composables:** Pure rendering functions. Take state in, emit events out.
- **ViewModels:** One per screen/feature. Hold UiState as StateFlow. Call repositories/use cases. Scoped to navigation destination (not shared across screens).
- **UiState:** Sealed classes/data classes representing screen state.

#### Domain Layer (Optional)
- **UseCases:** Encapsulate complex business logic used by multiple ViewModels.
- Examples where useful: `GetSmartSuggestedTasksUseCase`, `CalculateNextCooldownUseCase`, `CheckDependencyChainUseCase`
- Skip for simple CRUD operations — ViewModel calls Repository directly.

#### Data Layer
- **Repositories:** Single source of truth for each data type. Expose Flow for reactive updates. Handle data transformations.
- **DAOs:** Room data access objects. Raw database operations.
- **Entities:** Room database entities. Mapped to domain models by repository.

### Data Model — Task Type Polymorphism

The app has distinct task types with different behaviors. Two viable approaches:

#### Recommended: Single Table + Type Discriminator

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val type: TaskType,          // ONE_TIME, QUOTA, COOLDOWN, RECURRING
    val category: String?,
    val createdAt: Instant,
    val updatedAt: Instant,

    // One-time task fields
    val softDeadline: Instant?,
    val hardDeadline: Instant?,

    // Quota task fields (nullable for non-quota types)
    val quotaTarget: Int?,       // e.g., 2
    val quotaPeriod: Period?,    // e.g., WEEKLY

    // Cooldown task fields
    val cooldownDuration: Duration?,  // e.g., 8 hours

    // Recurring task fields
    val recurrenceRule: String?,  // e.g., "DAILY", "WEEKLY", "MONTHLY", "YEARLY"

    // Common fields
    val isArchived: Boolean = false,
    val parentTaskId: Long? = null,  // for subtasks
    val sortOrder: Int = 0,
    val dependencyVisibility: DependencyVisibility = DependencyVisibility.GREYED_OUT,
)

enum class TaskType { ONE_TIME, QUOTA, COOLDOWN, RECURRING }
enum class DependencyVisibility { HIDDEN, GREYED_OUT }
```

**Why single table:** Task types share most fields. Queries across all types are common (focus view, category view). Simpler joins. Room handles nullable columns well. Avoids multi-table queries for mixed-type lists.

**Separate tables for relationships:**

```kotlin
@Entity(tableName = "completions")
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val completedAt: Instant,
    val note: String?,
)

@Entity(
    tableName = "dependencies",
    primaryKeys = ["taskId", "dependsOnId"]
)
data class DependencyEntity(
    val taskId: Long,       // this task
    val dependsOnId: Long,  // depends on this task
)
```

### Navigation Architecture

**Single Activity + Compose Navigation:**

```
MainActivity (only Activity)
└── NavHost
    ├── TaskListScreen (home/focus view)
    ├── TaskDetailScreen (view/edit task)
    ├── QuickCaptureScreen (minimal capture UI)
    ├── CompletionHistoryScreen
    └── SettingsScreen
```

- One NavController manages all screens
- Type-safe navigation routes using Kotlin serialization
- Deep links for widget/notification taps
- SavedStateHandle for process death restoration

### State Management

```
User Action → ViewModel → Repository → Room DAO
                ↑                          ↓
            UiState ← Flow ← Room Query (reactive)
```

- **StateFlow** in ViewModels for UI state
- **Room returns Flow** for reactive database queries — UI updates automatically when data changes
- **SavedStateHandle** for surviving process death (stores IDs and filter state, not full data)
- **No LiveData** — StateFlow is the modern replacement

### Data Flow for Key Features

#### Quick Capture
```
QuickCaptureScreen → TaskViewModel.quickAdd(title) → TaskRepository.insert(
    TaskEntity(title=title, type=ONE_TIME, ...)  // minimal defaults
) → Room inserts → Flow emits → TaskList updates
```

#### Focus View (Smart Suggestions)
```
FocusViewModel collects from:
├── TaskRepository.getActiveTasks()      // all non-archived
├── CompletionRepository.getTodayCompletions()
└── DependencyRepository.getBlockedTaskIds()

Algorithm: Filter blocked → Sort by urgency/deadline → Take top N
```

#### Cooldown Timer Check
```
CooldownUseCase:
1. Get task's last completion from CompletionRepository
2. Calculate: lastCompletion + cooldownDuration
3. If now > cooldownTime → available
4. Else → show countdown remaining
```

#### Quota Progress
```
QuotaUseCase:
1. Get completions in current period (this week/month)
2. Count completions for this task
3. Return: completed / quotaTarget (e.g., 1/3)
```

### Suggested Build Order

```
Phase 1: Foundation
├── Project setup (Gradle, dependencies, theme)
├── Room database + entities + DAOs
├── Repository layer
└── Basic navigation shell

Phase 2: Core Task Management
├── Quick capture screen
├── Task list screen (simple list)
├── Task detail/edit screen
└── Basic CRUD operations

Phase 3: Task Types
├── One-time tasks with deadlines
├── Recurring tasks (daily/weekly/monthly/yearly)
├── Completion tracking + history
└── Task type-specific UI components

Phase 4: Organization & Focus
├── Categories + filtering
├── Focus view with smart suggestions
├── Subtasks + dependency system
└── Task splitting/detaching

Phase 5: Advanced Types
├── Quota-based tasks
├── Cooldown timer tasks
└── Completion statistics

Phase 6: Polish
├── Home screen widget
├── Theming (Material You dynamic colors)
├── Data export/backup
└── Settings screen
```

### Key Architectural Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| ViewModel scope | Per-screen, not shared | Avoids god-ViewModels. Shared data goes through repositories. |
| Domain layer | Optional, add as needed | Start simple. Add UseCases when ViewModel logic gets complex. |
| Task type modeling | Single table + discriminator | Simpler queries for mixed-type lists. Shared fields reduce duplication. |
| Reactive data | Room Flow → StateFlow | Automatic UI updates when database changes. Single source of truth. |
| Dependency injection | Koin | Lightweight, KMP-compatible, no annotation processing. |
| Process death | SavedStateHandle + Room | Room IS the persistence. SavedStateHandle for transient UI state (scroll position, filters). |

### Module Structure (Initial)

Start with a single module, extract later if complexity warrants:

```
app/
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── entities/
│   │   └── dao/
│   └── repository/
├── domain/
│   └── usecase/          # add as needed
├── ui/
│   ├── theme/
│   ├── components/       # shared composables
│   ├── tasklist/
│   ├── taskdetail/
│   ├── quickcapture/
│   ├── history/
│   └── settings/
├── di/                   # Koin modules
├── navigation/
└── util/
```

**When to extract modules:** If build times exceed 30 seconds or if preparing for KMP shared module. Until then, package-level separation is sufficient.

## Sources

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Data Layer Guide](https://developer.android.com/topic/architecture/data-layer)
- [Domain Layer Guide](https://developer.android.com/topic/architecture/domain-layer)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation)
- [Save UI States](https://developer.android.com/topic/libraries/architecture/saving-states)
- [Room Polymorphism Patterns](https://commonsware.com/AndroidArch/pages/chap-roompoly-002)
- [ViewModel SavedState](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate)
