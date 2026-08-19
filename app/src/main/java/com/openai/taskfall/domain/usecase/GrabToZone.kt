package com.openai.taskfall.domain.usecase

import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.TaskCard

/**
 * The UI supplies the card center and destination bounds after a hand or
 * controller drag. Keeping the hit test pure makes hand/controller parity
 * deterministic and easy to boundary-test.
 */
data class ZoneRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float): Boolean = x in left..right && y in top..bottom
}

class GrabToZone(private val zones: Map<TaskBucket, ZoneRect>) {
    fun resolve(card: TaskCard, x: Float, y: Float): TaskCard? = zones.entries
        .firstOrNull { (_, rect) -> rect.contains(x, y) }
        ?.key
        ?.let { classifyCard(card, it) }
}
