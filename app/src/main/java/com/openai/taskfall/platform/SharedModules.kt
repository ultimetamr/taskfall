package com.openai.taskfall.platform

import com.openai.taskfall.domain.model.SessionSummary

interface HandInput {
    fun isAvailable(): Boolean
}

interface ControllerInput {
    fun isAvailable(): Boolean
}

interface GrabInteractable {
    fun onGrab(id: Int)
    fun onRelease(id: Int, x: Float, y: Float)
}

interface SpatialAnchorStore

interface AudioCue {
    fun play(bucketSymbol: String)
}

interface Haptics {
    fun confirm()
    fun reject()
}

interface TutorialStep {
    val id: String
    val copy: String
}

interface PauseMenu

interface ScreenshotExporter {
    fun export(summary: SessionSummary): Result<Unit>
}
