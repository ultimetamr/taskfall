package com.openai.taskfall.ui.sorting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openai.taskfall.data.repository.TaskfallRepository
import com.openai.taskfall.domain.model.SessionSummary
import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.TaskfallPhase
import com.openai.taskfall.domain.usecase.classifyCard
import com.openai.taskfall.domain.usecase.nextBucket
import com.openai.taskfall.domain.usecase.parseTaskInput
import com.openai.taskfall.domain.usecase.summarize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskfallViewModel(
    private val repository: TaskfallRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskfallUiState())
    val uiState: StateFlow<TaskfallUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val input = repository.loadLastInput()
            val recent = repository.loadRecentSessions()
            val reduceMotion = repository.loadReduceMotion()
            _uiState.update { it.copy(inputText = input, recentSessionCount = recent.size, reduceMotion = reduceMotion) }
        }
    }

    fun onEvent(event: TaskfallEvent) {
        when (event) {
            TaskfallEvent.StartCapture -> _uiState.update { it.copy(phase = TaskfallPhase.ONBOARDING) }
            TaskfallEvent.ContinueOnboarding -> _uiState.update { it.copy(phase = TaskfallPhase.CAPTURE) }
            is TaskfallEvent.InputChanged -> _uiState.update { it.copy(inputText = event.input) }
            TaskfallEvent.StartSorting -> startSorting()
            is TaskfallEvent.Grab -> _uiState.update { it.copy(activeCardId = event.id) }
            is TaskfallEvent.Drop -> classify(event.id, event.bucket)
            is TaskfallEvent.CycleBucket -> {
                val card = _uiState.value.cards.firstOrNull { it.id == event.id } ?: return
                classify(event.id, nextBucket(card.bucket))
            }
            TaskfallEvent.AutoDecide -> autoDecide()
            TaskfallEvent.Pause -> _uiState.update { it.copy(phase = TaskfallPhase.PAUSED) }
            TaskfallEvent.Resume -> _uiState.update { it.copy(phase = TaskfallPhase.SORTING) }
            TaskfallEvent.Reset -> startSorting()
            TaskfallEvent.Finish -> finish()
            TaskfallEvent.DismissBreakPrompt -> _uiState.update { it.copy(showBreakPrompt = false) }
            TaskfallEvent.ToggleReduceMotion -> toggleReduceMotion()
            TaskfallEvent.ComfortTick -> checkComfort()
            TaskfallEvent.SpawnTick -> revealNextCard()
            TaskfallEvent.ExitToHome -> _uiState.update { TaskfallUiState(recentSessionCount = it.recentSessionCount) }
        }
    }

    private fun startSorting() {
        val cards = parseTaskInput(_uiState.value.inputText)
        if (cards.isEmpty()) return
        viewModelScope.launch { repository.saveLastInput(_uiState.value.inputText) }
        _uiState.update {
            it.copy(
                phase = TaskfallPhase.SORTING,
                cards = cards,
                spawnedCardCount = 1,
                activeCardId = null,
                sessionStartedAtEpochMs = System.currentTimeMillis(),
                showBreakPrompt = false,
            )
        }
    }

    private fun classify(id: Int, bucket: TaskBucket) {
        _uiState.update { state ->
            state.copy(
                cards = state.cards.map { card -> if (card.id == id) classifyCard(card, bucket) else card },
                activeCardId = null,
            )
        }
    }

    private fun autoDecide() {
        _uiState.update { state ->
            state.copy(cards = state.cards.map { card -> if (card.bucket == null) classifyCard(card, TaskBucket.UNDECIDED) else card })
        }
    }

    private fun revealNextCard() {
        _uiState.update { state ->
            val outgoingIndex = state.spawnedCardCount - 1
            val updatedCards = state.cards.mapIndexed { index, card ->
                if (index == outgoingIndex && card.bucket == null && card.id != state.activeCardId) {
                    classifyCard(card, TaskBucket.UNDECIDED)
                } else {
                    card
                }
            }
            state.copy(
                cards = updatedCards,
                spawnedCardCount = (state.spawnedCardCount + 1).coerceAtMost(state.cards.size),
            )
        }
    }

    private fun toggleReduceMotion() {
        val enabled = !_uiState.value.reduceMotion
        _uiState.update { it.copy(reduceMotion = enabled) }
        viewModelScope.launch { repository.saveReduceMotion(enabled) }
    }

    private fun finish() {
        val state = _uiState.value
        val completed = state.cards.map { if (it.bucket == null) classifyCard(it, TaskBucket.UNDECIDED) else it }
        val session = SessionSummary(System.currentTimeMillis(), completed.size, summarize(completed))
        viewModelScope.launch {
            repository.saveSession(session)
            val count = repository.loadRecentSessions().size
            _uiState.update { it.copy(phase = TaskfallPhase.RESULT, cards = completed, recentSessionCount = count) }
        }
    }

    private fun checkComfort() {
        val startedAt = _uiState.value.sessionStartedAtEpochMs ?: return
        if (System.currentTimeMillis() - startedAt >= 10 * 60 * 1000L) {
            _uiState.update { it.copy(showBreakPrompt = true) }
        }
    }
}
