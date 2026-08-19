package com.openai.taskfall.domain.usecase

import com.openai.taskfall.domain.model.FallingCard
import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.TaskCard

private const val MAX_CARDS = 20
private const val MAX_CHINESE_CHARS = 26
private const val SPAWN_INTERVAL_MS = 2_000L

fun truncateTaskText(text: String): String {
    val normalized = text.trim().replace(Regex("\\s+"), " ")
    if (normalized.length <= MAX_CHINESE_CHARS) return normalized
    return normalized.take(MAX_CHINESE_CHARS - 1) + "…"
}

fun parseTaskInput(input: String): List<TaskCard> = input
    .lineSequence()
    .map(String::trim)
    .filter(String::isNotEmpty)
    .take(MAX_CARDS)
    .mapIndexed { index, text ->
        TaskCard(id = index, rawText = text, displayText = truncateTaskText(text))
    }
    .toList()

fun nextBucket(current: TaskBucket?): TaskBucket = when (current) {
    null -> TaskBucket.UNDECIDED
    TaskBucket.TODAY -> TaskBucket.LATER
    TaskBucket.LATER -> TaskBucket.DELEGATE
    TaskBucket.DELEGATE -> TaskBucket.UNDECIDED
    TaskBucket.UNDECIDED -> TaskBucket.TODAY
}

fun buildSpawnSchedule(cards: List<TaskCard>, nowEpochMs: Long): List<FallingCard> = cards.mapIndexed { index, card ->
    FallingCard(card = card, spawnIndex = index, spawnedAtEpochMs = nowEpochMs + (index * SPAWN_INTERVAL_MS))
}

class FallingCardSpawner(
    private val cards: List<TaskCard>,
    private val clockMs: () -> Long = System::currentTimeMillis,
) {
    private val schedule = buildSpawnSchedule(cards, clockMs())

    fun visibleCards(nowEpochMs: Long = clockMs()): List<FallingCard> = schedule.filter { it.spawnedAtEpochMs <= nowEpochMs }

    fun isComplete(nowEpochMs: Long = clockMs()): Boolean = visibleCards(nowEpochMs).size == cards.size
}

fun classifyCard(card: TaskCard, bucket: TaskBucket): TaskCard = card.copy(bucket = bucket)

fun summarize(cards: List<TaskCard>): Map<TaskBucket, Int> = TaskBucket.values().associateWith { bucket ->
    cards.count { it.bucket == bucket }
}
