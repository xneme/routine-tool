# Phase 3: Structure & Breakdown - Context

**Gathered:** 2026-01-30
**Status:** Ready for planning

<domain>
## Phase Boundary

Break tasks into subtasks with independent completion tracking. Users can add, complete, and reorder subtasks within any task. Completion timestamps are recorded for all completions.

</domain>

<decisions>
## Implementation Decisions

### Subtask presentation
- Progress indicator on collapsed tasks: row of checkboxes for ≤6 subtasks, text count ("2/5") for >6
- Checkboxes and green checkmarks match existing task card styling (green checkmarks same size as boxes)
- Expanded view shows simple checklist: checkbox + title per subtask
- Completed subtasks show green checkmark only, no strikethrough or dimming

### Adding subtasks
- Subtasks added from edit screen only (not from expanded task card on list)
- Subtasks section hidden behind expandable "Add Subtasks" button (matches other edit screen sections)
- Inline text field at end of subtask list for adding
- Subtasks are title-only — no notes, deadlines, or other fields
- Soft limit with warning around ~20 subtasks (suggest task might be too complex)

### Completion behavior
- Parent and subtasks are independent — completing all subtasks does NOT auto-complete parent
- Parent can be completed anytime regardless of subtask state — no blocking or warnings
- Completing parent preserves subtask completion states (no cascade)
- Subtask completion gets lighter haptic feedback than full task completion

### Reordering interaction
- Long-press drag to reorder (no visible drag handles)
- Reordering from edit screen only (matches where subtasks are added)
- Completed subtasks remain reorderable
- Visual feedback during drag: elevated shadow + gap indicator at drop position

### Claude's Discretion
- Exact animation timing for drag feedback
- Soft limit threshold number (suggested ~20)
- Specific warning message wording

</decisions>

<specifics>
## Specific Ideas

- Checkbox row indicator should match existing task card checkbox/checkmark styling for visual consistency
- Green checkmarks in indicator row can be same size as unchecked boxes (unlike larger checkmarks on full task cards)

</specifics>

<deferred>
## Deferred Ideas

- **Haptic feedback bug**: User reported task completion haptic is not working — investigate whether implementation exists or is broken (not Phase 3 scope)

</deferred>

---

*Phase: 03-structure-breakdown*
*Context gathered: 2026-01-30*
