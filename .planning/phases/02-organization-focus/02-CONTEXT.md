# Phase 2: Organization & Focus - Context

**Gathered:** 2026-01-29
**Status:** Ready for planning

<domain>
## Phase Boundary

Smart filtering and focus view to combat overwhelm. Users can sort tasks, filter the task list, and access a curated focus view showing limited priority tasks. This phase does NOT add new task types, subtasks, or recurring tasks — those belong in later phases.

</domain>

<decisions>
## Implementation Decisions

### Sorting behavior
- Default sort: Urgency-first (overdue → due soon → no deadline) — matches Phase 1 behavior
- Sort UI: Dropdown menu triggered by sort icon
- Sort preference persists between sessions
- When sorted by deadline: tasks without deadlines appear at bottom

### Filtering design
- Filter UI: Horizontal scrollable filter chips below sort
- Filterable dimensions: Status (Active, Done, Overdue) + Deadline type (Soft, Hard, No deadline)
- Multiple filters: AND logic (e.g., Active AND Hard deadline shows only active tasks with hard deadlines)
- Filter state does NOT persist — always reset to "all" on app open

### Focus view
- Entry point: FAB action (floating action button with 'Focus' option)
- Task selection: Hybrid — auto-selected by urgency, but user can pin/unpin to override
- Task limit: User configurable (success criteria requirement)
- Visual treatment: Full screen, replaces task list, back button to return

### Task editing UX
- Expandable sections: Notes field + deadline type options
- Reveal trigger: Tap section header to expand
- Edit location: Full screen (consistent with add task)
- State memory: Expansion state persists between edits (if you expanded notes, it stays expanded)

### Claude's Discretion
- Default focus view task limit (before user configures)
- Filter chip visual styling
- Animation/transition for focus view entry
- Exact dropdown menu positioning and styling

</decisions>

<specifics>
## Specific Ideas

No specific requirements — open to standard approaches

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 02-organization-focus*
*Context gathered: 2026-01-29*
