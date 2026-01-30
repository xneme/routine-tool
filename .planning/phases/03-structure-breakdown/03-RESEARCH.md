# Phase 3: Structure & Breakdown - Research

**Researched:** 2026-01-30
**Domain:** Room one-to-many relationships, Compose drag-and-drop, haptic feedback, completion tracking
**Confidence:** HIGH

## Summary

Phase 3 adds subtask management to tasks using a one-to-many Room relationship. The standard approach uses separate entity tables with @Relation for querying and @Transaction for atomicity. Drag-to-reorder in Compose is best handled by a third-party library (sh.calvin.reorderable) which uses long-press gestures and the new Modifier.animateItem API for smooth animations. Haptic feedback uses View.performHapticFeedback() with HapticFeedbackConstants.CONFIRM for task completion and lighter constants for subtask interactions. Timestamp tracking follows the existing pattern of Long (epoch millis) in Room entities, converted to Instant in the repository layer.

The architecture builds on established patterns from Phase 1 (Room entity separation, timestamp handling) and Phase 2 (ExpandableSection composable, AnimatedVisibility patterns). No new architectural paradigms are needed - this is primarily schema expansion and UI composition.

**Primary recommendation:** Use separate SubtaskEntity table with foreign key to TaskEntity, use Calvin-LL/Reorderable library for drag-and-drop, implement position-based ordering with fractional indexing approach for reordering.

## Standard Stack

The established libraries/tools for this domain:

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Room | 2.7.0 (current) | Database one-to-many relationships | Official Android ORM with @Relation support |
| sh.calvin.reorderable | 3.0.0 | Drag-to-reorder in Compose | Uses new animateItem API, active maintenance, long-press support |
| HapticFeedbackConstants | Android SDK | Tactile feedback | System-integrated, no permission needed |
| kotlinx-datetime | 0.7.1 (current) | Timestamp handling | Already in project, type-safe instant conversion |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Modifier.dropShadow | Compose UI | Visual elevation during drag | For drag feedback if built-in elevation insufficient |
| DataStore Preferences | 1.1.1 (current) | Persist expansion state | Already in project for section expansion |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| sh.calvin.reorderable | aclassen/ComposeReorderable | Older API, doesn't use Modifier.animateItem |
| Fractional indexing | Gap buffering (integers) | More arbitrary, slightly more complex |
| Separate table | @Embedded list | Poor performance, no independent queries |

**Installation:**
```bash
# Add to gradle/libs.versions.toml
[versions]
reorderable = "3.0.0"

[libraries]
reorderable = { module = "sh.calvin.reorderable:reorderable", version.ref = "reorderable" }

# Add to app/build.gradle.kts dependencies
implementation(libs.reorderable)
```

## Architecture Patterns

### Recommended Project Structure
```
data/local/entities/
├── TaskEntity.kt              # Existing parent entity
├── SubtaskEntity.kt           # New child entity
└── TaskWithSubtasks.kt        # Relation model for querying

domain/model/
├── Task.kt                    # Existing domain model
├── Subtask.kt                 # New domain model (title, completed, position)
└── TaskWithSubtasks.kt        # Domain model with subtasks

data/repository/
└── TaskRepository.kt          # Extended with subtask CRUD operations

ui/components/
└── SubtaskList.kt             # New composable for subtask checklist
```

### Pattern 1: One-to-Many Room Relationship with Foreign Key
**What:** TaskEntity (parent) has many SubtaskEntity (child), joined with @Relation
**When to use:** When child records need independent querying and modification

**Example:**
```kotlin
// Source: https://developer.android.com/training/data-storage/room/relationships/one-to-many

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,        // Foreign key reference
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val position: Float,       // For manual reordering
    val createdAt: Long = System.currentTimeMillis()
)

// Relation model for querying
data class TaskWithSubtasksEntity(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity>
)

// DAO query with @Transaction
@Transaction
@Query("SELECT * FROM tasks WHERE id = :taskId")
suspend fun getTaskWithSubtasks(taskId: String): TaskWithSubtasksEntity?
```

### Pattern 2: Manual Reordering with Fractional Indexing
**What:** Store Float position for each subtask, calculate midpoint when reordering
**When to use:** User-controlled ordering that needs to persist

**Example:**
```kotlin
// Source: https://hollos.dev/blog/fractional-indexing-a-solution-to-sorting/
// Concept adapted to Room

suspend fun reorderSubtask(
    subtaskId: String,
    newIndex: Int,
    allSubtasks: List<SubtaskEntity>
) {
    val sorted = allSubtasks.sortedBy { it.position }

    val newPosition = when {
        newIndex == 0 -> sorted.firstOrNull()?.position?.minus(1f) ?: 0f
        newIndex >= sorted.size -> sorted.lastOrNull()?.position?.plus(1f) ?: sorted.size.toFloat()
        else -> {
            val prev = sorted[newIndex - 1].position
            val next = sorted[newIndex].position
            (prev + next) / 2f
        }
    }

    subtaskDao.updatePosition(subtaskId, newPosition)
}

// Query ordered by position
@Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC")
fun observeSubtasks(taskId: String): Flow<List<SubtaskEntity>>
```

### Pattern 3: Long-Press Drag with Reorderable Library
**What:** Use sh.calvin.reorderable for drag-to-reorder with long-press activation
**When to use:** Reorderable lists in LazyColumn without visible drag handles

**Example:**
```kotlin
// Source: https://github.com/Calvin-LL/Reorderable

val lazyListState = rememberLazyListState()
val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
    viewModel.reorderSubtask(from.index, to.index)
}

LazyColumn(state = lazyListState) {
    items(subtasks, key = { it.id }) { subtask ->
        ReorderableItem(reorderableState, key = subtask.id) { isDragging ->
            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

            SubtaskRow(
                subtask = subtask,
                onToggle = { viewModel.toggleSubtask(subtask.id) },
                modifier = Modifier
                    .longPressDraggableHandle(
                        onDragStarted = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        },
                        onDragStopped = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE)
                        }
                    )
                    .shadow(elevation)
            )
        }
    }
}
```

### Pattern 4: Lightweight Haptic Feedback for Subtasks
**What:** Use lighter haptic constants for secondary interactions
**When to use:** Distinguishing importance between task and subtask completion

**Example:**
```kotlin
// Source: https://developer.android.com/develop/ui/views/haptics/haptic-feedback

// Existing task completion (keep as-is)
view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

// New subtask completion (lighter)
view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

// Drag start
view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

// Drag end
view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE)
```

### Pattern 5: Inline Text Field for Adding Items
**What:** TextField at end of list that adds item on Enter/Done
**When to use:** Quick sequential item entry without navigation

**Example:**
```kotlin
// Source: https://developer.android.com/develop/ui/compose/touch-input/keyboard-input/commands

var newSubtaskTitle by remember { mutableStateOf("") }

OutlinedTextField(
    value = newSubtaskTitle,
    onValueChange = { newSubtaskTitle = it },
    placeholder = { Text("Add subtask") },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(
        onDone = {
            if (newSubtaskTitle.isNotBlank()) {
                viewModel.addSubtask(newSubtaskTitle.trim())
                newSubtaskTitle = ""
            }
        }
    ),
    modifier = Modifier
        .fillMaxWidth()
        .onKeyEvent { event ->
            if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                if (newSubtaskTitle.isNotBlank()) {
                    viewModel.addSubtask(newSubtaskTitle.trim())
                    newSubtaskTitle = ""
                }
                true
            } else false
        }
)
```

### Anti-Patterns to Avoid
- **@Embedded list of subtasks in TaskEntity:** Poor performance, can't query subtasks independently, no proper foreign keys
- **Renumbering all positions on reorder:** Update only moved item using fractional indexing
- **Same haptic for task and subtask completion:** Reduces information value, makes interactions feel uniform
- **Visible drag handles:** CONTEXT.md specifies long-press only, no visible handles
- **Auto-completing parent when all subtasks done:** CONTEXT.md explicitly states independence

## Don't Hand-Roll

Problems that look simple but have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Drag-to-reorder in LazyColumn | Custom gesture detection + position calculation | sh.calvin.reorderable | Handles scroll boundaries, animates moves with animateItem, manages gesture conflicts |
| Manual ordering positions | Integer renumbering on every move | Fractional indexing (Float midpoint) | Only updates moved item, infinite precision for practical use |
| Room transaction management | Manual transaction wrapping | @Transaction annotation | Room optimizes multi-query operations automatically |
| Shadow/elevation during drag | Custom drawing code | Modifier.shadow() with animateDpAsState | Material-compliant, respects theme, GPU-accelerated |
| Haptic feedback | Vibrator service API | View.performHapticFeedback(HapticFeedbackConstants) | No VIBRATE permission needed, respects system settings |

**Key insight:** Drag-and-drop in Compose is deceptively complex - scroll boundaries, gesture conflicts, and animation coordination require significant testing. sh.calvin.reorderable solves these and uses the latest animateItem API (introduced in recent Compose versions) for smooth, modern animations.

## Common Pitfalls

### Pitfall 1: Forgetting @Transaction on Relation Queries
**What goes wrong:** Room runs multiple queries (parent, then children), risking inconsistent data if rows change between queries
**Why it happens:** @Relation looks like one query but Room expands it to multiple SELECT statements
**How to avoid:** Always annotate DAO methods returning @Relation models with @Transaction
**Warning signs:** Occasional missing subtasks, inconsistent counts, hard-to-reproduce bugs

### Pitfall 2: Not Indexing Foreign Key Columns
**What goes wrong:** Room shows "missing index" warnings in build output, query performance degrades with data growth
**Why it happens:** Room checks for indices on foreign key columns but doesn't create them automatically
**How to avoid:** Add indices in @Entity annotation: `@Entity(tableName = "subtasks", indices = [Index("taskId")])`
**Warning signs:** Build warnings, slow subtask loading on tasks with many items

### Pitfall 3: Collision Precision Loss with Fractional Indexing
**What goes wrong:** After ~53 consecutive moves between same two items, float precision exhausts, positions converge
**Why it happens:** Repeated halving reduces precision until values become identical
**How to avoid:** Detect when gap < threshold (e.g., 0.0001f), trigger batch renumbering of surrounding items
**Warning signs:** Items refusing to reorder after many operations, duplicate position values

### Pitfall 4: Forgetting to Order By Position in Queries
**What goes wrong:** Subtasks display in insertion order or random database order, not user's chosen order
**Why it happens:** Room doesn't enforce ordering, returns rows in database default order
**How to avoid:** Always include `ORDER BY position ASC` in subtask queries
**Warning signs:** Subtask order resets on app restart, inconsistent ordering across sessions

### Pitfall 5: Cascading Deletes Without Foreign Key Constraint
**What goes wrong:** Deleting a task leaves orphaned subtasks in database, growing dead data over time
**Why it happens:** Room doesn't enforce referential integrity unless explicit ForeignKey defined
**How to avoid:** Add ForeignKey with CASCADE: `@Entity(foreignKeys = [ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)])`
**Warning signs:** Database size grows unexpectedly, subtask queries return items with non-existent taskIds

### Pitfall 6: Not Handling Empty Position Lists
**What goes wrong:** First subtask fails to save with null/NaN position when calculating from empty list
**Why it happens:** Position calculation assumes at least one existing item
**How to avoid:** Check list emptiness, default first item to position 0f or 1f
**Warning signs:** Crash on adding first subtask, null position errors

### Pitfall 7: Reorderable Library Index Offset Issues
**What goes wrong:** Moving subtasks updates wrong positions if section headers or other items in same LazyColumn
**Why it happens:** from.index and to.index are LazyColumn positions, not data list positions
**How to avoid:** Adjust indices if mixing subtasks with headers: `val dataIndex = from.index - headerCount`
**Warning signs:** Wrong items reorder, positions corrupt when dragging

## Code Examples

Verified patterns from official sources:

### Complete SubtaskEntity with Foreign Key
```kotlin
// Source: https://developer.android.com/training/data-storage/room/relationships/one-to-many
// Source: https://medium.com/@vontonnie/connecting-room-tables-using-foreign-keys-c19450361603

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE  // Delete subtasks when task deleted
        )
    ],
    indices = [Index("taskId")]  // Required for FK performance
)
data class SubtaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val position: Float,
    val createdAt: Long = System.currentTimeMillis()
)
```

### Repository Subtask Operations
```kotlin
// Source: Project patterns from TaskRepository.kt

suspend fun addSubtask(taskId: String, title: String) {
    val existingSubtasks = subtaskDao.getSubtasks(taskId)
    val maxPosition = existingSubtasks.maxOfOrNull { it.position } ?: 0f

    val subtask = SubtaskEntity(
        taskId = taskId,
        title = title,
        position = maxPosition + 1f
    )
    subtaskDao.insert(subtask)
}

suspend fun toggleSubtask(subtaskId: String) {
    val subtask = subtaskDao.getById(subtaskId) ?: return

    if (subtask.isCompleted) {
        subtaskDao.update(subtask.copy(isCompleted = false, completedAt = null))
    } else {
        val now = System.currentTimeMillis()
        subtaskDao.update(subtask.copy(isCompleted = true, completedAt = now))
    }
}
```

### Progress Indicator for Collapsed Tasks
```kotlin
// User-specified pattern from CONTEXT.md
// Row of checkboxes for ≤6 subtasks, text count for >6

@Composable
fun SubtaskProgressIndicator(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    if (totalCount == 0) return

    if (totalCount <= 6) {
        // Row of checkboxes
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(totalCount) { index ->
                if (index < completedCount) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    } else {
        // Text count
        Text(
            text = "$completedCount/$totalCount",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}
```

### Drag Visual Feedback with Shadow
```kotlin
// Source: https://developer.android.com/develop/ui/compose/graphics/draw/shadows

ReorderableItem(reorderableState, key = subtask.id) { isDragging ->
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 200)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .longPressDraggableHandle()
    ) {
        // Subtask content
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Modifier.dragAndDropSource/Target | sh.calvin.reorderable library | 2024-2025 | Simpler API, built-in list handling, animateItem integration |
| Integer gap buffering (16384) | Float fractional indexing | Ongoing best practice | Simpler logic, no arbitrary constants |
| Vibrator service API | HapticFeedbackConstants | Long-standing best practice | No permission needed, system integration |
| Manual transaction wrapping | @Transaction annotation | Room 2.x | Automatic optimization, cleaner code |
| Modifier.shadow() only | Modifier.dropShadow() available | Compose UI 2024 | More customization options for drag feedback |

**Deprecated/outdated:**
- **Vibrator.vibrate(long)**: Use View.performHapticFeedback() or VibrationEffect for programmatic vibration
- **ReorderableCollection from accompanist**: Merged/superseded by third-party libraries with modern APIs
- **@TypeConverter for nested lists**: Use separate tables with @Relation for proper querying and performance

## Open Questions

Things that couldn't be fully resolved:

1. **Soft limit threshold for "too many subtasks" warning**
   - What we know: User wants warning around ~20 subtasks suggesting task is too complex
   - What's unclear: Exact threshold (18? 20? 25?), warning behavior (dismissible? blocking?)
   - Recommendation: Start with 20 exact, show non-blocking warning with "Add anyway" option

2. **Specific wording for complexity warning**
   - What we know: Should suggest task might be too complex to manage
   - What's unclear: Tone (helpful vs discouraging), specific message text
   - Recommendation: "This task has many steps. Consider breaking it into separate tasks for better focus." (Marked as Claude's discretion in CONTEXT.md)

3. **Exact animation timing for drag feedback**
   - What we know: Should have elevated shadow + gap indicator during drag
   - What's unclear: Shadow animation duration, gap indicator fade timing
   - Recommendation: 200ms for shadow elevation change (feels responsive without jarring), gap indicator instant (Marked as Claude's discretion in CONTEXT.md)

4. **Fractional indexing precision threshold**
   - What we know: Need to detect when float gap becomes too small
   - What's unclear: Exact threshold value, renumbering strategy
   - Recommendation: Trigger renumbering when gap < 0.0001f, renumber surrounding 10 items with spacing 1.0f

5. **Haptic feedback for task completion not working**
   - What we know: User reported this as a bug, deferred to future investigation
   - What's unclear: Whether code exists but is broken, or never implemented
   - Recommendation: Verify implementation exists in TaskCard.kt during Phase 3 work, fix if broken (code review shows implementation exists on line 114 and 130, may be system settings issue)

## Sources

### Primary (HIGH confidence)
- [Android Room one-to-many relationships](https://developer.android.com/training/data-storage/room/relationships/one-to-many) - Official docs
- [Android haptic feedback](https://developer.android.com/develop/ui/views/haptics/haptic-feedback) - Official docs
- [Compose shadows](https://developer.android.com/develop/ui/compose/graphics/draw/shadows) - Official docs
- [Calvin-LL/Reorderable GitHub](https://github.com/Calvin-LL/Reorderable) - Library documentation
- Existing codebase (TaskEntity.kt, TaskRepository.kt, AddTaskScreen.kt) - Established patterns

### Secondary (MEDIUM confidence)
- [Fractional indexing explained](https://hollos.dev/blog/fractional-indexing-a-solution-to-sorting/) - Reordering approach
- [Room ForeignKey CASCADE](https://medium.com/@vontonnie/connecting-room-tables-using-foreign-keys-c19450361603) - Implementation details
- [Compose keyboard actions](https://developer.android.com/develop/ui/compose/touch-input/keyboard-input/commands) - TextField handling
- [Room timestamp patterns](https://medium.com/@stephenja/timestamps-with-android-room-f3fd57b48250) - Timestamp best practices

### Tertiary (LOW confidence)
- [ComposeReorderable library](https://github.com/aclassen/ComposeReorderable) - Alternative approach (older API)
- [Room embedded vs relation performance](https://medium.com/@hassanwasfy/room-database-using-embedded-vs-relation-for-nested-data-in-android-3fbdf2125cb7) - Architecture tradeoffs
- WebSearch results on drag-drop implementation approaches - Community patterns

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Official Room docs, active library with recent updates, existing project dependencies
- Architecture: HIGH - Official Android patterns, project already uses Room/Repository/Domain separation
- Pitfalls: MEDIUM - Based on official warnings and community experience, not all verified in this codebase
- Reordering approach: MEDIUM - Fractional indexing is community best practice, not official Android guidance

**Research date:** 2026-01-30
**Valid until:** 2026-02-28 (30 days for stable Room/Compose patterns, library may update but API stable)
