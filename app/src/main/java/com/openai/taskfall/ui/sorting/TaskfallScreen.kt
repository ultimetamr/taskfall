package com.openai.taskfall.ui.sorting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.openai.taskfall.data.repository.SharedPreferencesTaskfallRepository
import com.openai.taskfall.platform.AndroidScreenshotExporter
import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.TaskfallPhase
import com.openai.taskfall.domain.usecase.summarize
import com.openai.taskfall.ui.sorting.components.BucketRow
import com.openai.taskfall.ui.sorting.components.TaskCardSurface
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.design.TextArea

@Composable
fun TaskfallScreen(
) {
    val context = LocalContext.current
    val view = LocalView.current
    val factory = remember(context) {
        TaskfallViewModelFactory(SharedPreferencesTaskfallRepository(context.applicationContext))
    }
    val viewModel: TaskfallViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val screenshotExporter = remember(context, view) {
        AndroidScreenshotExporter(context, view)
    }
    LaunchedEffect(state.phase) {
        while (state.phase == TaskfallPhase.SORTING) {
            delay(2_000L)
            viewModel.onEvent(TaskfallEvent.SpawnTick)
        }
    }
    LaunchedEffect(state.phase) {
        while (state.phase == TaskfallPhase.SORTING) {
            delay(60_000L)
            viewModel.onEvent(TaskfallEvent.ComfortTick)
        }
    }
    TaskfallContent(state = state, onEvent = viewModel::onEvent, onExportScreenshot = {
        state.toSessionSummary()?.let { screenshotExporter.export(it) }
    })
}

@Composable
private fun TaskfallContent(
    state: TaskfallUiState,
    onEvent: (TaskfallEvent) -> Unit,
    onExportScreenshot: () -> Unit,
) {
    when (state.phase) {
        TaskfallPhase.HOME -> HomeContent(state, onEvent)
        TaskfallPhase.ONBOARDING -> OnboardingContent(onEvent)
        TaskfallPhase.CAPTURE -> CaptureContent(state, onEvent)
        TaskfallPhase.SORTING -> SortingContent(state, onEvent)
        TaskfallPhase.PAUSED -> PauseContent(onEvent)
        TaskfallPhase.RESULT -> ResultContent(state, onEvent, onExportScreenshot)
    }
}

@Composable
private fun OnboardingContent(onEvent: (TaskfallEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("三步完成整理", style = PicoTheme.typography.displaySmall)
        Text("1. 粘贴最多 20 条待办", style = PicoTheme.typography.titleMedium)
        Text("2. 捏住便签拖入篮筐；手柄可先选便签再选篮筐", style = PicoTheme.typography.titleMedium)
        Text("3. 未抓取的便签会自动进入“待决定”，结果页仍可修改", style = PicoTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onEvent(TaskfallEvent.ExitToHome) }) { Text("返回") }
            Button(onClick = { onEvent(TaskfallEvent.ContinueOnboarding) }) { Text("继续") }
        }
    }
}

@Composable
private fun HomeContent(state: TaskfallUiState, onEvent: (TaskfallEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("空间便签瀑布", style = PicoTheme.typography.displayMedium)
        Text("把列表焦虑，变成一场轻松分拣。", style = PicoTheme.typography.titleMedium)
        Text("最多 20 条 · 每张便签 2 秒落下 · 可随时改回", style = PicoTheme.typography.bodyLarge)
        Button(onClick = { onEvent(TaskfallEvent.StartCapture) }) { Text("开始整理") }
        Text("最近完成 ${state.recentSessionCount} 次", style = PicoTheme.typography.labelMedium)
    }
}

@Composable
private fun CaptureContent(state: TaskfallUiState, onEvent: (TaskfallEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("把待办贴进来", style = PicoTheme.typography.displaySmall)
        Text("每行一条，最多 20 条。超过 26 字会自动截成两行。", style = PicoTheme.typography.bodyLarge)
        Text("例如：整理项目提案\n回复客户邮件\n下周预约体检", style = PicoTheme.typography.bodyMedium)
        Button(onClick = { onEvent(TaskfallEvent.ToggleReduceMotion) }) {
            Text(if (state.reduceMotion) "减少动态：开" else "减少动态：关")
        }
        TextArea(
            value = state.inputText,
            onValueChange = { onEvent(TaskfallEvent.InputChanged(it)) },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onEvent(TaskfallEvent.ExitToHome) }) { Text("返回") }
            Button(onClick = { onEvent(TaskfallEvent.StartSorting) }) { Text("开始落下") }
        }
    }
}

@Composable
private fun SortingContent(state: TaskfallUiState, onEvent: (TaskfallEvent) -> Unit) {
    val counts = summarize(state.cards)
    val active = state.activeCardId
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("分拣中", style = PicoTheme.typography.displaySmall)
                Text("已落下 ${state.spawnedCardCount}/${state.cards.size} · 看准便签，捏住拖入篮筐；手柄按键也可选择。", style = PicoTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEvent(TaskfallEvent.Pause) }) { Text("暂停") }
                Button(onClick = { onEvent(TaskfallEvent.Reset) }) { Text("重置") }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.cards.take(state.spawnedCardCount), key = { it.id }) { card ->
                    TaskCardSurface(
                        text = card.displayText,
                        index = card.id,
                        selected = active == card.id,
                        onGrab = { onEvent(TaskfallEvent.Grab(card.id)) },
                        onCycle = { onEvent(TaskfallEvent.CycleBucket(card.id)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        BucketRow(counts = counts, onDrop = { bucket, draggedId ->
            (draggedId ?: active)?.let { onEvent(TaskfallEvent.Drop(it, bucket)) }
        })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onEvent(TaskfallEvent.AutoDecide) }) { Text("未抓取的放入待决定") }
            Button(onClick = { onEvent(TaskfallEvent.Finish) }) { Text("查看结果") }
        }
        if (state.showBreakPrompt) {
            Text("已经整理 10 分钟了，休息一下再继续。", style = PicoTheme.typography.titleMedium)
            Button(onClick = { onEvent(TaskfallEvent.DismissBreakPrompt) }) { Text("知道了") }
        }
    }
}

@Composable
private fun PauseContent(onEvent: (TaskfallEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("已暂停", style = PicoTheme.typography.displaySmall)
        Button(onClick = { onEvent(TaskfallEvent.Resume) }) { Text("继续") }
        Button(onClick = { onEvent(TaskfallEvent.Reset) }) { Text("重置本局") }
        Button(onClick = { onEvent(TaskfallEvent.ExitToHome) }) { Text("退出到首页") }
    }
}

@Composable
private fun ResultContent(state: TaskfallUiState, onEvent: (TaskfallEvent) -> Unit, onExportScreenshot: () -> Unit) {
    val counts = summarize(state.cards)
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("整理完成", style = PicoTheme.typography.displaySmall)
        Text("你把 ${state.cards.size} 条待办分成了四组。", style = PicoTheme.typography.titleMedium)
        Text("可拖动便签重新分类；手柄可先选便签再选篮筐。", style = PicoTheme.typography.bodyMedium)
        BucketRow(counts = counts, onDrop = { bucket, draggedId ->
            (draggedId ?: state.activeCardId)?.let { onEvent(TaskfallEvent.Drop(it, bucket)) }
        })
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.cards, key = { it.id }) { card ->
                TaskCardSurface(
                    text = "${card.bucket?.symbol} ${card.displayText}",
                    index = card.id,
                    selected = state.activeCardId == card.id,
                    onGrab = { onEvent(TaskfallEvent.Grab(card.id)) },
                    onCycle = { onEvent(TaskfallEvent.CycleBucket(card.id)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onEvent(TaskfallEvent.ExitToHome) }) { Text("回到首页") }
            Button(onClick = onExportScreenshot) { Text("导出截图") }
        }
    }
}

private fun TaskfallUiState.toSessionSummary() =
    com.openai.taskfall.domain.model.SessionSummary(
        createdAtEpochMs = sessionStartedAtEpochMs ?: System.currentTimeMillis(),
        total = cards.size,
        counts = summarize(cards),
    )

class TaskfallViewModelFactory(
    private val repository: com.openai.taskfall.data.repository.TaskfallRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        TaskfallViewModel(repository) as T
}
