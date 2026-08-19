# 空间便签瀑布（Taskfall）

<p align="center">
  <img src="artifacts/branding/space-note-waterfall-icon-master.png" width="180" alt="空间便签瀑布应用图标" />
</p>

一款面向 PICO Project Swan / PICO OS 6 的 Shared Space 空间效率应用：将纷乱待办化作缓慢落下的便签，通过抓取分拣快速理清今天、以后、委托与待决定事项。

## 核心功能

- 一次输入或粘贴最多 20 条中文待办。
- 便签每 2 秒落下一张，始终保持在舒适视区内。
- 将便签拖入“今天”“以后”“交给别人”三个篮筐；未处理内容自动进入“待决定”。
- 支持从篮筐重新抓回、结束后继续编辑分类。
- 结果页按分类汇总，并支持导出截图。
- 本地保存设置、最近输入和最近 30 条分拣记录。
- 超过 26 个字符的便签自动以两行省略形式显示。

## 空间交互

- 注视 + 捏合作为主要选择方式。
- 支持抓取、拖拽和直接放入分类区域。
- 提供等价的手柄点击/抓取回退操作。
- Shared Space 平面窗口，不主动移动相机。
- 连续使用满 10 分钟时显示休息提示。
- 分类篮筐通过颜色、图标和空间反馈区分。

## 应用流程

```text
Home → Onboarding → Capture → Sorting → Paused → Result/Edit → Home
```

## 技术栈

- Kotlin / Android
- PICO Spatial SDK 6
- SpatialUI Compose + `PicoTheme`
- 单向 UI 状态与 ViewModel
- SharedPreferences 本地持久化
- PICO OS 6 `dragAndDropSource` / `dragAndDropTarget`

应用包名为 `com.openai.taskfall`，启动入口为 `.platform.LaunchActivity`。

## 项目结构

```text
app/src/main/java/com/openai/taskfall/
├── domain/          业务模型、输入解析和分拣规则
├── data/            本地存储与最近记录
├── platform/        Spatial Application、Activity 和平台模块
└── ui/sorting/      页面、ViewModel、便签、篮筐与结果界面

app/src/test/        20 条中文待办、截断和状态流程测试
design/taskfall/     产品、交互、视觉与体验设计文档
```

## 构建

准备 Java、Android SDK 与 PICO Spatial SDK 环境后运行：

```powershell
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

调试 APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 安装与启动

连接 PICO Swan 真机或启动 PICO OS 6 模拟器后：

```powershell
pico-cli device list --format json
pico-cli app install app/build/outputs/apk/debug/app-debug.apk --device <device-id> --replace
pico-cli app launch com.openai.taskfall --activity .platform.LaunchActivity --device <device-id>
```

## 已验证基线

- 单元测试与 Debug APK 构建通过。
- 已在 PICO OS 6.0.0 Swan x86_64 模拟器完成启动验证。
- 已在 Swan B3110（Android 16 / API 36 / ARM64）完成安装和启动验证。
- 启动检查期间无 Crash Buffer 或 AndroidRuntime 致命错误。

## MVP 范围

首版不包含登录、广告、支付、日历同步、任务提醒、多人实时协作、语音识别或用户内容社区。
