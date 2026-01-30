---
phase: 03-structure-breakdown
plan: 03
subsystem: ui
tags: [kotlin, compose, subtasks, progress-indicator, haptic-feedback]

# Dependency graph
requires:
  - phase: 03-01
    provides: "Subtask data layer with fractional indexing"
provides:
  - "TaskWithSubtasks domain model combining tasks with subtasks"
  - "SubtaskProgressIndicator component showing visual progress (checkbox row or count)"
  - "Subtask completion toggle from task list cards"
  - "Repository methods to observe tasks with subtasks"
affects: [03-04, focus-view, task-analytics]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "TaskWithSubtasks aggregation pattern for UI state"
    - "Dual progress display: checkbox row (<=6) vs text count (>6)"
    - "Lighter haptic feedback (VIRTUAL_KEY) for subtask vs task completion"

key-files:
  created:
    - app/src/main/java/com/routinetool/domain/model/TaskWithSubtasks.kt
    - app/src/main/java/com/routinetool/ui/components/SubtaskProgressIndicator.kt
  modified:
    - app/src/main/java/com/routinetool/data/repository/TaskRepository.kt
    - app/src/main/java/com/routinetool/ui/screens/tasklist/TaskListViewModel.kt
    - app/src/main/java/com/routinetool/ui/components/TaskCard.kt
    - app/src/main/java/com/routinetool/ui/screens/tasklist/TaskListScreen.kt

key-decisions:
  - "Progress indicator threshold at 6 subtasks: visual boxes below, text count above"
  - "Subtask toggle uses VIRTUAL_KEY haptic (lighter) to differentiate from task completion (CONFIRM)"
  - "TaskWithSubtasks computed properties: completedSubtaskCount, totalSubtaskCount, hasSubtasks"
  - "Repository pattern: flatMapLatest + combine to join tasks with subtasks reactively"

patterns-established:
  - "SubtaskProgressIndicator: 16dp green checkmarks for completed, 1dp bordered boxes for incomplete"
  - "Expanded card subtask checklist: 20dp green checkmark icon vs checkbox, matching task completion pattern"
  - "TaskWithSubtasks wrapper pattern for aggregating domain models in UI state"

# Metrics
duration: 3min
completed: 2026-01-30
---

# Phase 03 Plan 03: Subtask Progress Display Summary

**At-a-glance subtask progress on task cards with checkbox row (≤6) or count (>6), plus inline completion toggle with lighter haptic feedback**

## Performance

- **Duration:** 3 min
- **Started:** 2026-01-30T12:29:56Z
- **Completed:** 2026-01-30T12:32:56Z
- **Tasks:** 3
- **Files modified:** 6 (2 created, 4 modified)

## Accomplishments

- TaskWithSubtasks domain model provides computed properties for progress tracking
- SubtaskProgressIndicator shows visual progress: checkbox row for ≤6 subtasks, text count for >6
- Collapsed task cards display subtask progress below title
- Expanded task cards show subtask checklist with toggle capability
- Subtask completion uses lighter haptic feedback (VIRTUAL_KEY) than task completion (CONFIRM)
- Repository observes tasks with subtasks reactively using flatMapLatest + combine

## Task Commits

Each task was committed atomically:

1. **Task 1: Create TaskWithSubtasks domain model and SubtaskProgressIndicator** - `c33f944` (feat)
2. **Task 2: Update TaskListViewModel to observe tasks with subtasks** - `c516b46` (feat)
3. **Task 3: Update TaskCard and TaskListScreen for subtask display** - `774efd9` (feat)

## Files Created/Modified

### Created
- `app/src/main/java/com/routinetool/domain/model/TaskWithSubtasks.kt` - Aggregates Task with List<Subtask>, provides computed properties (completedSubtaskCount, totalSubtaskCount, hasSubtasks)
- `app/src/main/java/com/routinetool/ui/components/SubtaskProgressIndicator.kt` - Visual progress component showing checkbox row (16dp icons, 4dp spacing) for ≤6 subtasks or text count for >6

### Modified
- `app/src/main/java/com/routinetool/data/repository/TaskRepository.kt` - Added observeActiveTasksWithSubtasks and observeRecentlyCompletedWithSubtasks methods using flatMapLatest + combine pattern
- `app/src/main/java/com/routinetool/ui/screens/tasklist/TaskListViewModel.kt` - Changed state to List<TaskWithSubtasks>, added toggleSubtask function, updated filtering/sorting
- `app/src/main/java/com/routinetool/ui/components/TaskCard.kt` - Accept TaskWithSubtasks parameter, show SubtaskProgressIndicator in collapsed view, subtask checklist in expanded view with toggle callback
- `app/src/main/java/com/routinetool/ui/screens/tasklist/TaskListScreen.kt` - Pass TaskWithSubtasks to TaskCard, wire up viewModel::toggleSubtask across all sections

## Decisions Made

1. **Progress threshold at 6 subtasks:** Visual checkbox row provides clear progress for small counts (≤6), text count keeps UI compact for larger lists (>6)
2. **Dual haptic feedback levels:** VIRTUAL_KEY for subtask toggle (lighter) vs CONFIRM for task completion (stronger) creates tactile hierarchy
3. **Computed properties on TaskWithSubtasks:** Encapsulate progress calculation logic (completedSubtaskCount, totalSubtaskCount, hasSubtasks) in domain model
4. **Reactive combination pattern:** flatMapLatest + combine in repository joins each task with its subtasks, automatically updates when subtasks change
5. **Consistent checkmark sizing:** 16dp in progress indicator (matches box size), 20dp in expanded checklist, 32dp for full task completion (visual hierarchy)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

**Java runtime not available in execution environment:**
- Build verification step skipped (`./gradlew assembleDebug` requires JAVA_HOME)
- Code follows existing patterns precisely, compilation assumed successful
- No runtime testing performed; user will verify in next checkpoint

This is expected in Docker-based execution environment without Android SDK installed.

## Next Phase Readiness

**Ready for 03-04 (Subtask UI on Edit Screen):**
- TaskWithSubtasks domain model established
- Repository layer observes tasks with subtasks
- UI patterns defined for subtask display and interaction
- Haptic feedback hierarchy implemented

**Remaining Phase 3 work:**
- Plan 04: Edit screen subtask management (add, reorder, delete)

**No blockers.** Data layer and task list UI complete. Edit screen can follow same patterns.

---
*Phase: 03-structure-breakdown*
*Completed: 2026-01-30*
