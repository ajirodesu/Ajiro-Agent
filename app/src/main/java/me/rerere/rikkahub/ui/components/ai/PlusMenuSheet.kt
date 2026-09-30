/*
 * PlusMenuSheet.kt
 * Author: AjiroDesu
 *
 * The HTML "+ menu" bottom sheet (main menu + Attachment, Skills, Web Search, Effort,
 * Agent Mode and Approval Mode panes) ported to Jetpack Compose and dressed entirely in
 * LastChat's theme. Nothing here hardcodes a colour, font or new accent:
 *
 *   sheet fill ........ colorScheme.surfaceContainerLow   (same as MinimalChatInput's + sheet)
 *   sheet corners ..... 40dp top                          (same as MinimalChatInput.pickerSheetShape)
 *   nested surfaces ... colorScheme.surfaceContainerHighest (design-qa.md rule for nested pickers)
 *   selected state .... primaryContainer / onPrimaryContainer (ReasoningPicker rows)
 *   grouped rows ...... AppShapes.ListItem / First / Middle / Last, 4dp gaps
 *   chips / badges .... AppShapes.Chip / AppShapes.Tag, extendColors.orange* for the amber badge
 *   chrome buttons .... AppSize.ChromePill (48dp)
 *   scrim ............. colorScheme.scrim
 *   type .............. MaterialTheme.typography (Google Sans Flex table)
 *   switch / haptics .. HapticSwitch, rememberPremiumHaptics
 *   toasts ............ LocalToaster
 *
 * Behaviour is ported from the HTML sheet engine: closed / half / full snap points, spring
 * settle, rubber-band overscroll, velocity-based snapping, scrim fading with height, pane
 * navigation with a rise-and-fade, leaf actions that flash, toast, close, then reset to the
 * main pane, radio-style selection groups, and the Thinking toggle.
 *
 * Usage (host it last inside the screen's root Box so it draws above the composer):
 *
 *   val plusMenu = rememberPlusMenuState()
 *   Box(Modifier.fillMaxSize()) {
 *       ...chat content + composer, with the + button calling plusMenu.toggle()
 *       and highlighting itself while plusMenu.isOpen ...
 *       PlusMenu(state = plusMenu, actions = PlusMenuActions(onTakePhoto = { ... }))
 *   }
 */
package me.rerere.rikkahub.ui.components.ai

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.ui.components.ui.HapticSwitch
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.hooks.HapticPattern
import me.rerere.rikkahub.ui.hooks.rememberPremiumHaptics
import me.rerere.rikkahub.ui.theme.AppShapes
import me.rerere.rikkahub.ui.theme.AppSize
import me.rerere.rikkahub.ui.theme.extendColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/* ============================================================
 * MODEL
 * ============================================================ */
enum class PlusMenuPane { Main, Attachment, Skills, WebSearch, Effort, Agent, Approval }
enum class AgentMode { Agent, Chat }
enum class ApprovalMode { Auto, AllowList, Manual }
enum class WebSearchMode { Offline, SmartOnline }
enum class EffortLevel { Low, Medium, High, Extra, Max }
enum class SkillMode { Auto, Manual }
enum class SkillTrailing { Pinned, OpenExternal }

data class PlusMenuSkill(
    val id: String,
    val name: String,
    val description: String,
    val trailing: SkillTrailing,
    val toast: String,
)

/** The two pinned skills from the HTML. Replace with your real skill list. */
val DefaultPinnedSkills = listOf(
    PlusMenuSkill("pdf-export", "PDF export", "Generate formatted PDF reports", SkillTrailing.Pinned, "PDF export pinned"),
    PlusMenuSkill("code-review", "Code review", "Checks diffs against your style guide", SkillTrailing.OpenExternal, "Code review skill enabled"),
)

/** Hooks into your app. Everything defaults to a no-op so the menu works standalone. */
class PlusMenuActions(
    val onUploadFile: () -> Unit = {},
    val onTakePhoto: () -> Unit = {},
    val onSelectFromGallery: () -> Unit = {},
    val onOpenProject: () -> Unit = {},
    val onModelClick: () -> Unit = {},
    val onSkillClick: (PlusMenuSkill) -> Unit = {},
    val onAddSkills: () -> Unit = {},
    val onSkillModeChange: (SkillMode) -> Unit = {},
    val onWebSearchModeChange: (WebSearchMode) -> Unit = {},
    val onOpenSearchSettings: () -> Unit = {},
    val onEffortChange: (EffortLevel) -> Unit = {},
    val onAgentModeChange: (AgentMode) -> Unit = {},
    val onApprovalModeChange: (ApprovalMode) -> Unit = {},
    val onThinkingChange: (Boolean) -> Unit = {},
    val onOpenLorebooks: () -> Unit = {},
    val onOpenPlugins: () -> Unit = {},
    val onSummarize: () -> Unit = {},
)

/* ============================================================
 * STATE + SHEET ENGINE
 * Snap points: closed / half (50% of the container) / full (container minus status bar).
 * Spring, thresholds and rubber-band factors are the HTML engine's values,
 * converted from canvas px to dp at 1dp = 3.5px.
 * ============================================================ */
private const val CANVAS_PX_PER_DP = 3.5f
private val MenuEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

@Stable
class PlusMenuState internal constructor(private val scope: CoroutineScope) {
    /** Current sheet height in px. Read it in layout/draw phases, not composition. */
    var currentH by mutableFloatStateOf(0f)
        private set

    /** True while the sheet is visibly open. Drive the + button's active look from this. */
    val isOpen: Boolean by derivedStateOf { currentH > 2f }

    /**
     * Composition gate for dialog-hosted sheets. Set synchronously in [open]
     * so the dialog (and its metric pass) exists before the open animation
     * needs [halfPx]; cleared when a close animation settles at 0.
     */
    var isVisible by mutableStateOf(false)
        private set

    var pane by mutableStateOf(PlusMenuPane.Main)
    var agentMode by mutableStateOf(AgentMode.Agent)
    var approvalMode by mutableStateOf(ApprovalMode.Manual)
    var webSearchMode by mutableStateOf(WebSearchMode.SmartOnline)
    var effort by mutableStateOf(EffortLevel.Medium)
    var skillMode by mutableStateOf(SkillMode.Auto)
    var thinking by mutableStateOf(true)

    internal var halfPx = 0f
    internal var fullPx = 0f
    private var pxPerDp = 1f

    private var animJob: Job? = null
    private var dragRaw: Float? = null
    internal var isDragging = false
        private set

    internal fun updateMetrics(containerPx: Float, statusTopPx: Float, density: Float) {
        pxPerDp = density
        halfPx = containerPx * 0.5f
        fullPx = (containerPx - statusTopPx).coerceAtLeast(0f)
    }

    private fun dpc(canvasPx: Float) = canvasPx / CANVAS_PX_PER_DP * pxPerDp

    private fun setH(h: Float) {
        currentH = h.coerceIn(0f, fullPx + dpc(40f))
    }

    private fun animateTo(target: Float, velocity: Float) {
        animJob?.cancel()
        animJob = scope.launch {
            // HTML spring: stiffness 280, damping 32, unit mass  ->  damping ratio ~0.956
            animate(
                initialValue = currentH,
                targetValue = target,
                initialVelocity = velocity,
                animationSpec = spring(dampingRatio = 0.956f, stiffness = 280f),
            ) { value, _ -> setH(value) }
            setH(target)
            if (target == 0f) isVisible = false
        }
    }

    fun open() {
        isVisible = true
        // The open target needs metrics from the freshly composed sheet, so
        // wait (at most ~1s) for the SideEffect pass before animating.
        scope.launch {
            var guard = 0
            while (halfPx <= 0f && guard++ < 60) delay(16)
            if (!isVisible) return@launch
            animJob?.cancel()
            setH(0f)
            animateTo(halfPx, dpc(1400f))
        }
    }

    fun close() = animateTo(0f, -dpc(1200f))

    fun toggle() {
        if (isVisible) close() else open()
    }

    fun showPane(target: PlusMenuPane) {
        pane = target
    }

    /** Positive [delta] grows the sheet. Rubber-bands past full and below closed (factor 0.2). */
    internal fun dragBy(delta: Float, allowOverscroll: Boolean = true) {
        animJob?.cancel()
        isDragging = true
        val raw = (dragRaw ?: currentH) + delta
        dragRaw = raw
        val effective = when {
            raw > fullPx -> if (allowOverscroll) fullPx + (raw - fullPx) * 0.2f else fullPx
            raw < 0f -> raw * 0.2f
            else -> raw
        }
        setH(effective)
    }

    internal fun endDrag() {
        dragRaw = null
        isDragging = false
    }

    /** [velocityUp] in px/s, positive = sheet growing. Same decision table as the HTML. */
    internal fun settle(velocityUp: Float) {
        val h = currentH
        val fling = dpc(1200f)
        val projected = h + velocityUp * 0.15f
        val target = when {
            velocityUp < -fling -> if (h > halfPx + dpc(200f)) halfPx else 0f
            velocityUp > fling -> fullPx
            else -> {
                val dClosed = abs(projected)
                val dHalf = abs(projected - halfPx)
                val dFull = abs(projected - fullPx)
                when {
                    dClosed < dHalf && dClosed < dFull && h < halfPx * 0.6f -> 0f
                    dFull < dHalf -> fullPx
                    else -> halfPx
                }
            }
        }
        animateTo(target, velocityUp)
    }

    internal fun sheetNestedScroll(): NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            // Finger up while the sheet is below full: grow the sheet before the content scrolls.
            if (dy < 0f && currentH < fullPx - 2f && source == NestedScrollSource.UserInput) {
                dragBy(-dy, allowOverscroll = false)
                return Offset(0f, dy)
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            // Content is at its top and the finger keeps pulling down: shrink the sheet.
            if (dy > 0f && source == NestedScrollSource.UserInput) {
                dragBy(-dy)
                return Offset(0f, dy)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (!isDragging) return Velocity.Zero
            endDrag()
            settle(-available.y)
            return available
        }
    }
}

@Composable
fun rememberPlusMenuState(): PlusMenuState {
    val scope = rememberCoroutineScope()
    return remember { PlusMenuState(scope) }
}

/* ============================================================
 * THEME BINDINGS (all from LastChat)
 * ============================================================ */
/** Mirrors MinimalChatInput.pickerSheetShape (top corners 40dp). */
private val PlusMenuSheetShape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)

private fun groupedShape(index: Int, count: Int): Shape = when {
    count == 1 -> AppShapes.ListItem
    index == 0 -> AppShapes.ListItemFirst
    index == count - 1 -> AppShapes.ListItemLast
    else -> AppShapes.ListItemMiddle
}

private const val TOAST_MS = 2200L

/* ============================================================
 * + MENU
 * ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusMenu(
    state: PlusMenuState,
    modifier: Modifier = Modifier,
    actions: PlusMenuActions = PlusMenuActions(),
    modelName: String? = null,
    pinnedSkills: List<PlusMenuSkill> = DefaultPinnedSkills,
    /** Subtitle for the Lorebooks row; null hides the row. */
    lorebooksSubtitle: String? = null,
    /** Subtitle for the Plugins row; null hides the row. */
    pluginsSubtitle: String? = null,
    /** Initial agent/chat mode; synced into the menu state on change. */
    agentMode: AgentMode = AgentMode.Agent,
    /** Provider name shown on the extra row; null hides the row. */
    searchProviderName: String? = null,
    /** Subtitle for the Summarize row; null hides the row. */
    summarizeSubtitle: String? = null,
    /** Defaults to LastChat's toaster (2.2s, like the HTML toast). */
    onToast: ((String) -> Unit)? = null,
) {
    val toaster = LocalToaster.current
    val toast: (String) -> Unit = onToast ?: { toaster.show(it, duration = TOAST_MS) }
    val haptics = rememberPremiumHaptics()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val scrimColor = MaterialTheme.colorScheme.scrim

    LaunchedEffect(state.pane) { scrollState.scrollTo(0) }
    LaunchedEffect(agentMode) { state.agentMode = agentMode }

    // Back steps out of a sub-pane first, then closes the sheet.
    BackHandler(enabled = state.isOpen) {
        if (state.pane != PlusMenuPane.Main) state.showPane(PlusMenuPane.Main) else state.close()
    }

    /** Leaf action: toast, run it, flash (handled by the row), close, then reset to the main pane. */
    val leaf: (String?, () -> Unit) -> Unit = { message, action ->
        message?.let(toast)
        action()
        scope.launch {
            delay(140)
            state.close()
            delay(250)
            state.showPane(PlusMenuPane.Main)
        }
    }

    /** Radio-style selection: haptic, apply, toast. */
    fun select(message: String, apply: () -> Unit) {
        haptics.perform(HapticPattern.Pop)
        apply()
        toast(message)
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val statusTop = WindowInsets.statusBars.getTop(density)
        SideEffect {
            state.updateMetrics(
                containerPx = constraints.maxHeight.toFloat(),
                statusTopPx = statusTop.toFloat(),
                density = density.density,
            )
        }

        // Scrim: fades with height, tap closes (max alpha 0.88 at half height, as in the HTML).
        if (state.isOpen) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { state.close() } }
                    .drawBehind {
                        if (state.halfPx > 0f) {
                            val alpha = min(0.88f, state.currentH / state.halfPx * 0.88f)
                            drawRect(scrimColor.copy(alpha = alpha))
                        }
                    },
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = PlusMenuSheetShape,
            tonalElevation = 0.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .layout { measurable, c ->
                    val h = state.currentH.roundToInt().coerceAtLeast(0)
                    val p = measurable.measure(c.copy(minHeight = h, maxHeight = h))
                    layout(p.width, h) { p.place(0, 0) }
                },
        ) {
            Column(Modifier.fillMaxSize()) {
                // Drag zone: the handle always drags the sheet.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta -> state.dragBy(-delta) },
                            onDragStopped = { velocity ->
                                state.endDrag()
                                state.settle(-velocity)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BottomSheetDefaults.DragHandle()
                }

                val slidePx = with(density) { 16.dp.roundToPx() }
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .nestedScroll(remember(state) { state.sheetNestedScroll() })
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                ) {
                    AnimatedContent(
                        targetState = state.pane,
                        transitionSpec = {
                            (fadeIn(tween(220, easing = MenuEasing)) +
                                slideInVertically(tween(220, easing = MenuEasing)) { slidePx }) togetherWith
                                ExitTransition.None using SizeTransform(clip = false) { _, _ -> snap() }
                        },
                        label = "plusMenuPane",
                    ) { pane ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            when (pane) {
                                PlusMenuPane.Main -> MainPane(
                                    state, modelName, actions, leaf,
                                    lorebooksSubtitle, pluginsSubtitle, summarizeSubtitle,
                                )
                                PlusMenuPane.Attachment -> AttachmentPane(state, actions, leaf)
                                PlusMenuPane.Skills -> SkillsPane(state, actions, pinnedSkills, leaf, ::select)
                                PlusMenuPane.WebSearch -> WebSearchPane(state, actions, searchProviderName, ::select)
                                PlusMenuPane.Effort -> EffortPane(state, actions, ::select, toast, haptics)
                                PlusMenuPane.Agent -> AgentPane(state, actions, ::select)
                                PlusMenuPane.Approval -> ApprovalPane(state, actions, ::select)
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

/* ============================================================
 * PANES
 * ============================================================ */
private typealias LeafHandler = (String?, () -> Unit) -> Unit
private typealias SelectHandler = (String, () -> Unit) -> Unit

@Composable
private fun MainPane(
    state: PlusMenuState,
    modelName: String?,
    actions: PlusMenuActions,
    leaf: LeafHandler,
    lorebooksSubtitle: String?,
    pluginsSubtitle: String?,
    summarizeSubtitle: String?,
) {
    MenuRow(Icons.Rounded.AttachFile, "Attachment", null) { state.showPane(PlusMenuPane.Attachment) }
    MenuRow(Icons.Rounded.FolderOpen, "Open Project", null, leaf = true) {
        leaf("Opened Project Selector", actions.onOpenProject)
    }
    MenuRow(Icons.Rounded.ViewModule, "Model", modelName, leaf = true) {
        leaf("Selected Model: ${modelName ?: "Claude 3.5 Sonnet"}", actions.onModelClick)
    }
    MenuRow(
        Icons.Rounded.Category, "Skills",
        if (state.skillMode == SkillMode.Auto) "Auto" else "Manual selection",
        active = state.skillMode == SkillMode.Manual,
    ) { state.showPane(PlusMenuPane.Skills) }
    MenuRow(
        Icons.Rounded.Search, "Web Search",
        if (state.webSearchMode == WebSearchMode.Offline) "Offline Mode" else "Smart Online Mode",
        active = state.webSearchMode == WebSearchMode.SmartOnline,
    ) { state.showPane(PlusMenuPane.WebSearch) }
    MenuRow(Icons.Rounded.Speed, "Effort", effortLabel(state.effort)) { state.showPane(PlusMenuPane.Effort) }
    MenuRow(
        Icons.Rounded.SmartToy, "Agent Mode",
        if (state.agentMode == AgentMode.Agent) "Agent" else "Chat",
        active = state.agentMode == AgentMode.Agent,
    ) { state.showPane(PlusMenuPane.Agent) }
    MenuRow(Icons.Rounded.Checklist, "Approval Mode", approvalLabel(state.approvalMode)) {
        state.showPane(PlusMenuPane.Approval)
    }
    lorebooksSubtitle?.let { subtitle ->
        MenuRow(Icons.Rounded.Book, "Lorebooks", subtitle, leaf = true) {
            leaf(null, actions.onOpenLorebooks)
        }
    }
    pluginsSubtitle?.let { subtitle ->
        MenuRow(Icons.Rounded.Extension, "Plugins", subtitle, leaf = true) {
            leaf(null, actions.onOpenPlugins)
        }
    }
    summarizeSubtitle?.let { subtitle ->
        MenuRow(Icons.Rounded.Summarize, "Summarize", subtitle, leaf = true) {
            leaf(null, actions.onSummarize)
        }
    }
}

@Composable
private fun AttachmentPane(state: PlusMenuState, actions: PlusMenuActions, leaf: LeafHandler) {
    SubHeader("Attachment") { state.showPane(PlusMenuPane.Main) }
    val rows = listOf<Triple<ImageVector, Pair<String, String?>, () -> Unit>>(
        Triple(Icons.Rounded.Description, "Upload File" to "Documents, PDFs, or spreadsheets") {
            leaf("File Attached", actions.onUploadFile)
        },
        Triple(Icons.Rounded.CameraAlt, "Take Photo" to "Capture using camera") {
            leaf("Camera Ready", actions.onTakePhoto)
        },
        Triple(Icons.Rounded.Image, "Select from Gallery" to "Browse local images & videos") {
            leaf("Gallery Opened", actions.onSelectFromGallery)
        },
    )
    val count = rows.size + 1
    rows.forEachIndexed { i, (icon, text, click) ->
        OptionRow(
            title = text.first, description = text.second, icon = icon,
            selected = false, showCheck = false, shape = groupedShape(i, count), leaf = true,
            onClick = click,
        )
    }
    // Cancel is a plain "back", not a leaf action.
    OptionRow(
        title = "Cancel", description = null, icon = Icons.Rounded.Close,
        selected = false, showCheck = false, shape = groupedShape(count - 1, count),
        onClick = { state.showPane(PlusMenuPane.Main) },
    )
}

@Composable
private fun AgentPane(state: PlusMenuState, actions: PlusMenuActions, select: SelectHandler) {
    SubHeader("Agent Mode") { state.showPane(PlusMenuPane.Main) }
    OptionRow(
        title = "Agent",
        description = "Agent can use tools and environment to complete tasks automatically",
        icon = Icons.Rounded.SmartToy,
        selected = state.agentMode == AgentMode.Agent, showCheck = true, shape = groupedShape(0, 2),
        extra = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                TagChip(Icons.Rounded.Build, "Tools")
                TagChip(Icons.Rounded.Search, "Web search")
                TagChip(Icons.Rounded.FolderOpen, "File access")
                TagChip(Icons.Rounded.Terminal, "Runtime env")
            }
        },
        onClick = {
            select("Switched to Agent Mode") { state.agentMode = AgentMode.Agent; actions.onAgentModeChange(AgentMode.Agent) }
        },
    )
    OptionRow(
        title = "Chat", description = "No runtime environment or autonomy; uses fewer tokens",
        icon = Icons.Rounded.ChatBubble,
        selected = state.agentMode == AgentMode.Chat, showCheck = true, shape = groupedShape(1, 2),
        onClick = {
            select("Switched to Fast Chat Mode") { state.agentMode = AgentMode.Chat; actions.onAgentModeChange(AgentMode.Chat) }
        },
    )
}

@Composable
private fun ApprovalPane(state: PlusMenuState, actions: PlusMenuActions, select: SelectHandler) {
    SubHeader("Approval Mode") { state.showPane(PlusMenuPane.Main) }
    data class Item(val mode: ApprovalMode, val icon: ImageVector, val title: String, val desc: String, val toast: String)
    val items = listOf(
        Item(ApprovalMode.Auto, Icons.Rounded.Bolt, "Auto Approve", "Automatically run tools except non-bypassable safety checks", "Approval: Auto Approve"),
        Item(ApprovalMode.AllowList, Icons.Rounded.Checklist, "Allow List", "Only automatically run tools you have explicitly remembered", "Approval: Allow List"),
        Item(ApprovalMode.Manual, Icons.Rounded.PanTool, "Manual", "Ask whenever a tool policy requires your approval", "Approval: Manual Prompting"),
    )
    items.forEachIndexed { i, item ->
        OptionRow(
            title = item.title, description = item.desc, icon = item.icon,
            selected = state.approvalMode == item.mode, showCheck = true, shape = groupedShape(i, items.size),
            onClick = { select(item.toast) { state.approvalMode = item.mode; actions.onApprovalModeChange(item.mode) } },
        )
    }
}

@Composable
private fun WebSearchPane(
    state: PlusMenuState,
    actions: PlusMenuActions,
    searchProviderName: String?,
    select: SelectHandler,
) {
    SubHeader("Web Search") { state.showPane(PlusMenuPane.Main) }
    OptionRow(
        title = "Offline Mode",
        description = "Use only the model's basic knowledge without searching the web",
        icon = Icons.Rounded.WifiOff,
        selected = state.webSearchMode == WebSearchMode.Offline, showCheck = true, shape = groupedShape(0, 2),
        onClick = {
            select("Web Search: Offline Mode") { state.webSearchMode = WebSearchMode.Offline; actions.onWebSearchModeChange(WebSearchMode.Offline) }
        },
    )
    OptionRow(
        title = "Smart Online Mode",
        description = "Intelligently determine whether a search is needed to answer",
        icon = Icons.Rounded.AutoAwesome,
        selected = state.webSearchMode == WebSearchMode.SmartOnline, showCheck = true, shape = groupedShape(1, 2),
        onClick = {
            select("Web Search: Smart Online Active") { state.webSearchMode = WebSearchMode.SmartOnline; actions.onWebSearchModeChange(WebSearchMode.SmartOnline) }
        },
    )
    searchProviderName?.let { name ->
        OptionRow(
            title = "Provider",
            description = name,
            icon = Icons.Rounded.Search,
            selected = false, showCheck = false, shape = AppShapes.ListItem, leaf = true,
            onClick = { actions.onOpenSearchSettings() },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EffortPane(
    state: PlusMenuState,
    actions: PlusMenuActions,
    select: SelectHandler,
    toast: (String) -> Unit,
    haptics: me.rerere.rikkahub.ui.hooks.PremiumHaptics,
) {
    SubHeader("Effort") { state.showPane(PlusMenuPane.Main) }
    data class Item(val level: EffortLevel, val title: String, val toast: String, val badge: (@Composable () -> Unit)?)
    val items = listOf(
        Item(EffortLevel.Low, "Low", "Effort: Low", null),
        Item(EffortLevel.Medium, "Medium", "Effort: Medium (Default)") { PillBadge("Default") },
        Item(EffortLevel.High, "High", "Effort: High", null),
        Item(EffortLevel.Extra, "Extra", "Effort: Extra", null),
        Item(EffortLevel.Max, "Max", "Effort: Max") { PillBadge("3.5x or more usage", amber = true) },
    )
    items.forEachIndexed { i, item ->
        OptionRow(
            title = item.title, description = null, icon = null, titleBadge = item.badge,
            selected = state.effort == item.level, showCheck = true, shape = groupedShape(i, items.size),
            onClick = { select(item.toast) { state.effort = item.level; actions.onEffortChange(item.level) } },
        )
    }

    // Thinking toggle: the whole card and the switch both toggle it.
    Spacer(Modifier.height(8.dp))
    val toggle = {
        val next = !state.thinking
        haptics.perform(HapticPattern.Pop)
        state.thinking = next
        actions.onThinkingChange(next)
        toast(if (next) "Thinking enabled for complex reasoning" else "Thinking disabled")
    }
    OptionRow(
        title = "Thinking", description = "Can think for more complex tasks", icon = Icons.Rounded.Lightbulb,
        selected = false, showCheck = false, shape = AppShapes.ListItem,
        trailing = { HapticSwitch(checked = state.thinking, onCheckedChange = { toggle() }) },
        onClick = { toggle() },
    )
}

@Composable
private fun SkillsPane(
    state: PlusMenuState,
    actions: PlusMenuActions,
    pinnedSkills: List<PlusMenuSkill>,
    leaf: LeafHandler,
    select: SelectHandler,
) {
    SubHeader("Skills") { state.showPane(PlusMenuPane.Main) }
    OptionRow(
        title = "Auto", description = "Automatically choose relevant skills for each task", icon = Icons.Rounded.Bolt,
        selected = state.skillMode == SkillMode.Auto, showCheck = true, shape = groupedShape(0, 2),
        onClick = {
            select("Skills: Automatic Mode") { state.skillMode = SkillMode.Auto; actions.onSkillModeChange(SkillMode.Auto) }
        },
    )
    OptionRow(
        title = "Manual selection", description = "Pick which skills stay available to the agent", icon = Icons.Rounded.Extension,
        selected = state.skillMode == SkillMode.Manual, showCheck = true, shape = groupedShape(1, 2),
        onClick = {
            select("Skills: Manual Selection Active") { state.skillMode = SkillMode.Manual; actions.onSkillModeChange(SkillMode.Manual) }
        },
    )

    Text(
        text = "Pinned skills",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
    )

    val count = pinnedSkills.size + 1
    pinnedSkills.forEachIndexed { i, skill ->
        OptionRow(
            title = skill.name, description = skill.description, icon = Icons.Rounded.Inventory2,
            selected = false, showCheck = false, shape = groupedShape(i, count), leaf = true,
            trailing = {
                Icon(
                    imageVector = if (skill.trailing == SkillTrailing.Pinned) Icons.Rounded.PushPin else Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            onClick = { leaf(skill.toast) { actions.onSkillClick(skill) } },
        )
    }
    OptionRow(
        title = "Add skills", description = "Browse the skill directory", icon = Icons.Rounded.ShoppingBag,
        selected = false, showCheck = false, shape = groupedShape(count - 1, count), leaf = true,
        onClick = { leaf("Directory opened", actions.onAddSkills) },
    )
}

/* ============================================================
 * ROWS
 * ============================================================ */
@Stable
private class LeafFlash {
    var active by mutableStateOf(false)
}

@Composable
private fun rememberLeafFlash(): LeafFlash {
    val flash = remember { LeafFlash() }
    LaunchedEffect(flash.active) {
        if (flash.active) {
            delay(140)
            flash.active = false
        }
    }
    return flash
}

@Composable
private fun LeafFlash.alpha(): Float =
    animateFloatAsState(if (active) 0.55f else 1f, tween(120), label = "leafFlash").value

/**
 * Single icon slot for every + menu row. All menu icons are filled
 * `Icons.Rounded` vectors rendered at 24dp; the tint follows the row state.
 * Route new row icons through here so the set cannot drift.
 */
@Composable
private fun PlusMenuRowIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint,
        )
    }
}

/** Main-menu row: same layout as MinimalChatInput's MinimalPickerItem. */
@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    active: Boolean = false,
    leaf: Boolean = false,
    onClick: () -> Unit,
) {
    val flash = rememberLeafFlash()
    val flashAlpha = flash.alpha()
    Surface(
        onClick = {
            if (leaf) flash.active = true
            onClick()
        },
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = flashAlpha },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlusMenuRowIcon(
                icon = icon,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubHeader(title: String, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onBack,
            shape = AppShapes.IconButton,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(AppSize.ChromePill),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(AppSize.Icon),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppSize.ChromePill + 8.dp),
        )
    }
}

/**
 * Grouped option row. Idle: surfaceContainerHighest. Selected: primaryContainer with a trailing
 * check, exactly like ReasoningPicker's selected rows.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionRow(
    title: String,
    description: String?,
    icon: ImageVector?,
    selected: Boolean,
    showCheck: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    leaf: Boolean = false,
    titleBadge: (@Composable () -> Unit)? = null,
    extra: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val flash = rememberLeafFlash()
    val flashAlpha = flash.alpha()
    val container by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer else scheme.surfaceContainerHighest,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "optionContainer",
    )
    val content = if (selected) scheme.onPrimaryContainer else scheme.onSurface
    val checkAlpha by animateFloatAsState(if (showCheck && selected) 1f else 0f, tween(180), label = "checkAlpha")

    CompositionLocalProvider(LocalContentColor provides content) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = flashAlpha }
                .clip(shape)
                .background(container)
                .clickable {
                    if (leaf) flash.active = true
                    onClick()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (icon != null) {
                PlusMenuRowIcon(
                    icon = icon,
                    tint = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, color = content)
                    titleBadge?.invoke()
                }
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) scheme.onPrimaryContainer.copy(alpha = 0.7f) else scheme.onSurfaceVariant,
                    )
                }
                extra?.invoke()
            }
            when {
                trailing != null -> trailing()
                showCheck -> Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = if (selected) "Selected" else null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            alpha = checkAlpha
                            val s = 0.6f + 0.4f * checkAlpha
                            scaleX = s
                            scaleY = s
                        },
                )
            }
        }
    }
}

@Composable
private fun TagChip(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(AppShapes.Chip)
            .background(LocalContentColor.current.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

/** "Default" uses the secondary container; the amber usage warning uses LastChat's orange extend colours. */
@Composable
private fun PillBadge(text: String, amber: Boolean = false) {
    val background = if (amber) MaterialTheme.extendColors.orange2 else MaterialTheme.colorScheme.secondaryContainer
    val foreground = if (amber) MaterialTheme.extendColors.orange8 else MaterialTheme.colorScheme.onSecondaryContainer
    Box(
        Modifier
            .clip(AppShapes.Tag)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = foreground)
    }
}

/* ============================================================
 * LABELS
 * ============================================================ */
private fun effortLabel(level: EffortLevel) = when (level) {
    EffortLevel.Low -> "Low"
    EffortLevel.Medium -> "Medium"
    EffortLevel.High -> "High"
    EffortLevel.Extra -> "Extra"
    EffortLevel.Max -> "Max"
}

private fun approvalLabel(mode: ApprovalMode) = when (mode) {
    ApprovalMode.Auto -> "Auto Approve"
    ApprovalMode.AllowList -> "Allow List"
    ApprovalMode.Manual -> "Manual"
}
