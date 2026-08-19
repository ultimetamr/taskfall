package com.openai.taskfall.ui.sorting

import com.openai.taskfall.data.repository.InMemoryTaskfallRepository
import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.TaskfallPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TaskfallViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun followsOnboardingAndAutoDecidesAnUnclaimedCard() {
        val viewModel = TaskfallViewModel(InMemoryTaskfallRepository())

        viewModel.onEvent(TaskfallEvent.StartCapture)
        assertEquals(TaskfallPhase.ONBOARDING, viewModel.uiState.value.phase)

        viewModel.onEvent(TaskfallEvent.ContinueOnboarding)
        viewModel.onEvent(TaskfallEvent.InputChanged("第一条\n第二条"))
        viewModel.onEvent(TaskfallEvent.StartSorting)
        viewModel.onEvent(TaskfallEvent.SpawnTick)

        val state = viewModel.uiState.value
        assertEquals(TaskfallPhase.SORTING, state.phase)
        assertEquals(2, state.spawnedCardCount)
        assertEquals(TaskBucket.UNDECIDED, state.cards.first().bucket)
        assertEquals(null, state.cards.last().bucket)
    }
}
