package com.openai.taskfall.ui.sorting.components

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.LocalContentColor
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.haptic.controllerHapticFeedback
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TaskCardSurface(
    text: String,
    index: Int,
    selected: Boolean,
    onGrab: () -> Unit,
    onCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(24.dp))
            .spatialHoverEffect(enabled = true)
            .dragAndDropSource(
                transferData = {
                    DragAndDropTransferData(
                        clipData = ClipData.newPlainText(TASKFALL_CARD_LABEL, index.toString()),
                    )
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onGrab,
            )
            .controllerHapticFeedback(interactionSource = interactionSource)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("便签 ${index + 1}", style = PicoTheme.typography.labelMedium)
            Text(text, style = PicoTheme.typography.titleMedium)
            if (selected) {
                Button(onClick = onCycle) { Text("换一个篮筐") }
            }
        }
    }
}

@Composable
fun BasketSurface(
    label: String,
    symbol: String,
    count: Int,
    onDropCard: (Int) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isDropHovered by remember { mutableStateOf(false) }
    val dropTarget = remember(onDropCard) {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                isDropHovered = true
            }

            override fun onExited(event: DragAndDropEvent) {
                isDropHovered = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDropHovered = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val id = event.toAndroidDragEvent().clipData
                    ?.takeIf { it.description.label?.toString() == TASKFALL_CARD_LABEL }
                    ?.getItemAt(0)
                    ?.text
                    ?.toString()
                    ?.toIntOrNull()
                    ?: return false
                onDropCard(id)
                return true
            }
        }
    }
    Box(
        modifier = modifier
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(22.dp))
            .spatialHoverEffect(enabled = true)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    val description = event.toAndroidDragEvent().clipDescription
                    description?.label?.toString() == TASKFALL_CARD_LABEL &&
                        description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = dropTarget,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .controllerHapticFeedback(interactionSource = interactionSource)
            .padding(16.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides PicoTheme.colorScheme.labelPrimary) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$symbol  $label", style = PicoTheme.typography.labelLarge)
                Text(count.toString(), style = PicoTheme.typography.displaySmall)
                if (isDropHovered) Text("松开放入", style = PicoTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun BucketRow(
    counts: Map<com.openai.taskfall.domain.model.TaskBucket, Int>,
    onDrop: (com.openai.taskfall.domain.model.TaskBucket, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        com.openai.taskfall.domain.model.TaskBucket.values().forEach { bucket ->
            BasketSurface(
                label = bucket.label,
                symbol = bucket.symbol,
                count = counts[bucket] ?: 0,
                onDropCard = { cardId -> onDrop(bucket, cardId) },
                onClick = { onDrop(bucket, null) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private const val TASKFALL_CARD_LABEL = "taskfall-card-id"
