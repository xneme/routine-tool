# Phase 1: Foundation & Quick Capture - Context

**Gathered:** 2026-01-28
**Status:** Ready for planning

<domain>
## Phase Boundary

Zero-config task capture and basic management for an Android app designed for neurodivergent users. Users can capture tasks instantly with just a title, manage one-time tasks with optional soft or hard deadlines, complete and archive tasks, and reschedule overdue tasks without guilt. Local-only storage. No recurring tasks, no subtask breakdown, no filtering/focus views — those are later phases.

</domain>

<decisions>
## Implementation Decisions

### Capture flow
- FAB (floating action button) to initiate task creation
- Tapping FAB opens a full-screen new-task screen
- Only title field visible by default — tap "Add details" to reveal deadline/type options
- After saving, return to the task list (no stay-for-another, no undo toast)

### Task presentation
- Card-based layout for each task
- Card shows: title, details field, icon for special task types, closest deadline, small checkmarks for completed subtasks or repetitions
- Cards are expandable — tap to reveal full content and editing options
- Single-time task with deadline and optional soft deadline is the default type
- Default list ordering: by deadline proximity (closest deadline first, no-deadline tasks at bottom)
- Completed tasks move to a "Done" section at the bottom of the list
- Single-time completed tasks clear from Done section after 24 hours (still in archive); repeat/quota tasks stay visible for their relevant time period

### Deadline & overdue handling
- Soft vs hard deadlines distinguished by different icons (e.g., soft = clock, hard = exclamation/flag)
- Overdue state shown as a subtle "overdue" badge next to the deadline — informational, not alarming
- A separate "Overdue" section appears above the main list when any tasks are overdue
- Rescheduling happens from the expanded card detail view (tap card, edit deadline)
- Long-overdue tasks (weeks+) get a gentle suggestion to reschedule or archive — no judgment, no escalation

### Emotional tone & language
- Completion feedback: subtle haptic tap + checkmark animation — tactile confirmation without words
- Empty state: calm blank screen with simple message like "Nothing here yet" — no pressure to add tasks
- Overall visual mood: clean and neutral — sharp edges, monochrome with accent color, minimal decoration, tool-like and focused

### Claude's Discretion
- Exact icon choices for soft/hard deadlines and task type badges
- Loading states and transitions
- Specific spacing, typography, and card dimensions
- "Overdue for a long time" threshold (how many days before suggesting reschedule)
- Details field behavior on the new-task screen

</decisions>

<specifics>
## Specific Ideas

- The app should feel like a focused tool, not a playful toy — clean and neutral aesthetic
- Haptic feedback on task completion is important for tactile satisfaction
- The expandable card pattern is central — collapsed cards show key info, expanded cards show everything
- Subtask checkmarks and repetition indicators should be visible even on collapsed cards (future-proofing the card design for phases 3-4)

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 01-foundation-quick-capture*
*Context gathered: 2026-01-28*
