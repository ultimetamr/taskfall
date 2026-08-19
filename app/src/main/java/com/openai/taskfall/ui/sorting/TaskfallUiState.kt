package com.openai.taskfall.ui.sorting

import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.TaskCard
import com.openai.taskfall.domain.model.TaskfallPhase

data class TaskfallUiState(
    val phase: TaskfallPhase = TaskfallPhase.HOME,
    val inputText: String = "",
    val cards: List<TaskCard> = emptyList(),
    val spawnedCardCount: Int = 0,
    val activeCardId: Int? = null,
    val reduceMotion: Boolean = false,
    val showBreakPrompt: Boolean = false,
    val sessionStartedAtEpochMs: Long? = null,
    val recentSessionCount: Int = 0,
)

sealed interface TaskfallEvent {
    data object StartCapture : TaskfallEvent
    data object ContinueOnboarding : TaskfallEvent
    data class InputChanged(val input: String) : TaskfallEvent
    data object StartSorting : TaskfallEvent
    data class Grab(val id: Int) : TaskfallEvent
    data class Drop(val id: Int, val bucket: TaskBucket) : TaskfallEvent
    data class CycleBucket(val id: Int) : TaskfallEvent
    data object AutoDecide : TaskfallEvent
    data object Pause : TaskfallEvent
    data object Resume : TaskfallEvent
    data object Reset : TaskfallEvent
    data object Finish : TaskfallEvent
    data object DismissBreakPrompt : TaskfallEvent
    data object ToggleReduceMotion : TaskfallEvent
    data object ComfortTick : TaskfallEvent
    data object SpawnTick : TaskfallEvent
    data object ExitToHome : TaskfallEvent
}
