# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-01-28)

**Core value:** Tasks are easy to capture and impossible to lose, while the app actively helps you focus on what matters right now instead of drowning you in everything at once.
**Current focus:** Phase 2 - Organization & Focus

## Current Position

Phase: 2 of 6 (Organization & Focus)
Plan: 2 of 4
Status: In progress
Last activity: 2026-01-29 - Completed 02-02-PLAN.md (Sorting/Filtering Implementation)

Progress: [████████░░] 84% (Phases 1 + 1.1 complete, Plans 1-2 of Phase 2 complete)

## Performance Metrics

**Velocity:**
- Total plans completed: 7
- Average duration: 85min
- Total execution time: 9.8 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01-foundation-quick-capture | 3/3 | 559min | 186min |
| 01.1-ui-refinements | 2/2 | 18min | 9min |
| 02-organization-focus | 2/4 | 5min | 2.5min |

**Recent Trend:**
- Last 5 plans: 01.1-01 (3min), 01.1-02 (15min), 02-01 (2min), 02-02 (3min)
- Trend: Infrastructure and UI plans executing quickly when well-scoped

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

### Pending Todos

**Phase 2 Organization & Focus** - In Progress
- [x] Plan 01: Preference Infrastructure
- [x] Plan 02: Sorting/Filtering Implementation
- [ ] Plan 03: Focus View
- [ ] Plan 04: Edit Progressive Disclosure

### Roadmap Evolution

- Phase 1.1 inserted after Phase 1: UI Refinements (URGENT) - Visual polish from user feedback after Phase 1 verification
- Phase 1.1 completed 2026-01-29

### Blockers/Concerns

**Sort/filter implementation complete:**
- TaskListViewModel combines repository flows with filter/sort state
- SortDropdown with 3 options (Urgency, Deadline, Created)
- FilterChipRow with 6 filters (3 status + 3 deadline type)
- Sort persists via DataStore, filters reset on app restart

**Ready for Plan 03:** Focus View with hybrid task selection

## Session Continuity

Last session: 2026-01-29
Stopped at: Completed 02-02-PLAN.md - Sorting/Filtering Implementation
Resume file: .planning/phases/02-organization-focus/02-03-PLAN.md
