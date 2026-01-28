# Routine Tool

## What This Is

A task and routine management app designed specifically for neurodivergent people who struggle with traditional todo apps. Addresses three core failures: overwhelm from seeing everything at once, forgetting to check the app, and tasks feeling too big or vague to start. Android app with local storage, built by and for people who need a system that works with their brain, not against it.

## Core Value

Tasks are easy to capture and impossible to lose, while the app actively helps you focus on what matters right now instead of drowning you in everything at once.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Quick task capture with minimal friction (add now, detail later)
- [ ] One-time tasks with optional soft/hard deadlines
- [ ] Repeating quota tasks ("exercise 2x per week") with completion tracking
- [ ] Cooldown timer tasks ("medication not until 8 hours pass") with timestamp tracking
- [ ] Yearly/monthly/daily repeating tasks
- [ ] Subtask creation and ability to detach subtasks into standalone tasks
- [ ] Task dependencies with per-task visibility setting (hide blocked vs show greyed)
- [ ] Completion history with timestamps for all task types
- [ ] Smart "focus view" that suggests priority tasks based on urgency/deadlines
- [ ] User-adjustable task limit in focus view
- [ ] Ordering/filtering by category, urgency, deadline proximity
- [ ] Categories for task organization

### Out of Scope

- Cross-device sync — single device is sufficient for v1, adds significant complexity
- Web version — Android first, architecture should accommodate but not implement yet
- iOS version — same as web, future consideration
- Push notifications — v2 feature, requires more infrastructure
- Anxiety vault ("spooky here-be-dragons") — v2 feature, core task management comes first
- User accounts/authentication — local-only app, no backend
- Play Store distribution — APK sideload is acceptable for initial users
- Social/sharing features — personal productivity tool, not collaborative

## Context

**Target users:** The developer and friends/family with similar neurodivergent traits (ADHD, executive function challenges, memory issues). Not building for a general audience initially.

**Why existing apps fail:**
- Show everything at once → paralysis
- No concept of "I can't deal with this right now"
- Tasks pile up without structure for breaking them down
- No way to verify "did I already do this today?"
- Repeating tasks are all-or-nothing, no quota tracking

**Use cases from initial brainstorm:**
- Exercise tracking: "2 times this week" with visual progress
- Medication verification: "Did I take my morning pills?" with timestamp proof
- Child fever care: "Gave ibuprofen at 2pm, can give paracetamol at 10pm"
- Car maintenance: "Inspection due 12 months from last visit"
- Basic self-care: "Eat every 2 hours", "Drink water" reminders

**Technical direction:** Android-first but architecture decisions should not preclude web/iOS later. This means avoiding Android-only patterns where cross-platform alternatives exist at similar complexity.

## Constraints

- **Platform**: Android (Kotlin/Jetpack Compose likely, but open to cross-platform if it doesn't compromise UX)
- **Storage**: Local only — SQLite or Room, no cloud backend
- **Distribution**: APK sideload acceptable, no Play Store requirements for v1
- **UI Philosophy**: Calm and minimal when viewing tasks; progressive disclosure when editing (complexity hidden behind expandable menus/toolbars)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Local-only storage for v1 | Eliminates auth, sync conflicts, backend costs; sufficient for small user group | — Pending |
| Distinct task types (one-time, quota, cooldown) vs unified "repeat" system | Clearer mental model for users; each type has genuinely different behavior | — Pending |
| Per-task dependency visibility setting | Some tasks benefit from seeing the full chain, others from hiding what can't be done yet | — Pending |
| Vault feature deferred to v2 | Core task management must work first; vault is a coping mechanism, not the foundation | — Pending |

---
*Last updated: 2025-01-28 after initialization*
