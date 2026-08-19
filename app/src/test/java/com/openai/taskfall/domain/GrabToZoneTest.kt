package com.openai.taskfall.domain

import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.TaskCard
import com.openai.taskfall.domain.usecase.GrabToZone
import com.openai.taskfall.domain.usecase.ZoneRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GrabToZoneTest {
    private val card = TaskCard(1, "整理资料", "整理资料")
    private val hitTest = GrabToZone(
        mapOf(
            TaskBucket.TODAY to ZoneRect(0f, 0f, 100f, 100f),
            TaskBucket.LATER to ZoneRect(110f, 0f, 210f, 100f),
        ),
    )

    @Test
    fun resolvesDropInsideDestination() {
        assertEquals(TaskBucket.TODAY, hitTest.resolve(card, 40f, 50f)?.bucket)
    }

    @Test
    fun rejectsDropOutsideAllDestinations() {
        assertNull(hitTest.resolve(card, 400f, 400f))
    }
}
