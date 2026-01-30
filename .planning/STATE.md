# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-01-28)

**Core value:** Tasks are easy to capture and impossible to lose, while the app actively helps you focus on what matters right now instead of drowning you in everything at once.
**Current focus:** Phase 3 - Structure & Breakdown (IN PROGRESS)

## Current Position

Phase: 3 of 6 (Structure & Breakdown)
Plan: 3 of 4
Status: In progress
Last activity: 2026-01-30 - Completed 03-03-PLAN.md (Subtask Progress Display)

Progress: [███████████░] 94% (Phases 1 + 1.1 + 2 complete, Phase 3 nearly complete)

## Performance Metrics

**Velocity:**
- Total plans completed: 11
- Average duration: 56min
- Total execution time: 10 hours 9 min

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01-foundation-quick-capture | 3/3 | 559min | 186min |
| 01.1-ui-refinements | 2/2 | 18min | 9min |
| 02-organization-focus | 4/4 | 11min | 2.8min |
| 03-structure-breakdown | 3/4 | 9min | 3min |

**Recent Trend:**
- Last 5 plans: 02-03 (3min), 02-04 (3min), 03-01 (3min), 03-03 (3min)
- Trend: Consistently fast execution with well-scoped tasks

*Updated after each plan completion*

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- Phase 1 focus: Local-only storage eliminates auth and sync complexity
- Phase 1 focus: Distinct task types provide clearer mental model than unified system
- Vault feature deferred to v2: Core task management must work first
- **01-01:** Timestamp storage uses Long (epoch millis) in Room entities, converted to Instant in repository layer
- **01-01:** Deadline-proximity ordering: overdue tasks first, then nearest deadline, then no-deadline
- **01-01:** Monochrome theme with calm accent (#5C6BC0) - no harsh red for overdue states
- **01-01:** Room schema export enabled for future migration support
- **01-02:** Task domain model separate from Room entity, with mappers in repository layer
- **01-02:** UDF pattern with single StateFlow in ViewModel for task list state
- **01-02:** Expansion state tracked locally in composable, not ViewModel (single card expanded at a time)
- **01-02:** Long-overdue threshold set to 7 days for gentle reschedule prompts
- **01-02:** Quick reschedule buttons: Tomorrow, +1 week, Remove deadline
- **01-03:** Progressive disclosure: title field visible by default, details hidden until user taps "Add details"
- **01-03:** AddTaskViewModel supports both add (no taskId) and edit (with taskId) modes via Koin parametersOf
- **01-03:** Type-safe navigation routes with NavRoutes object and helper functions for parameterized routes
- **01-03:** Material 3 DatePickerDialog for deadline selection, converts millis to LocalDate
- **01-03:** Save button disabled when title is blank (only required field)
- **01.1-01:** ElevatedCard with 2.dp elevation for visual distinction
- **01.1-01:** Reschedule options collapsed behind expandable button (AnimatedVisibility)
- **01.1-01:** Checkbox moved to right side for cleaner layout
- **01.1-01:** Deadline icons use primary (soft) and secondary (hard) colors
- **01.1-02:** Custom subtle tints (8-12% alpha) instead of Material container colors
- **01.1-02:** Tasks due TODAY are not overdue (compare against start of day)
- **01.1-02:** Bright green checkmark (32dp) replaces checkbox for completed tasks
- **01.1-02:** HorizontalDividers between sections, conditional on content
- **02-01:** DataStore 1.1.1 for preferences (latest stable, coroutines-native)
- **02-01:** Flow-based async access pattern for all preferences
- **02-01:** Default focus task limit of 5 (cognitive comfort zone)
- **02-01:** SortOption comparators handle null deadlines with MAX_VALUE
- **02-02:** Filter state does NOT persist (resets on app restart per CONTEXT.md)
- **02-02:** Sort preference persists via DataStore (per CONTEXT.md)
- **02-02:** Status filters (Active/Overdue/Done) AND deadline type filters (Soft/Hard/No deadline)
- **02-02:** Tasks with both soft+hard deadlines match if either filter is on
- **02-03:** Hybrid task selection: pinned tasks first, remaining slots by urgency
- **02-03:** Urgency comparator: overdue first, then nearest deadline, then no deadline
- **02-03:** Expandable FAB menu replaces simple FAB for multi-action support
- **02-04:** ExpandableSection composable encapsulates expand/collapse pattern
- **02-04:** AnimatedVisibility with expandVertically + fadeIn/fadeOut for smooth transitions
- **02-04:** Expansion state persists to DataStore via ViewModel toggle functions
- **03-01:** Fractional indexing for subtask position allows reordering without updating all rows
- **03-01:** Precision loss detection triggers full renumbering when gaps become too small (<0.0001f)
- **03-01:** CASCADE delete on foreign key ensures subtasks deleted with parent task
- **03-01:** Database version 2 with migration creating subtasks table
- **03-03:** Progress indicator threshold at 6 subtasks: visual boxes below, text count above
- **03-03:** Subtask toggle uses VIRTUAL_KEY haptic (lighter) vs task completion CONFIRM (stronger)
- **03-03:** TaskWithSubtasks computed properties: completedSubtaskCount, totalSubtaskCount, hasSubtasks
- **03-03:** Repository flatMapLatest + combine pattern joins tasks with subtasks reactively

### Pending Todos

**Phase 3 Structure & Breakdown** - IN PROGRESS
- [x] Plan 01: Subtask Data Layer
- [ ] Plan 02: Subtask UI Components (in progress)
- [x] Plan 03: Subtask Progress Display
- [ ] Plan 04: Remaining work

### Roadmap Evolution

- Phase 1.1 inserted after Phase 1: UI Refinements (URGENT) - Visual polish from user feedback after Phase 1 verification
- Phase 1.1 completed 2026-01-29
- Phase 2 completed 2026-01-29
- Phase 3 started 2026-01-30

### Blockers/Concerns

**Phase 3 Plan 03 complete:**
- Subtask progress display on task cards working
- TaskWithSubtasks aggregation pattern established
- SubtaskProgressIndicator shows visual progress (checkbox row or count)
- Subtask toggle from task list with lighter haptic feedback
- Repository observes tasks with subtasks reactively
- No blockers for remaining Phase 3 work

## Session Continuity

Last session: 2026-01-30
Stopped at: Completed 03-03-PLAN.md (Subtask Progress Display)
Resume file: Continue with remaining Phase 3 work
