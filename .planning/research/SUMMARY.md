# Project Research Summary

**Project:** Routine Tool - Neurodivergent Task Management
**Domain:** ADHD/executive dysfunction-focused Android task and routine management
**Researched:** 2026-01-28
**Confidence:** HIGH

## Executive Summary

This is a neurodivergent-focused task and routine management Android app addressing ADHD-specific challenges like time blindness, object permanence issues, and executive dysfunction. Experts build these apps by prioritizing immediate utility over feature richness, designing shame-free interfaces that combat the "Wall of Shame" effect, and providing external structure without rigid enforcement. The market leaders (Tiimo, Morgen, Sunsama) succeed by implementing visual organization, focus views limiting cognitive load, and dopamine-triggering completion feedback.

The recommended approach is native Android with Kotlin + Jetpack Compose using MVVM architecture, local-first Room database, and minimal notifications. Core MVP features are quick capture (thought captured in under 10 seconds), task breakdown to combat paralysis, visual organization with flat hierarchies, and a focus view showing only 3-5 next tasks. Time awareness features (estimates, countdown timers) and capacity management follow in phase 2 to prevent burnout. Defer complex features like AI breakdown, calendar integration, and body doubling to post-MVP—they add value but require infrastructure that delays getting core utility to users.

Key risks are the "Wall of Shame" (accumulating overdue tasks causing app abandonment), setup friction (complex onboarding exhausting ADHD motivation before first task is captured), and notification overload leading to notification blindness. Mitigation: auto-archive old tasks, zero-config first use with progressive disclosure, and minimal default notifications with user control. The primary architectural risk is premature optimization—starting with Hilt instead of Koin, over-engineering the domain layer before complexity warrants it, or building multi-module structure before build times justify it. Keep it simple initially, extract complexity only when measurable pain occurs.

## Key Findings

### Recommended Stack

Native Android development with modern Kotlin ecosystem provides the optimal foundation. Kotlin 2.3.0 with the stable K2 compiler, Jetpack Compose for declarative UI with Material 3 design system, and Room 2.7+ for local database persistence. This stack is both production-ready today and future-proof for Kotlin Multiplatform expansion if iOS/web versions are needed later.

**Core technologies:**
- **Kotlin 2.3.0**: Android standard language with K2 compiler stable, multiplatform-ready for future expansion
- **Jetpack Compose + Material 3**: Declarative UI with single-activity architecture, Material You dynamic theming, accessibility built-in
- **Room 2.7+**: Google-recommended ORM for local SQLite, compile-time SQL verification, Flow integration for reactive UI updates
- **Navigation Compose 2.8+**: Type-safe navigation with Kotlin serialization, single-activity architecture
- **Koin 4.0+**: Lightweight dependency injection, simpler than Hilt for this project scale, KMP-compatible
- **WorkManager 2.10+**: Reliable background scheduling for notifications and reminders (minimal use per project goals)
- **kotlinx-datetime 0.7.1**: Multiplatform date/time library for task scheduling and cooldown calculations

**What to avoid:**
- Hilt/Dagger (overkill, slower builds, not KMP-compatible)
- XML Views (legacy, Compose is the present and future)
- Firebase (no cloud backend needed for v1)
- Flutter/React Native (Kotlin + Compose has its own multiplatform story via KMP/CMP)

### Expected Features

Research identified 8 table stakes features, 12 differentiators, and 11 anti-features to explicitly avoid.

**Must have (table stakes):**
- **Quick Capture** — ADHD brains lose thoughts in seconds, must capture instantly (natural language, voice optional)
- **Task Breakdown/Subtasks** — Large tasks trigger paralysis, breaking into 5-minute steps bypasses overwhelm
- **Visual Organization** — Object permanence issues mean hidden = forgotten; color-coding, flat hierarchy (NOT folders)
- **Time Awareness** — Time blindness requires external time representation (countdown timers, visual duration)
- **Flexible Structure** — Rigid systems break when one thing goes wrong, must adapt without penalty
- **Dopamine Rewards** — ADHD = low dopamine, immediate rewards (check-offs, streaks) make tasks feel possible
- **Focus View** — Seeing 20 tasks = paralysis; show only what's relevant now (3-5 task limit)
- **Repeating Tasks** — Routines essential but hard to remember, daily/weekly/custom recurrence

**Should have (competitive differentiators):**
- **Quota-Based Tasks** — "Exercise 3x this week" (flexible) vs "Exercise M/W/F" (rigid), reduces shame
- **Cooldown Timers** — After completion, shows rest period before next, prevents hyperfocus burnout
- **Completion History** — Shows accomplishments, not just pending tasks, counters "Wall of Shame"
- **Capacity Management** — Warns when daily plan exceeds realistic time, prevents over-scheduling

**Defer to v2+ (high complexity or specialized):**
- **AI Task Breakdown** — High value but requires AI infrastructure (Tiimo/AFFiNE offer this in 2026)
- **Task Dependencies** — Complex to implement, medium adoption rate
- **Unified Calendar/Tasks** — Complex integration work (Morgen/Sunsama lead this)
- **Energy/Focus Mapping** — Requires historical data collection first
- **Body Doubling** — Requires social infrastructure (accounts, pairing)

**Anti-features (explicitly avoid):**
- Notification bombardment (causes anxiety → ignore all → miss important ones)
- Nested folders/hidden tasks (object permanence: out of sight = doesn't exist)
- Static long lists ("Wall of Shame" = app avoidance = failure spiral)
- Rigid scheduling (breaks cause guilt and abandonment)
- Complex feature overload (decision paralysis and overwhelm)
- Perfectionist language ("Complete your streak!" creates shame on failure)
- Mandatory multi-step setup (ADHD interest expires before setup finishes)
- Complex gamification (simple rewards = dopamine, elaborate systems = cognitive load)

### Architecture Approach

MVVM + Clean Architecture following Google's official Android architecture guide. Jetpack Compose UI layer with ViewModels holding StateFlow for reactive state, Repository layer as single source of truth exposing Flow from Room database queries, and optional Domain layer (UseCases) added only when business logic complexity warrants it. Single-activity architecture with Navigation Compose for type-safe routing.

**Major components:**
1. **UI Layer** — Composables (pure rendering) ← ViewModels (state management) ← UiState (sealed classes)
2. **Data Layer** — Repositories (single source of truth) ← DAOs (Room operations) ← Room Database (local persistence)
3. **Domain Layer (optional)** — UseCases for complex business logic (quota calculations, cooldown checks, dependency chains) when ViewModel logic becomes unwieldy
4. **Navigation** — Single NavController with type-safe routes, deep links for widget/notification taps
5. **State Persistence** — Room IS the persistence, SavedStateHandle for transient UI state (scroll position, filters)

**Data model approach:** Single table with type discriminator (TaskType enum: ONE_TIME, QUOTA, COOLDOWN, RECURRING) plus separate tables for completions and dependencies. This avoids complex multi-table queries for mixed-type lists while maintaining relational integrity.

**Module structure:** Start with single module app, extract later only if build times exceed 30 seconds or preparing for KMP shared module. Package-level separation is sufficient initially.

### Critical Pitfalls

Research identified 12 domain pitfalls; the top 5 critical for ADHD users:

1. **The "Wall of Shame" (Critical, Phase 1)** — Growing overdue task backlogs become visual reminders of failure rather than productivity tools. ADHD users stop opening app entirely to avoid shame. **Prevention:** Auto-archive old tasks, allow dismissal without marking failed, neutral (not red/angry) colors for overdue tasks, emphasize present/future over past failures.

2. **Setup Friction (Critical, Phase 1)** — Complex onboarding kills adoption before first task is captured. ADHD executive dysfunction makes "planning the planning system" impossible. **Prevention:** Zero-config first use (capture task within 10 seconds), progressive disclosure, no account/category requirements upfront.

3. **Notification Overload (Moderate, Phase 2)** — Too many notifications cause "notification blindness" where all reminders are auto-dismissed. Everything screams for attention = nothing seems important. **Prevention:** Default to minimal notifications, smart bundling (daily digest), respect timing (no sleep interruptions), allow notification budgets.

4. **Object Permanence - App Invisibility (Critical, Phase 1)** — Users forget the app exists because ADHD working memory issues make "out of sight = out of mind" literal. **Prevention:** Always-visible home screen widget showing current tasks, persistent notification (optional) as existence reminder.

5. **Decision Paralysis (Moderate, Phase 1)** — Flat task lists without clear "do this next" guidance cause sensory overload. Every task feels both urgent and impossible simultaneously. **Prevention:** Focus view showing ONE task at a time, automatic "suggested next task," limit visible tasks to 3-5, hide the rest.

**Additional critical patterns:**
- Goldilocks Problem: Minimalist apps become boring (no dopamine), gamified apps burn out when novelty fades. **Solution:** Middle path with subtle completion celebration, no elaborate point systems.
- Inflexibility: Rigid schedules break when life happens. **Solution:** Flexible time windows, easy reschedule without penalty, "suggest don't enforce" approach.
- Subtask Trap: Task breakdown can consume all motivational energy. **Solution:** Make subtasks optional, limit depth to 1-2 levels, allow starting tasks without breakdown.

## Implications for Roadmap

Based on research, suggested 6-phase structure:

### Phase 1: Foundation & Quick Capture
**Rationale:** Address the most critical abandonment risks first. Setup friction and Wall of Shame are the #1 reasons ADHD users abandon todo apps within 2 weeks. Quick capture must work perfectly or users lose thoughts before recording them (object permanence issue).

**Delivers:** Working Android app with zero-config onboarding, instant task capture, basic task list with visual organization (colors, flat hierarchy), and task completion with simple dopamine reward (satisfying check-off animation).

**Addresses (from FEATURES.md):**
- Quick Capture (table stakes, complexity: low)
- Visual Organization (table stakes, complexity: medium)
- Dopamine Rewards (table stakes, complexity: low-medium)
- Basic one-time tasks with soft/hard deadlines

**Avoids (from PITFALLS.md):**
- Setup Friction: Zero-config, first task captured in 10 seconds
- Wall of Shame: Auto-archive tasks older than X days, neutral colors
- Object Permanence: Persistent notification or basic widget showing "next task"

**Stack elements:** Kotlin, Compose, Room, Navigation, Material 3
**Architecture:** MVVM shell, single TaskEntity table, basic Repository/ViewModel/Composable structure
**Research needed:** No (standard Android patterns, well-documented)

### Phase 2: Task Breakdown & Focus View
**Rationale:** Once capture works, address overwhelm and paralysis. Task breakdown is table stakes for ADHD users (large tasks = paralysis), but must be implemented carefully to avoid Subtask Trap. Focus view addresses Decision Paralysis by showing only 3-5 next tasks.

**Delivers:** Subtask system (optional, shallow hierarchy), focus view showing suggested next tasks, task filtering by category/type, and completion history view to counter Wall of Shame.

**Addresses:**
- Task Breakdown/Subtasks (table stakes, complexity: medium)
- Focus View (table stakes, complexity: medium)
- Completion History (differentiator, complexity: low-medium)

**Avoids:**
- Decision Paralysis: Focus view with hard limit (3-5 tasks)
- Subtask Trap: Make subtasks optional, limit depth to 1-2 levels
- Wall of Shame: Completion history emphasizes accomplishments

**Stack elements:** Room relations (parent/child tasks), Flow queries for reactive filtering
**Architecture:** DependencyEntity table, CompletionEntity table, GetSuggestedTasksUseCase (optional)
**Research needed:** No (standard patterns for hierarchical data in Room)

### Phase 3: Repeating Tasks & Time Awareness
**Rationale:** Build out time-related features. Repeating tasks are table stakes for routine management. Time awareness features (estimates, countdown timers) address time blindness without requiring rigid scheduling (which triggers Inflexibility pitfall).

**Delivers:** Recurring task templates (daily/weekly/monthly/yearly), time estimates per task, countdown timers for task duration, and next occurrence calculation for recurring tasks.

**Addresses:**
- Repeating Tasks (table stakes, complexity: medium)
- Time Awareness (table stakes, complexity: medium)
- Basic time-based filtering ("due today," "due this week")

**Avoids:**
- Inflexibility: Recurring tasks suggest, don't enforce
- Time Blindness: External time visualization (countdown timers, visual duration)

**Stack elements:** kotlinx-datetime for recurrence calculations, WorkManager for optional reminders
**Architecture:** Recurrence logic in Repository or UseCase layer
**Research needed:** Minor (recurrence rule parsing patterns, WorkManager scheduling)

### Phase 4: Quota & Cooldown Tasks
**Rationale:** Implement differentiating task types. Quota-based tasks address project-specific goal (flexible scheduling reduces shame). Cooldown timers are unique feature supporting ADHD transition needs.

**Delivers:** Quota task type ("3x this week" with flexible completion tracking), cooldown task type (shows rest period after completion), progress indicators for quota tasks, and cooldown countdown UI.

**Addresses:**
- Quota-Based Tasks (differentiator, complexity: medium)
- Cooldown Timers (differentiator, complexity: low)
- Capacity Management foundations (preparing for Phase 5)

**Stack elements:** Duration/Period calculations via kotlinx-datetime
**Architecture:** QuotaUseCase, CooldownUseCase for business logic
**Research needed:** No (straightforward date/time calculations)

### Phase 5: Capacity Management & Polish
**Rationale:** Add intelligent features that prevent burnout. Capacity management warns when daily plan exceeds realistic time (addresses ADHD over-scheduling tendency). Widget provides persistent visibility (combats Object Permanence pitfall).

**Delivers:** Daily capacity calculation (total time estimates vs. available time), overload warnings during planning, home screen widget showing focus tasks, and theming with Material You dynamic colors.

**Addresses:**
- Capacity Management (differentiator, complexity: medium)
- Object Permanence (via widget, critical mitigation)
- Flexible Structure (smart warnings, not enforcement)

**Avoids:**
- Object Permanence: Widget ensures app remains visible
- Inflexibility: Capacity warnings suggest, don't block

**Stack elements:** Glance API for widget, Material 3 dynamic theming
**Architecture:** CapacityUseCase for time calculations
**Research needed:** Minor (Glance widget API patterns)

### Phase 6: Data & Settings
**Rationale:** Production readiness features. Data export for user control, backup for safety, settings for customization without forcing upfront configuration.

**Delivers:** JSON export/import via kotlinx-serialization, settings screen (notification preferences, theme selection, archive rules), data backup functionality, and about/help content.

**Addresses:**
- User control and data portability
- Customization via progressive disclosure (not upfront setup)

**Stack elements:** kotlinx-serialization for export, File API for backup
**Research needed:** No (standard file I/O and serialization patterns)

### Phase Ordering Rationale

**Dependency-driven:**
- Phase 1 must come first: Without quick capture, users can't add tasks
- Phase 2 builds on Phase 1: Subtasks require basic tasks, focus view requires task list
- Phase 3 adds time dimension: Recurrence and estimates build on task foundation
- Phase 4 specializes task types: Quota/cooldown need completion tracking from Phase 2
- Phase 5 requires time data: Capacity management needs estimates from Phase 3

**Risk mitigation sequence:**
- Phase 1 addresses top 3 abandonment risks (Setup Friction, Wall of Shame, Object Permanence basics)
- Phase 2 addresses Decision Paralysis and Subtask Trap
- Notification-related risks deferred (intentionally minimal notifications per project context)

**Value delivery:**
- Phase 1 delivers minimal viable utility (capture, list, complete)
- Phase 2 delivers ADHD-specific value (breakdown, focus)
- Phase 3 delivers routine management (repeating tasks)
- Phase 4 delivers unique differentiators (quota, cooldown)
- Phase 5 delivers burnout prevention (capacity)
- Phase 6 delivers production polish (export, settings)

### Research Flags

**Phases with standard patterns (skip research-phase):**
- **Phase 1:** Compose basics, Room CRUD, Navigation — well-documented official patterns
- **Phase 2:** Room relations, Flow queries — standard hierarchical data patterns
- **Phase 3:** Recurrence logic, WorkManager — established patterns available
- **Phase 4:** Date/time calculations — kotlinx-datetime documentation covers this
- **Phase 6:** File I/O, serialization — standard Android patterns

**Phases needing minor targeted research:**
- **Phase 5:** Glance widget API for home screen widget (newer API, worth quick research)

**Areas needing validation during implementation:**
- Notification strategy (Phase 5 if added): Too user-specific, needs testing with target users
- Focus view algorithm (Phase 2): "Suggested next task" logic needs user feedback to tune
- Auto-archive timing (Phase 1): How many days before tasks fade needs user research

**Deferred complexity (post-v1):**
- AI Task Breakdown: Requires AI infrastructure (OpenAI/Gemini integration research)
- Calendar Integration: Complex sync patterns, multiple calendar APIs
- Task Dependencies: Graph algorithms for dependency chains, complex UI
- Body Doubling: Social features require accounts, pairing, real-time sync

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | **HIGH** | All recommendations verified against official Kotlin/Android docs and 2025-2026 release pages. Version compatibility confirmed. |
| Features | **MEDIUM** | Based on comprehensive WebSearch across 30+ sources, cross-referenced. No single authoritative source (neurodivergent task management is emerging field), but strong consensus on core patterns. Anti-features derived from user research. |
| Architecture | **HIGH** | Follows official Android Architecture Guide. MVVM + Clean Architecture is Google-recommended. Room, Compose, Navigation patterns well-documented. |
| Pitfalls | **HIGH** | Wall of Shame, notification fatigue, object permanence, and decision paralysis extensively documented in ADHD user research 2025-2026. Subtask trap and inflexibility have mixed evidence but reported consistently. |

**Overall confidence:** **HIGH**

The stack and architecture recommendations are based on official sources and established patterns. Feature research synthesized multiple credible 2026 sources with strong consensus. Pitfall research draws from extensive ADHD user studies. The main uncertainty is in UX details that require user testing (notification timing, auto-archive thresholds, focus view algorithm tuning).

### Gaps to Address

**During Phase 1 planning:**
- **Auto-archive threshold:** Research suggests "old tasks fade" but doesn't specify optimal timing (3 days? 7 days? 14 days?). **Mitigation:** Start with 14 days (conservative), make configurable in Phase 6, gather user feedback.
- **Dopamine reward intensity:** Balance between "satisfying" and "overwhelming." **Mitigation:** Start subtle (simple animation, color change), iterate based on engagement metrics.

**During Phase 2 planning:**
- **Focus view limit:** Research suggests 3-5 tasks, but optimal number varies by user. **Mitigation:** Default to 3, make adjustable in Phase 6 settings.
- **Suggested next task algorithm:** Multiple viable approaches (deadline-based, recency-based, dependency-based). **Mitigation:** Start simple (deadline + creation time), iterate with user feedback.

**During Phase 5 planning:**
- **Capacity calculation:** How to estimate "available time" for ADHD users with unpredictable energy. **Mitigation:** Conservative estimate (6 hours of productive time per day default), user-adjustable.
- **Widget design:** Balance between information density and glanceability. **Mitigation:** Research Glance best practices, follow Material 3 widget guidelines, user testing.

**Post-MVP research needs:**
- **Notification strategies:** If adding beyond minimal defaults, needs user testing (too variable for desk research)
- **AI task breakdown prompts:** If implementing AI features, needs prompt engineering research
- **Calendar sync patterns:** If adding calendar integration, needs research into Calendar Provider API and sync adapters

**Known unknowns:**
- Actual user behavior patterns (solved through analytics and user interviews post-launch)
- Optimal defaults for ADHD-specific features (requires A/B testing with target users)
- Feature adoption rates (which differentiators actually drive retention)

## Sources

### Primary (HIGH confidence)

**Official Android/Kotlin Documentation:**
- [Kotlin 2.3.0 Release](https://blog.jetbrains.com/kotlin/2025/12/kotlin-2-3-0-released/)
- [Kotlin Releases](https://kotlinlang.org/docs/releases.html)
- [Compose Multiplatform 1.10.0](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/)
- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Data Layer Guide](https://developer.android.com/topic/architecture/data-layer)
- [Domain Layer Guide](https://developer.android.com/topic/architecture/domain-layer)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation)
- [Room KMP Support](https://developer.android.com/kotlin/multiplatform/room)
- [Save UI States](https://developer.android.com/topic/libraries/architecture/saving-states)

**Kotlin Libraries:**
- [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)
- [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)

### Secondary (MEDIUM confidence)

**ADHD Task Management Research (2026 sources):**
- [ADHD Task Management Apps 2026: 6 Best To-Do List Picks | AFFiNE](https://affine.pro/blog/adhd-task-management-apps)
- [7 Best ADHD Productivity Apps for Focus & Planning in 2026 | Morgen](https://www.morgen.so/blog-posts/adhd-productivity-apps)
- [Resource Roundup: Top ADHD Apps | AuDHD Psychiatry](https://www.audhdpsychiatry.co.uk/top-adhd-apps/)
- [5 to-do list apps that actually work with ADHD | Zapier](https://zapier.com/blog/adhd-to-do-list/)
- [To-Do Lists for ADHD: We Tested The 7 Best Apps | Saner.AI](https://www.saner.ai/blogs/best-todo-lists-for-adhd)
- [Top 10 Best Productivity Apps for ADHD in 2026](https://elephas.app/blog/best-productivity-apps-adhd)

**Neurodivergent UX Design:**
- [Neurodiversity in UX | Inclusive Design Principles](https://www.aufaitux.com/blog/neuro-inclusive-ux-design/)
- [Neurodiversity In UX: 7 Key Design Principles](https://devqube.com/neurodiversity-in-ux/)
- [Embracing Neurodiversity in UX Design | UXmatters](https://www.uxmatters.com/mt/archives/2024/04/embracing-neurodiversity-in-ux-design-crafting-inclusive-digital-environments.php)

**ADHD-Specific Challenges:**
- [Why ADHD Brains Struggle With Task Management (And Why Most Systems Make It Worse)](https://medium.com/@Adhd.taskflow/why-adhd-brains-struggle-with-task-management-and-why-most-systems-make-it-worse-4668ff042017)
- [ADHD Time Blindness: How to Detect It & Regain Control Over Time - ADDA](https://add.org/adhd-time-blindness/)
- [Object Permanence & ADHD: "Out of Sight, Out of Mind"](https://attncenter.nyc/object-permanence-and-adhd-the-out-of-sight-out-of-mind-phenomenon-explained/)
- [ADHD Paralysis Is Real: Here Are 8 Ways to Overcome It](https://add.org/adhd-paralysis/)

**Gamification & Motivation:**
- [Gamification ADHD: How to make tasks easier to start - Tiimo](https://www.tiimoapp.com/resource-hub/gamification-adhd)
- [How Gamification in ADHD Apps Can Boost User Retention](https://imaginovation.net/blog/gamification-adhd-apps-user-retention/)

**Visual & Time Features:**
- [Visual Timers for ADHD](https://visualtimer.com/timers/visual-timers-for-adhd/)
- [7 ADHD Time Management Tools to Boost Focus in 2026](https://tivazo.com/blogs/adhd-time-management-tools/)
- [Tiimo: Visual Planner for ADHD](https://www.tiimoapp.com/)

**Context Switching & Notifications:**
- [Context Switching: Why It Kills Productivity & How to Fix (2026 Guide) | Reclaim](https://reclaim.ai/blog/context-switching)
- [How To Better Use Reminders & Notifications with ADHD](https://imbusybeingawesome.com/adhd-reminders/)

**Shame & Emotional Impact:**
- [ADHD and Shame: Why It Happens and What to Do](https://psychcentral.com/adhd/reducing-one-of-the-most-painful-symptoms-of-adhd)
- [Why ADHD and Shame Are So Deeply Connected + How to Heal It](https://www.simplypsychology.org/adhd-and-shame.html)
- [Shame Spiraling And ADHD](https://www.joonapp.io/post/shame-spiraling-and-adhd)

### Tertiary (reference materials)

**Architecture Patterns:**
- [Room Polymorphism Patterns](https://commonsware.com/AndroidArch/pages/chap-roompoly-002)
- [ViewModel SavedState](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate)

**Competitive Analysis:**
- [Best ADHD Time Management Apps in 2026](https://toolfinder.co/lists/adhd-time-management-apps)
- [Hypermonkey: ADHD Productivity App](https://apps.apple.com/us/app/hypermonkey-adhd-productivity/id6736893516)
- [Best ADHD Planner and Productivity App | Lunatask](https://lunatask.app/adhd)
- [RoutineFlow: Routine for ADHD](https://play.google.com/store/apps/details?id=app.routineflow.routineflow&hl=en_US)

---
*Research completed: 2026-01-28*
*Ready for roadmap: yes*
