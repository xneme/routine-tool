# Feature Landscape

**Domain:** Neurodivergent-focused task/routine management app
**Researched:** 2026-01-28
**Confidence:** MEDIUM (based on comprehensive WebSearch across multiple 2026 sources)

## Table Stakes

Features users expect. Missing = product feels incomplete or actively harmful for neurodivergent users.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Quick Capture** | ADHD brains lose thoughts in seconds. Must capture before thought disappears. | Low | Natural language input ("Call GP tomorrow at 10am"). Voice capture reduces friction. |
| **Task Breakdown / Subtasks** | Large tasks trigger paralysis. Breaking into 5-minute steps bypasses overwhelm. | Medium | AI-assisted breakdown is becoming standard in 2026. Manual breakdown is table stakes. |
| **Visual Organization** | ADHD brains struggle with object permanence. Hidden = forgotten. Visual = remembered. | Medium | Color-coding, icon-based schedules, Kanban boards. NOT nested folders or hidden lists. |
| **Time Awareness** | Time blindness means ADHD users can't estimate duration. Must show time externally. | Medium | Countdown timers, time estimates per task, visual time representation. |
| **Flexible/Forgiving Structure** | Rigid systems break when one thing goes wrong. Must adapt to reality. | Medium | No penalties for missed tasks. Easy to reschedule. Accepts that plans change. |
| **Dopamine Rewards** | ADHD = low dopamine. Without immediate rewards, tasks feel impossible to start. | Low-Medium | Check-offs, streaks, progress bars, satisfying interactions. NOT complex gamification. |
| **Focus View** | Seeing 20 tasks = sensory overload = paralysis. Must show only what's relevant now. | Medium | "Today" view, "Next 3 tasks" view, capacity-aware daily limits. |
| **Repeating Tasks** | Routines are essential but hard to remember. Must support recurring patterns. | Medium | Daily/weekly/custom recurrence. NOT just calendar events—actual task templates. |

## Differentiators

Features that set products apart for neurodivergent users specifically. Not expected, but highly valued.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **AI Task Breakdown** | Speaking overwhelming task → AI generates concrete steps with time estimates. Removes planning paralysis. | High | Tiimo, AFFiNE offer this in 2026. Competitive differentiator. |
| **Quota-Based Tasks** | "Exercise 3x this week" instead of "Exercise Monday/Wednesday/Friday". Flexible completion reduces shame. | Medium | Addresses rigid scheduling anxiety. Not common but highly valued. |
| **Cooldown Timers** | After completing task, shows rest period before next one. Prevents burnout from hyperfocus. | Low | Acknowledges that ADHD brains need transitions. Rare feature. |
| **Completion History** | Shows what you DID accomplish, not just what's pending. Counters "Wall of Shame" effect. | Low-Medium | Provides dopamine from seeing progress. Reduces guilt. |
| **Task Dependencies** | "Can't do B until A is done." External structure for overwhelmed brains. | High | Complex to implement but powerful for executive dysfunction. |
| **Energy/Focus Mapping** | Schedule tasks around natural rhythms (morning focus vs afternoon crash). | Medium | Addresses reality of ADHD energy fluctuations. Growing trend in 2026. |
| **Brain Dump Space** | Unstructured capture area separate from organized lists. Prevents organization paralysis during capture. | Low | AFFiNE's "Edgeless Mode", Hypermonkey's "Yap Zone". Lets thoughts flow before structure. |
| **Capacity Management** | Warns when daily plan exceeds realistic time. Prevents over-scheduling to burnout. | Medium | Sunsama does this well. Critical for ADHD users who can't estimate capacity. |
| **Unified Calendar/Tasks** | All tasks + events in one view. Prevents context-switching that breaks ADHD focus. | High | Morgen, Sunsama leading this in 2026. Addresses "four mental models" problem. |
| **Body Doubling Support** | Virtual co-working, accountability partners, social commitment features. | Medium | Addresses isolation and lack of external accountability. |
| **Shame-Free Missed Tasks** | No punishment for incomplete tasks. Easy to reschedule without guilt messaging. | Low | Critical for ADHD users who already struggle with self-criticism. |
| **Progressive Disclosure** | Start simple, reveal complexity only when needed. Prevents overwhelm from feature overload. | Medium | Key UX pattern for neurodivergent users per 2026 research. |

## Anti-Features

Features to explicitly NOT build. Common mistakes that make apps worse for neurodivergent users.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| **Notification Bombardment** | ADHD brains already overwhelmed. Push notifications → anxiety → ignore all notifications → miss important ones. | Gentle, contextual reminders. WhatsApp/SMS for critical items. User control over notification density. Allow disabling per-task. |
| **Nested Folders/Hidden Tasks** | Object permanence issues mean "out of sight = doesn't exist." Tasks buried in folders are forgotten forever. | Flat visual hierarchy. Tags/filters instead of folders. Everything visible in main view. |
| **Static Long Lists** | Seeing 50 undone tasks = "Wall of Shame" = app avoidance = failure spiral. | Focus view showing only 3-5 next actions. Auto-archive old tasks. Emphasize "Today" over "All." |
| **Rigid Scheduling** | Life is chaotic with ADHD. Fixed schedules break, causing guilt and abandonment. | Flexible time windows. "Sometime this week" options. Easy rescheduling without friction. |
| **Complex Feature Overload** | Too many features = decision paralysis = overwhelm = abandonment. | Simple core, progressive disclosure. Hide advanced features until needed. |
| **Perfectionist Language** | "Complete your daily streak!" creates shame when inevitable failure happens. | Neutral, supportive language. "Ready to try again?" not "You broke your streak." |
| **Mandatory Multi-Step Setup** | ADHD users abandon during long onboarding. The interest that sparked trying the app expires before setup finishes. | Instant utility. Add first task in 10 seconds. Progressive onboarding as features are needed. |
| **Cross-App Task Management** | Switching between Notion + Todoist + Calendar = context switching = broken focus = tasks lost in transitions. | Unified interface. Import from other sources but provide single source of truth. |
| **Gamification Complexity** | Simple rewards = dopamine. Complex point systems/levels = cognitive load = abandonment. | Satisfying check-offs, simple streaks, visual progress. NOT elaborate leveling systems. |
| **Auto-Scheduling Without Control** | AI moving tasks without consent = loss of control = anxiety for ADHD users who need predictability. | AI suggests, user approves. Never force automated changes to schedule. |
| **Lack of Visual Differentiation** | Text-only lists blur together. ADHD brains struggle to distinguish similar items without visual cues. | Colors, icons, visual timelines. Make categories instantly distinguishable at a glance. |

## Feature Dependencies

Understanding how features build on each other:

```
Core Foundation Layer:
├─ Quick Capture (enables everything else - must work instantly)
│  └─ Brain Dump Space (capture without structure pressure)
│
├─ Task Storage & Display
│  ├─ Visual Organization (flat hierarchy, color-coding)
│  └─ Focus View (filter to manageable subset)
│
├─ Basic Task Properties
│  ├─ Task Breakdown/Subtasks (reduces overwhelm)
│  ├─ Time Estimates (for capacity management)
│  └─ Repeating Tasks (for routines)

Advanced Layer (builds on foundation):
├─ Task Dependencies (requires subtasks + time estimates)
├─ Capacity Management (requires time estimates + calendar integration)
├─ Quota-Based Tasks (builds on repeating tasks)
├─ Cooldown Timers (requires task completion tracking)
├─ Completion History (requires completion tracking + time data)
└─ AI Features
   ├─ AI Task Breakdown (enhances manual subtasks)
   └─ Energy/Focus Mapping (requires completion history data)

Integration Layer:
├─ Unified Calendar/Tasks (requires calendar sync)
└─ Body Doubling (requires user accounts + social features)
```

## Neurodivergent-Specific Considerations

### Cognitive Load Management
- **Principle:** Every decision costs mental energy. ADHD users have limited decision-making capacity.
- **Application:** Default to simplest path. Minimize clicks. Provide sensible defaults. Use progressive disclosure.

### Time Blindness Accommodation
- **Principle:** ADHD brains cannot internally estimate time passage or duration.
- **Application:** External time visualization (countdown timers, visual shrinking disks). Always show estimated duration. Alert before transitions.

### Object Permanence Issues
- **Principle:** "Out of sight, out of mind" is literal for ADHD brains.
- **Application:** Visual task boards over folders. Persistent "Today" view. Avoid pagination where tasks disappear on page 2.

### Dopamine-Seeking Behavior
- **Principle:** ADHD = dopamine deficit. Tasks without immediate reward feel impossible to start.
- **Application:** Satisfying check-offs. Visual progress indicators. Small wins celebrated. NOT complex delayed rewards.

### Rejection Sensitive Dysphoria (RSD)
- **Principle:** ADHD users experience criticism/failure as intensely painful. Apps can trigger RSD.
- **Application:** Shame-free language. No punishment for missed tasks. Celebrate attempts, not just completions.

### Sensory Overload Prevention
- **Principle:** ADHD brains are easily overwhelmed by visual/auditory stimuli.
- **Application:** Clean interfaces. User control over colors/sounds. Minimize animations. Allow "quiet mode."

### Executive Function Support
- **Principle:** ADHD users struggle with planning, prioritization, task initiation, and transitions.
- **Application:** AI-assisted breakdown. External structure (dependencies). Transition support (cooldown timers). Prioritization frameworks (Eisenhower Matrix).

## MVP Recommendation

For MVP targeting neurodivergent users, prioritize in this order:

### Phase 1: Core Capture & Organization (MVP Foundation)
1. **Quick Capture** - Must work instantly or users lose thoughts
2. **Task Breakdown/Subtasks** - Essential for overcoming paralysis
3. **Visual Organization** - Color-coding, flat hierarchy (not folders)
4. **Focus View** - Show only 3-5 next tasks to prevent overwhelm
5. **Repeating Tasks** - Table stakes for routine management

**Why this order:** These five features address the core ADHD problems: forgetting thoughts, overwhelm from big tasks, visual disorganization, sensory overload from long lists, and difficulty maintaining routines.

### Phase 2: Time & Capacity Management
6. **Time Estimates** - Required for capacity management
7. **Countdown Timers** - Addresses time blindness
8. **Capacity Management** - Prevents over-scheduling to burnout

**Why next:** Without time awareness features, ADHD users overcommit and burn out, abandoning the app.

### Phase 3: Motivation & Progress
9. **Dopamine Rewards** - Simple check-offs, streaks
10. **Completion History** - Shows accomplishments, counters shame
11. **Quota-Based Tasks** - Differentiator for flexible routines

**Why next:** Motivation features keep users returning. History counters the "Wall of Shame" effect.

### Defer to Post-MVP:

**High Value but Complex:**
- **Task Dependencies**: High complexity, medium adoption rate
- **AI Task Breakdown**: High value but requires AI infrastructure
- **Unified Calendar Integration**: Complex integration work
- **Energy/Focus Mapping**: Requires historical data collection first

**Specialized Features:**
- **Body Doubling**: Requires social infrastructure
- **Brain Dump Space**: Can be addressed with tags/unstructured capture initially

**Already Deferred (per project context):**
- Anxiety vault
- Notifications (intentionally minimal)
- Sync (keeping local-first)

## Feature Implementation Pitfalls

### Task Breakdown Pitfalls
- **Trap:** Making subtasks look identical to parent tasks
- **Solution:** Visual hierarchy (indentation, smaller text, different color)

### Focus View Pitfalls
- **Trap:** "Today" view that shows 20 tasks = still overwhelming
- **Solution:** Hard limit (3-5 tasks) with explicit "add more" action

### Repeating Task Pitfalls
- **Trap:** Creating new task instance for every recurrence = list clutter
- **Solution:** Single task with completion history + "next due" date

### Dopamine Reward Pitfalls
- **Trap:** Complex gamification that requires understanding point systems
- **Solution:** Visceral, immediate feedback (satisfying animation, sound, visual change)

### Capacity Management Pitfalls
- **Trap:** Warning user they're overbooked AFTER they've planned their day
- **Solution:** Real-time capacity indicator during planning

## Competitive Landscape (2026)

### Leaders in ADHD Task Management:
- **Tiimo**: Visual planning with AI breakdown, icon-based schedules, countdown timers
- **Morgen**: Unified calendar/tasks, AI scheduling, capacity-aware Frames
- **Sunsama**: Guided daily planning, prevents overcommitment, mindful design
- **AFFiNE**: Visual whiteboard + database, AI breakdown, Edgeless brain dump
- **Lunatask**: Encrypted all-in-one with habit tracking, privacy-focused
- **Todoist**: Mainstream with good ADHD adaptability, Karma gamification

### Market Gaps (Opportunities):
1. **Quota-based task management**: Mentioned in project context, rare in market
2. **Cooldown timer focus**: Transition support underserved
3. **Shame-free design philosophy**: Most apps still use guilt-driven language
4. **Offline-first with no sync pressure**: Privacy + no "must sync" anxiety

## Sources

This research was conducted on 2026-01-28 using multiple current sources:

**ADHD Task Management Features:**
- [ADHD Task Management Apps 2026: 6 Best To-Do List Picks | AFFiNE](https://affine.pro/blog/adhd-task-management-apps)
- [7 Best ADHD Productivity Apps for Focus & Planning in 2026 | Morgen](https://www.morgen.so/blog-posts/adhd-productivity-apps)
- [Resource Roundup: Top ADHD Apps | AuDHD Psychiatry](https://www.audhdpsychiatry.co.uk/top-adhd-apps/)
- [5 to-do list apps that actually work with ADHD | Zapier](https://zapier.com/blog/adhd-to-do-list/)

**Neurodivergent UX Design:**
- [Neurodiversity in UX | Inclusive Design Principles](https://www.aufaitux.com/blog/neuro-inclusive-ux-design/)
- [Neurodiversity In UX: 7 Key Design Principles](https://devqube.com/neurodiversity-in-ux/)
- [Embracing Neurodiversity in UX Design | UXmatters](https://www.uxmatters.com/mt/archives/2024/04/embracing-neurodiversity-in-ux-design-crafting-inclusive-digital-environments.php)

**ADHD App Mistakes & Frustrations:**
- [ADHD apps mistakes and frustrations research](https://affine.pro/blog/adhd-task-management-apps)
- [Common ADHD task app problems](https://www.morgen.so/blog-posts/adhd-productivity-apps)

**Time Blindness & Visual Features:**
- [Visual Timers for ADHD](https://visualtimer.com/timers/visual-timers-for-adhd/)
- [7 ADHD Time Management Tools to Boost Focus in 2026](https://tivazo.com/blogs/adhd-time-management-tools/)
- [Tiimo: Visual Planner for ADHD](https://www.tiimoapp.com/)

**Gamification & Dopamine:**
- [Gamification ADHD: How to make tasks easier to start - Tiimo](https://www.tiimoapp.com/resource-hub/gamification-adhd)
- [How Gamification in ADHD Apps Can Boost User Retention](https://imaginovation.net/blog/gamification-adhd-apps-user-retention/)

**Task Breakdown & Subtasks:**
- [Hypermonkey: ADHD Productivity App](https://apps.apple.com/us/app/hypermonkey-adhd-productivity/id6736893516)
- [Best ADHD Planner and Productivity App | Lunatask](https://lunatask.app/adhd)

**Notification Design:**
- [ADHD and Memory: Visual Aids vs Phone Reminders](https://www.millennialtherapy.com/anxiety-therapy-blog/visual-memory-aids-adhd)
- [How To Better Use Reminders & Notifications with ADHD](https://imbusybeingawesome.com/adhd-reminders/)

**Prioritization & Scheduling:**
- [Eisenhower Matrix for ADHD | Tiimo](https://www.tiimoapp.com/resource-hub/how-to-prioritize-tasks-eisenhower-matrix)
- [The Eisenhower Matrix: Prioritizing Tasks with ADHD](https://www.thesummitpsychology.com/blog/the-eisenhower-matrix-prioritizing-tasks-with-adhd)

**Routine & Habit Management:**
- [RoutineFlow: Routine for ADHD](https://play.google.com/store/apps/details?id=app.routineflow.routineflow&hl=en_US)
- [Brili Routine Apps For Adults & Families with ADHD](https://brili.com/)

**Overall ADHD App Landscape:**
- [Best ADHD Time Management Apps in 2026](https://toolfinder.co/lists/adhd-time-management-apps)
- [Top 10 Best Productivity Apps for ADHD in 2026](https://elephas.app/blog/best-productivity-apps-adhd)
- [Best ADHD Apps & Tools for Adults (2026 Guide)](https://www.kantoko.com.au/articles/best-adhd-apps-tools-2025)

**Confidence Note:** MEDIUM confidence. Research based on comprehensive WebSearch across 30+ sources from 2026, cross-referenced across multiple domains. No single authoritative source exists for this domain (neurodivergent task management is emerging field), but multiple credible sources agree on core patterns. Anti-features derived from user research discussions and UX design principles rather than explicit "don't do this" documentation.
