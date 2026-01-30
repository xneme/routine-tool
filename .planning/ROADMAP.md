# Roadmap: Routine Tool

## Overview

This roadmap delivers an Android task management app designed specifically for neurodivergent users who struggle with traditional todo apps. The journey progresses from rapid task capture and visual organization through advanced task types and external visibility, culminating in data portability. Each phase addresses specific ADHD-related challenges like overwhelm, object permanence issues, and time blindness while building toward a complete routine management system.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Foundation & Quick Capture** - Zero-config task capture and basic management
- [x] **Phase 1.1: UI Refinements** - Visual polish from user feedback (INSERTED)
- [x] **Phase 2: Organization & Focus** - Smart filtering and focus view to combat overwhelm
- [x] **Phase 3: Structure & Breakdown** - Subtasks and completion tracking
- [ ] **Phase 4: Recurring & Time-Based Tasks** - Advanced task types for routines
- [ ] **Phase 5: Visibility & Widget** - Home screen presence for object permanence
- [ ] **Phase 6: Data Portability** - Export and import for backup

## Phase Details

### Phase 1: Foundation & Quick Capture
**Goal**: User can capture tasks instantly and manage them without friction or shame
**Depends on**: Nothing (first phase)
**Requirements**: CAPT-01, CAPT-02, CAPT-03, TYPE-01, TYPE-02, UX-01, UX-02, UX-04, DATA-01
**Success Criteria** (what must be TRUE):
  1. User can add a task with just a title in under 10 seconds from app launch
  2. User can complete one-time tasks and see them move to an archived state
  3. User can set soft or hard deadlines on tasks without being forced to
  4. Overdue tasks can be rescheduled or dismissed without guilt messaging
  5. App displays calm, minimal task list with clean visual hierarchy
**Plans**: 3 plans

Plans:
- [x] 01-01-PLAN.md — Project scaffolding + data layer (Room database, Koin DI, Material 3 theme)
- [x] 01-02-PLAN.md — Task list screen (expandable cards, sections, completion with haptics)
- [x] 01-03-PLAN.md — Add task screen + navigation + verification (progressive disclosure, full flow)

### Phase 1.1: UI Refinements (INSERTED)
**Goal**: Polish visual hierarchy and task card presentation based on user feedback
**Depends on**: Phase 1
**Requirements**: UX-01 (refinement), UX-02 (refinement)
**Success Criteria** (what must be TRUE):
  1. Task cards are visually distinct from each other (not appearing as one block)
  2. Collapsed tasks have reduced vertical spacing for better density
  3. Sections (Overdue, Active, Done) are clearly separated visually
  4. Active tasks section has a header like other sections
  5. Checkbox positioned on right side under deadline icons
  6. Icons have appropriate color coding (not monochrome)
  7. Overdue tasks have subtle red tint for differentiation
  8. Done tasks have subtle green tint for differentiation
  9. Reschedule options collapsed behind expandable button
  10. App header removed from top of screen
**Plans**: 2 plans

Plans:
- [x] 01.1-01-PLAN.md — Card visual refinements (ElevatedCard, semantic tinting, checkbox repositioning, collapsible reschedule, icon colors)
- [x] 01.1-02-PLAN.md — List layout refinements (remove TopAppBar, section dividers, Tasks header) + visual verification

### Phase 2: Organization & Focus
**Goal**: User can filter task views and access a curated focus list that prevents overwhelm
**Depends on**: Phase 1
**Requirements**: ORG-01, ORG-02, ORG-03, ORG-04, UX-03
**Success Criteria** (what must be TRUE):
  1. User can sort tasks by urgency, deadline proximity, or creation date
  2. User can filter the task list to show only specific task types
  3. User can access a focus view showing a limited set of priority tasks
  4. User can adjust the number of tasks shown in focus view
  5. Task editing reveals additional details through expandable sections rather than cluttered forms
**Plans**: 4 plans

Plans:
- [x] 02-01-PLAN.md — Preference infrastructure (DataStore, SortOption, FilterState)
- [x] 02-02-PLAN.md — Sorting and filtering UI (sort dropdown, filter chips)
- [x] 02-03-PLAN.md — Focus View (hybrid task selection, pin/unpin, task limit)
- [x] 02-04-PLAN.md — Edit progressive disclosure (expandable sections)

### Phase 3: Structure & Breakdown
**Goal**: User can break down overwhelming tasks into manageable subtasks and track completion
**Depends on**: Phase 2
**Requirements**: STRUC-01, STRUC-02, STRUC-03, TRAC-01
**Success Criteria** (what must be TRUE):
  1. User can add subtasks to any task to break down complexity
  2. User can mark individual subtasks as complete independently of the parent task
  3. User can reorder subtasks within a task to prioritize steps
  4. App records completion timestamps for every task and subtask completion
**Plans**: 3 plans

Plans:
- [x] 03-01-PLAN.md — Data layer (SubtaskEntity, SubtaskDao, migration, repository methods)
- [x] 03-02-PLAN.md — Subtask management UI (edit screen section, reorderable list, soft limit)
- [x] 03-03-PLAN.md — Task card progress (SubtaskProgressIndicator, subtask checklist, haptics)

### Phase 4: Recurring & Time-Based Tasks
**Goal**: User can create repeating routines with flexible tracking that reduces shame
**Depends on**: Phase 3
**Requirements**: TYPE-03, TYPE-04, TYPE-05, TRAC-02, TRAC-03, TRAC-04
**Success Criteria** (what must be TRUE):
  1. User can create recurring tasks that repeat daily, weekly, monthly, or yearly
  2. User can create quota tasks with a target count within a time period (e.g., "3x this week")
  3. User can create cooldown tasks with a minimum interval between completions (e.g., "every 8 hours")
  4. User can view completion history for repeating tasks showing when each instance was completed
  5. Quota tasks display visual progress toward target (e.g., "2 of 3 this week")
  6. Cooldown tasks display countdown timer until next available completion
**Plans**: TBD

Plans:
- [ ] 04-01: TBD during planning
- [ ] 04-02: TBD during planning
- [ ] 04-03: TBD during planning

### Phase 5: Visibility & Widget
**Goal**: User has persistent visibility of priority tasks via home screen widget
**Depends on**: Phase 4
**Requirements**: UX-05
**Success Criteria** (what must be TRUE):
  1. User can add a home screen widget that displays current priority tasks
  2. Widget shows up-to-date task information without needing to open the app
  3. Tapping widget tasks opens the app directly to that task
**Plans**: TBD

Plans:
- [ ] 05-01: TBD during planning

### Phase 6: Data Portability
**Goal**: User can export and import all task data for backup and migration
**Depends on**: Phase 5
**Requirements**: DATA-02, DATA-03
**Success Criteria** (what must be TRUE):
  1. User can export all tasks to a file for backup
  2. User can import tasks from a backup file to restore data
  3. Export includes all task types, subtasks, completion history, and settings
**Plans**: TBD

Plans:
- [ ] 06-01: TBD during planning

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 1.1 → 2 → 3 → 4 → 5 → 6

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation & Quick Capture | 3/3 | ✓ Complete | 2026-01-29 |
| 1.1 UI Refinements (INSERTED) | 2/2 | ✓ Complete | 2026-01-29 |
| 2. Organization & Focus | 4/4 | ✓ Complete | 2026-01-30 |
| 3. Structure & Breakdown | 3/3 | ✓ Complete | 2026-01-30 |
| 4. Recurring & Time-Based Tasks | 0/TBD | Not started | - |
| 5. Visibility & Widget | 0/TBD | Not started | - |
| 6. Data Portability | 0/TBD | Not started | - |
