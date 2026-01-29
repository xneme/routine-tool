# Requirements: Routine Tool

**Defined:** 2026-01-28
**Core Value:** Tasks are easy to capture and impossible to lose, while the app actively helps you focus on what matters right now instead of drowning you in everything at once.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Capture

- [x] **CAPT-01**: User can add a task with just a title in under 10 seconds *(Phase 1)*
- [x] **CAPT-02**: New tasks default to uncategorized with no deadline or importance required *(Phase 1)*
- [x] **CAPT-03**: User can add details (deadline, importance, type) to an existing task at any time *(Phase 1)*

### Task Types

- [x] **TYPE-01**: User can create one-time tasks that are done once and archived *(Phase 1)*
- [x] **TYPE-02**: User can set optional soft deadline (reminder) or hard deadline (must-do) on one-time tasks *(Phase 1)*
- [ ] **TYPE-03**: User can create recurring tasks that repeat daily, weekly, monthly, or yearly
- [ ] **TYPE-04**: User can create quota tasks with a target count within a time period (e.g., "3x this week")
- [ ] **TYPE-05**: User can create cooldown tasks with a minimum interval between completions (e.g., "every 8 hours")

### Organization

- [ ] **ORG-01**: User can sort tasks by urgency, deadline proximity, or creation date
- [ ] **ORG-02**: User can filter tasks by type (one-time, recurring, quota, cooldown)
- [ ] **ORG-03**: App provides focus view showing a limited, smart-suggested set of priority tasks
- [ ] **ORG-04**: User can adjust how many tasks the focus view shows

### Structure

- [ ] **STRUC-01**: User can add subtasks to any task
- [ ] **STRUC-02**: User can mark individual subtasks as complete independently
- [ ] **STRUC-03**: User can reorder subtasks within a task

### Tracking

- [ ] **TRAC-01**: App records completion timestamps for all task and subtask completions
- [ ] **TRAC-02**: User can view completion history for repeating tasks (when each instance was completed)
- [ ] **TRAC-03**: Quota tasks show visual progress toward target (e.g., "2 of 3 this week")
- [ ] **TRAC-04**: Cooldown tasks show countdown timer until next available completion

### User Experience

- [x] **UX-01**: App uses shame-free language (no punishment for missed tasks, neutral tones) *(Phase 1)*
- [x] **UX-02**: Overdue tasks can be easily rescheduled or dismissed without guilt messaging *(Phase 1)*
- [ ] **UX-03**: Task editing uses progressive disclosure (simple view, details behind expandable sections)
- [x] **UX-04**: Task list view is calm and minimal with clean visual hierarchy *(Phase 1)*
- [ ] **UX-05**: App provides Android home screen widget showing current priority tasks

### Data

- [x] **DATA-01**: All data stored locally on device (Room/SQLite) *(Phase 1)*
- [ ] **DATA-02**: User can export all tasks to a file for backup
- [ ] **DATA-03**: User can import tasks from a backup file

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Organization

- **ORG-05**: User can organize tasks with categories or tags
- **ORG-06**: User can search tasks by text

### Structure

- **STRUC-04**: User can detach a subtask into a standalone task
- **STRUC-05**: User can create dependencies between tasks (task B requires task A)
- **STRUC-06**: User can choose per-task whether blocked tasks are hidden or shown greyed out

### Capture

- **CAPT-04**: User can add tasks via Android share sheet from other apps
- **CAPT-05**: User can add tasks via voice input

### User Experience

- **UX-06**: App sends optional notifications for approaching deadlines
- **UX-07**: App provides notification when quota tasks need more completions this period
- **UX-08**: App provides "anxiety vault" — separate space for overwhelming tasks, completely hidden from main list

### Sync

- **SYNC-01**: User can sync tasks across multiple devices
- **SYNC-02**: User can access tasks via web interface
- **SYNC-03**: User can access tasks via iOS app

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| User accounts/authentication | Local-only app, no backend for v1 |
| Social/sharing features | Personal productivity tool, not collaborative |
| AI task breakdown | Requires AI infrastructure, adds complexity |
| Calendar integration | Complex integration work, defer to v2+ |
| Gamification/points/levels | Research shows gamification causes burnout for ADHD users |
| Play Store distribution | APK sideload acceptable for initial users |
| Complex onboarding | Research shows setup friction causes ADHD abandonment |
| Streak/punishment systems | Directly harmful for neurodivergent users (shame/RSD) |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| CAPT-01 | Phase 1 | Pending |
| CAPT-02 | Phase 1 | Pending |
| CAPT-03 | Phase 1 | Pending |
| TYPE-01 | Phase 1 | Pending |
| TYPE-02 | Phase 1 | Pending |
| TYPE-03 | Phase 4 | Pending |
| TYPE-04 | Phase 4 | Pending |
| TYPE-05 | Phase 4 | Pending |
| ORG-01 | Phase 2 | Pending |
| ORG-02 | Phase 2 | Pending |
| ORG-03 | Phase 2 | Pending |
| ORG-04 | Phase 2 | Pending |
| STRUC-01 | Phase 3 | Pending |
| STRUC-02 | Phase 3 | Pending |
| STRUC-03 | Phase 3 | Pending |
| TRAC-01 | Phase 3 | Pending |
| TRAC-02 | Phase 4 | Pending |
| TRAC-03 | Phase 4 | Pending |
| TRAC-04 | Phase 4 | Pending |
| UX-01 | Phase 1 | Pending |
| UX-02 | Phase 1 | Pending |
| UX-03 | Phase 2 | Pending |
| UX-04 | Phase 1 | Pending |
| UX-05 | Phase 5 | Pending |
| DATA-01 | Phase 1 | Pending |
| DATA-02 | Phase 6 | Pending |
| DATA-03 | Phase 6 | Pending |

**Coverage:**
- v1 requirements: 27 total
- Mapped to phases: 27
- Unmapped: 0

---
*Requirements defined: 2026-01-28*
*Last updated: 2026-01-28 after roadmap creation*
