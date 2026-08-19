package com.openai.taskfall.domain

import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.usecase.buildSpawnSchedule
import com.openai.taskfall.domain.usecase.FallingCardSpawner
import com.openai.taskfall.domain.usecase.parseTaskInput
import com.openai.taskfall.domain.usecase.truncateTaskText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import com.openai.taskfall.data.repository.InMemoryTaskfallRepository
import com.openai.taskfall.domain.model.SessionSummary

class TaskfallUseCasesTest {
    @Test
    fun parsesTwentyChineseTasksAndCapsInput() {
        val input = (1..21).joinToString("\n") { "任务 $it：整理项目资料" }
        val cards = parseTaskInput(input)
        assertEquals(20, cards.size)
        assertEquals(0, cards.first().id)
        assertEquals(19, cards.last().id)
    }

    @Test
    fun truncatesLongChineseTextToTwoLineBudget() {
        val source = "这是一个超过二十六个汉字的待办事项，用来验证自动截断行为"
        val result = truncateTaskText(source)
        assertTrue(result.endsWith("…"))
        assertEquals(26, result.length)
    }

    @Test
    fun emptyLinesAreIgnoredAndWhitespaceIsNormalized() {
        val cards = parseTaskInput("\n  回复邮件  \n\n准备会议")
        assertEquals(listOf("回复邮件", "准备会议"), cards.map { it.displayText })
    }

    @Test
    fun spawnScheduleKeepsTwoSecondCadence() {
        val cards = parseTaskInput("今天\n以后\n交接")
        val schedule = buildSpawnSchedule(cards, nowEpochMs = 1000L)
        assertEquals(listOf(1000L, 3000L, 5000L), schedule.map { it.spawnedAtEpochMs })
    }

    @Test
    fun destinationsIncludeSafeUndecidedBucket() {
        assertEquals("待决定", TaskBucket.UNDECIDED.label)
    }

    @Test
    fun keepsOnlyThirtyRecentSessions() = runTest {
        val repository = InMemoryTaskfallRepository()
        repeat(31) { index ->
            repository.saveSession(SessionSummary(index.toLong(), 1, mapOf(TaskBucket.TODAY to 1)))
        }
        val sessions = repository.loadRecentSessions()
        assertEquals(30, sessions.size)
        assertEquals(30L, sessions.first().createdAtEpochMs)
        assertEquals(1L, sessions.last().createdAtEpochMs)
    }

    @Test
    fun spawnerRevealsOnlyCardsWhoseTwoSecondSlotHasArrived() {
        val cards = parseTaskInput("甲\n乙\n丙")
        val spawner = FallingCardSpawner(cards) { 1000L }
        assertEquals(1, spawner.visibleCards(1000L).size)
        assertEquals(2, spawner.visibleCards(3000L).size)
        assertTrue(spawner.isComplete(5000L))
    }
}
