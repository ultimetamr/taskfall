package com.openai.taskfall.domain.model

enum class TaskBucket(val label: String, val symbol: String) {
    TODAY("今天", "◷"),
    LATER("以后", "⌁"),
    DELEGATE("交给别人", "↗"),
    UNDECIDED("待决定", "?")
}

enum class TaskfallPhase { HOME, ONBOARDING, CAPTURE, SORTING, PAUSED, RESULT }

data class TaskCard(
    val id: Int,
    val rawText: String,
    val displayText: String,
    val bucket: TaskBucket? = null,
)

data class SessionSummary(
    val createdAtEpochMs: Long,
    val total: Int,
    val counts: Map<TaskBucket, Int>,
)

data class FallingCard(
    val card: TaskCard,
    val spawnIndex: Int,
    val spawnedAtEpochMs: Long,
)
