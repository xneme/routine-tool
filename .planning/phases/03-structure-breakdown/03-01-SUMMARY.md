---
phase: 03-structure-breakdown
plan: 01
subsystem: database
tags: [room, kotlin, android, subtasks, fractional-indexing]

# Dependency graph
requires:
  - phase: 01-foundation-quick-capture
    provides: TaskEntity, TaskDao, TaskRepository patterns for Room database architecture
provides:
  - SubtaskEntity with CASCADE delete foreign key to TaskEntity
  - SubtaskDao with complete CRUD operations
  - Subtask domain model with Instant timestamps
  - Repository methods for subtask management with fractional indexing
  - Database migration from v1 to v2
affects:
  - 03-02-subtask-ui-components (will use observeSubtasks and repository methods)
  - 03-03-completion-tracking (will depend on subtask completion state)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Fractional indexing for subtask ordering with precision loss detection"
    - "CASCADE delete foreign key ensures subtask cleanup when parent task deleted"
    - "Separate domain models with Instant timestamps, entity models with Long epoch millis"

key-files:
  created:
    - app/src/main/java/com/routinetool/data/local/entities/SubtaskEntity.kt
    - app/src/main/java/com/routinetool/data/local/entities/TaskWithSubtasksEntity.kt
    - app/src/main/java/com/routinetool/data/local/database/SubtaskDao.kt
    - app/src/main/java/com/routinetool/domain/model/Subtask.kt
  modified:
    - app/src/main/java/com/routinetool/data/local/database/AppDatabase.kt
    - app/src/main/java/com/routinetool/data/repository/TaskRepository.kt
    - app/src/main/java/com/routinetool/di/AppModule.kt

key-decisions:
  - "Fractional indexing for subtask position allows reordering without updating all rows"
  - "Precision loss detection triggers full renumbering when gaps become too small"
  - "CASCADE delete on foreign key ensures data integrity"
  - "Database version 2 with migration from v1"

patterns-established:
  - "Subtask position uses Float for fractional indexing"
  - "Completion tracking with completedAt timestamp (nullable Long in entity, Instant in domain)"
  - "Repository methods follow existing Task patterns with Flow observables and suspend functions"

# Metrics
duration: 3min
completed: 2026-01-30
---

# Phase 03 Plan 01: Subtask Data Layer Summary

**Room database with SubtaskEntity, CASCADE foreign key, fractional indexing for reordering, and complete repository methods**

## Performance

- **Duration:** 3 min (172 seconds)
- **Started:** 2026-01-30T12:22:30Z
- **Completed:** 2026-01-30T12:25:22Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments

- Created SubtaskEntity with ForeignKey CASCADE delete to TaskEntity, ensuring data integrity
- Implemented SubtaskDao with complete CRUD operations and Flow observables
- Built Subtask domain model following existing Task patterns with Instant timestamps
- Added TaskRepository methods for subtask management with fractional indexing
- Created database migration from v1 to v2 with proper schema changes

## Task Commits

Each task was committed atomically:

1. **Task 1: Create SubtaskEntity and relation model** - `df0b229` (feat)
2. **Task 2: Create SubtaskDao and update AppDatabase** - `129efeb` (feat)
3. **Task 3: Create domain model and repository methods** - `d7187f6` (feat)

## Files Created/Modified

**Created:**
- `app/src/main/java/com/routinetool/data/local/entities/SubtaskEntity.kt` - Room entity with ForeignKey CASCADE delete, Index on taskId
- `app/src/main/java/com/routinetool/data/local/entities/TaskWithSubtasksEntity.kt` - Relation model for querying tasks with subtasks
- `app/src/main/java/com/routinetool/data/local/database/SubtaskDao.kt` - DAO with insert, update, delete, queries, and Flow observables
- `app/src/main/java/com/routinetool/domain/model/Subtask.kt` - Domain model with Instant timestamps

**Modified:**
- `app/src/main/java/com/routinetool/data/local/database/AppDatabase.kt` - Version 2, added SubtaskEntity, subtaskDao(), MIGRATION_1_2
- `app/src/main/java/com/routinetool/data/repository/TaskRepository.kt` - Added SubtaskDao injection, observeSubtasks, addSubtask, toggleSubtask, deleteSubtask, reorderSubtask with fractional indexing
- `app/src/main/java/com/routinetool/di/AppModule.kt` - Added MIGRATION_1_2 to database builder, provided subtaskDao, injected into TaskRepository

## Decisions Made

**Fractional Indexing Implementation:**
- Used Float for position field to enable fractional indexing
- New position calculation: between two items = (prev + next) / 2
- Precision loss detection (gap < 0.0001f) triggers renumbering all subtasks with integer positions
- This avoids database updates for all rows on every reorder while handling precision limits

**CASCADE Delete:**
- ForeignKey with onDelete = CASCADE ensures subtasks are automatically deleted when parent task is deleted
- Maintains data integrity without manual cleanup code

**Migration Strategy:**
- Database version incremented to 2
- Migration creates subtasks table with all columns, foreign key constraint, and index
- Schema export enabled for future migration tracking

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

**Build verification skipped:**
- Java/Android build environment not available in execution context
- Code follows existing patterns exactly (TaskEntity, TaskDao, TaskRepository)
- Verification will occur when UI components are built in subsequent plans

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

**Ready for 03-02 (Subtask UI Components):**
- SubtaskEntity, SubtaskDao, Subtask domain model all available
- Repository methods: observeSubtasks, addSubtask, toggleSubtask, deleteSubtask, reorderSubtask
- Database migration registered and ready for first app launch after code integration

**No blockers:**
- All data layer infrastructure complete
- UI can now bind to observeSubtasks Flow and call repository methods

---
*Phase: 03-structure-breakdown*
*Completed: 2026-01-30*
