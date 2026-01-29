package com.routinetool.domain.model

enum class SortOption(val displayName: String) {
    URGENCY("Urgency"),
    DEADLINE("Deadline"),
    CREATED("Created");

    fun comparator(): Comparator<Task> = when (this) {
        URGENCY -> compareBy(
            { task ->
                val nearestDeadline = listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull()
                if (nearestDeadline != null && nearestDeadline.toEpochMilliseconds() < System.currentTimeMillis()) 0 else 1
            },
            { task -> listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull()?.toEpochMilliseconds() ?: Long.MAX_VALUE }
        )
        DEADLINE -> compareBy { task ->
            listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull()?.toEpochMilliseconds() ?: Long.MAX_VALUE
        }
        CREATED -> compareByDescending { it.createdAt.toEpochMilliseconds() }
    }
}
