package me.rerere.rikkahub.ui.components.ai

import android.net.Uri
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.rikkahub.ui.components.ui.SearchProviderIcon
import me.rerere.search.SearchServiceOptions
import coil3.compose.AsyncImage
import me.rerere.ai.ui.UIMessagePart
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.spring
import androidx.compose.ui.zIndex
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.verticalScroll
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import me.rerere.ai.provider.Model
import me.rerere.ai.ui.ToolApprovalState
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.ai.models.ModelCatalogService
import me.rerere.rikkahub.data.ai.mcp.McpManager
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.findProvider
import me.rerere.rikkahub.data.datastore.resolveConversationContext
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.model.canManuallySummarizeConversation
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.data.model.SKILL_SELECTION_OVERRIDE_ID
import me.rerere.rikkahub.data.model.hasManualSkillSelectionOverride
import me.rerere.rikkahub.data.model.Skill
import me.rerere.rikkahub.data.model.withoutSkillSelectionOverride
import me.rerere.rikkahub.service.ChatService
import me.rerere.rikkahub.ui.components.crop.CropImageScreen
import me.rerere.rikkahub.ui.components.chat.LastChatComposerPlusButton
import me.rerere.rikkahub.ui.components.chat.LastChatComposerAction
import me.rerere.rikkahub.ui.components.chat.LastChatComposerActionButton
import me.rerere.rikkahub.ui.components.chat.LastChatComposerCapsule
import me.rerere.rikkahub.ui.components.chat.LastChatComposerAttachmentRow
import me.rerere.rikkahub.ui.components.chat.LastChatComposerAudioIcon
import me.rerere.rikkahub.ui.components.chat.LastChatDocumentAttachmentTile
import me.rerere.rikkahub.ui.components.chat.LastChatComposerImageAttachment
import me.rerere.rikkahub.ui.components.chat.LastChatComposerInputShape
import me.rerere.rikkahub.ui.components.chat.LastChatComposerMediaAttachment
import me.rerere.rikkahub.ui.components.chat.LastChatComposerRow
import me.rerere.rikkahub.ui.components.chat.LastChatComposerVideoIcon
import me.rerere.rikkahub.ui.components.ui.icons.ModeIcons
import me.rerere.rikkahub.ui.components.ui.permission.PermissionCamera
import me.rerere.rikkahub.ui.components.ui.permission.PermissionMicrophone
import me.rerere.rikkahub.ui.components.ui.permission.rememberPermissionState
import me.rerere.rikkahub.ui.theme.AppShapes
import me.rerere.rikkahub.ui.theme.AppSize
import me.rerere.rikkahub.ui.theme.LocalDarkMode
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.context.LocalSTTState
import me.rerere.rikkahub.ui.hooks.ChatInputState
import me.rerere.rikkahub.ui.hooks.HapticPattern
import me.rerere.rikkahub.ui.hooks.mergeCommittedSttTranscript
import me.rerere.rikkahub.ui.hooks.rememberPremiumHaptics
import me.rerere.rikkahub.ui.hooks.shouldApplyPendingSttTranscript
import me.rerere.rikkahub.ui.modifier.blurredContainerColor
import me.rerere.rikkahub.ui.modifier.lastChatBlurEffect
import me.rerere.rikkahub.data.ai.tools.LocalToolOption
import me.rerere.rikkahub.data.ai.tools.AskUserAnswer
import me.rerere.rikkahub.data.ai.tools.AskUserAnswerPayload
import me.rerere.rikkahub.data.ai.tools.AskUserOption
import me.rerere.rikkahub.data.ai.tools.AskUserQuestionnaire
import me.rerere.rikkahub.data.ai.tools.ASK_USER_TOOL_NAME
import me.rerere.rikkahub.data.ai.tools.findPendingAskUserToolCall
import me.rerere.rikkahub.data.ai.tools.toJsonElement
import me.rerere.rikkahub.data.repository.ChatAttachmentManager
import me.rerere.rikkahub.data.repository.WorkspaceRepository
import me.rerere.rikkahub.utils.JsonInstantPretty
import me.rerere.rikkahub.utils.jsonPrimitiveOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.time.Instant
import kotlin.uuid.Uuid
import org.koin.compose.koinInject

/**
 * Minimal ChatGPT-style input bar with bottom sheet picker.
 * Shows a simple input bar with + button, text field, and send button.
 * The + button opens a bottom sheet with file upload, model picker, and other options.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun MinimalChatInput(
    state: ChatInputState,
    conversation: Conversation,
    settings: Settings,
    mcpManager: McpManager,
    enableSearch: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    chatSuggestions: List<String> = emptyList(),
    onClickSuggestion: (String) -> Unit = {},
    onUpdateChatModel: (Model) -> Unit,
    onUpdateAssistant: (Assistant) -> Unit,
    onUpdateConversation: (Conversation) -> Unit,
    onToolApproval: (toolCallId: String, approved: Boolean, reason: String, answer: String?) -> Unit,
    onUpdateSearchService: (Int) -> Unit,
    onClearContext: () -> Unit,
    onCancelClick: () -> Unit,
    onSendClick: () -> Unit,
    onLongSendClick: () -> Unit,
    onNavigateToLorebook: (String) -> Unit = {},
    onNavigateToWorkspace: (workspaceId: String?) -> Unit = {},
    onRefreshContext: suspend () -> ChatService.ContextRefreshResult = {
        ChatService.ContextRefreshResult(
            success = false,
            errorResId = R.string.context_refresh_no_summarizer,
        )
    },
    onDeleteFile: (Uri) -> Unit = {},
    bottomAccessory: @Composable (() -> Unit)? = null,
    showScrollToBottomButton: Boolean = false,
    onScrollToBottomClick: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
) {
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val conversationContext = remember(settings, conversation) {
        settings.resolveConversationContext(conversation)
    }
    val assistant = conversationContext.assistant
    val currentChatModel = conversationContext.chatModel
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val workspaceRepository = koinInject<WorkspaceRepository>()
    val modelCatalog = koinInject<ModelCatalogService>()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val availableSkills = remember(settings.skills) {
        settings.skills
    }
    val availableSkillIds = remember(availableSkills) { availableSkills.map { it.id }.toSet() }
    val assistantAvailableSkillIds = remember(settings.skills, assistant.id) {
        settings.skills.filter { it.isAvailableForAssistant(assistant.id) }.map { it.id }.toSet()
    }
    val pendingQuestionnaire = remember(conversation.messageNodes) {
        conversation.currentMessages.findPendingAskUserToolCall()
    }
    val isQuestionnaireActive = pendingQuestionnaire != null
    val pendingToolApproval = remember(conversation.messageNodes, pendingQuestionnaire?.toolCallId) {
        if (pendingQuestionnaire != null) {
            null
        } else {
            conversation.currentMessages.findPendingToolApproval()
        }
    }
    val isToolApprovalActive = pendingToolApproval != null
    val questionnaire = pendingQuestionnaire?.questionnaire
    val questionnaireToolCallId = pendingQuestionnaire?.toolCallId
    var questionnaireIndex by rememberSaveable(questionnaireToolCallId) { mutableStateOf(0) }
    var questionnaireSelectedOptions by rememberSaveable(questionnaireToolCallId) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var questionnaireCustomAnswers by rememberSaveable(questionnaireToolCallId) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    val questionnaireTextState = remember(questionnaireToolCallId) { TextFieldState() }
    val toolApprovalTextState = remember(pendingToolApproval?.toolCallId) { TextFieldState() }
    val currentQuestion = questionnaire?.questions?.getOrNull(questionnaireIndex)
    val isFinalQuestion = questionnaire != null && questionnaireIndex == questionnaire.questions.lastIndex

    val attachedUnsupportedArchives = remember(state.pendingAttachments) {
        state.pendingAttachments
            .map { it.part }
            .filterIsInstance<UIMessagePart.Document>()
            .filter { me.rerere.rikkahub.data.ai.transformers.isArchiveOrBinaryFile(it.fileName, it.mime) }
    }
    var dismissedWorkspaceRequiredCardForAttachments by remember { mutableStateOf<Set<String>>(emptySet()) }
    // Prune dismissed ids for attachments that no longer exist so re-attaching
    // the same file later shows the card again instead of staying dismissed.
    LaunchedEffect(attachedUnsupportedArchives.map { it.url }) {
        val current = attachedUnsupportedArchives.map { it.url }.toSet()
        dismissedWorkspaceRequiredCardForAttachments =
            dismissedWorkspaceRequiredCardForAttachments.intersect(current)
    }
    val activeUnsupportedArchive = attachedUnsupportedArchives.firstOrNull { it.url !in dismissedWorkspaceRequiredCardForAttachments }
    val isWorkspaceRequiredCardVisible = activeUnsupportedArchive != null && assistant.workspaceId == null

    // OLED dark mode detection for buttons (not sheet backgrounds)
    val amoledMode by me.rerere.rikkahub.ui.hooks.rememberAmoledDarkMode()
    val isDarkMode = me.rerere.rikkahub.ui.theme.LocalDarkMode.current
    val isAmoled = amoledMode && isDarkMode

    // Camera permission - must be in parent, not inside ModalBottomSheet
    val cameraPermission = rememberPermissionState(PermissionCamera)
    val microphonePermission = rememberPermissionState(PermissionMicrophone)
    val stt = LocalSTTState.current
    val sttState by stt.state.collectAsStateWithLifecycle()
    val hasSelectedSttProvider = settings.sttModelId != null
    var sttDraft by remember { mutableStateOf("") }
    var acceptSttWhenIdle by remember { mutableStateOf(false) }
    var discardSttWhenIdle by remember { mutableStateOf(false) }
    var sttEpochAtStop by remember { mutableIntStateOf(0) }
    val sttRecording = sttState.isRecording
    val sttFinalizing = sttState.status == me.rerere.asr.ASRStatus.Stopping

    LaunchedEffect(sttState.errorMessage) {
        sttState.errorMessage?.let { error ->
            toaster.show(error, type = me.rerere.rikkahub.ui.components.ui.ToastType.Error)
            stt.clearError()
        }
    }

    fun startSttRecording() {
        if (!microphonePermission.allRequiredPermissionsGranted) {
            microphonePermission.requestPermissions()
            return
        }
        sttDraft = ""
        acceptSttWhenIdle = false
        discardSttWhenIdle = false
        sttEpochAtStop = state.sttCommitEpoch
        keyboardController?.hide()
        stt.start { transcript ->
            sttDraft = transcript
        }
    }

    fun stopSttRecording(accept: Boolean) {
        sttEpochAtStop = state.sttCommitEpoch
        acceptSttWhenIdle = accept
        discardSttWhenIdle = !accept
        stt.stop()
    }

    LaunchedEffect(sttState.transcript) {
        if (sttState.transcript.isNotBlank()) {
            sttDraft = sttState.transcript
        }
    }

    LaunchedEffect(sttRecording, sttFinalizing, acceptSttWhenIdle, discardSttWhenIdle) {
        if (!sttRecording && !sttFinalizing && (acceptSttWhenIdle || discardSttWhenIdle)) {
            if (
                shouldApplyPendingSttTranscript(
                    accept = acceptSttWhenIdle,
                    epochAtCapture = sttEpochAtStop,
                    epochNow = state.sttCommitEpoch,
                )
            ) {
                delay(220)
                if (
                    shouldApplyPendingSttTranscript(
                        accept = true,
                        epochAtCapture = sttEpochAtStop,
                        epochNow = state.sttCommitEpoch,
                    )
                ) {
                    val transcript = sttDraft.trim()
                    if (transcript.isNotBlank()) {
                        state.setMessageText(
                            mergeCommittedSttTranscript(
                                existing = state.textContent.text.toString(),
                                transcript = transcript,
                            )
                        )
                        runCatching { state.focusRequester.requestFocus() }
                    }
                }
            }
            sttDraft = ""
            acceptSttWhenIdle = false
            discardSttWhenIdle = false
        }
    }

    // Plus menu sheet state (replaces the old showPicker ModalBottomSheet).
    // The + button toggles this; its active border follows plusMenu.isOpen.
    val plusMenu = rememberPlusMenuState()
    var isFocused by remember { mutableStateOf(false) }
    var isExpandedFullScreen by remember { mutableStateOf(false) }
    var imageToCrop by remember { mutableStateOf<PendingImageCrop?>(null) }
    var pendingAttachmentImports by remember { mutableIntStateOf(0) }
    val isImportingAttachments = pendingAttachmentImports > 0
    val onAttachmentImportStarted = { pendingAttachmentImports += 1 }
    val onAttachmentImportFinished = {
        pendingAttachmentImports = (pendingAttachmentImports - 1).coerceAtLeast(0)
    }

    // Camera state (hoisted from the old picker content)
    var cameraOutputUri by remember { mutableStateOf<Uri?>(null) }
    var cameraOutputFile by remember { mutableStateOf<File?>(null) }

    fun importImages(
        uris: List<Uri>,
        onFinally: () -> Unit = {}
    ) {
        if (uris.isEmpty()) {
            onFinally()
            return
        }

        onAttachmentImportStarted()
        scope.launch {
            try {
                val importedUris = withContext(Dispatchers.IO) {
                    ChatAttachmentManager.importChatFiles(uris)
                }
                if (importedUris.isEmpty()) {
                    Log.w("MinimalChatInput", "Failed to import ${uris.size} selected image(s)")
                    toaster.show(context.getString(R.string.chat_input_selected_image_failed))
                } else {
                    state.addImages(importedUris)
                }
            } finally {
                onAttachmentImportFinished()
                onFinally()
            }
        }
    }

    // Camera launcher (declared before launchCamera so the function can reference it)
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captureSuccessful ->
        val capturedUri = cameraOutputUri
        val capturedFile = cameraOutputFile
        if (captureSuccessful && capturedUri != null) {
            plusMenu.close()
            importImages(
                uris = listOf(capturedUri),
                onFinally = {
                    capturedFile?.delete()
                    cameraOutputFile = null
                    cameraOutputUri = null
                }
            )
        } else {
            capturedFile?.delete()
            cameraOutputFile = null
            cameraOutputUri = null
        }
    }

    fun launchCamera() {
        if (!cameraPermission.allRequiredPermissionsGranted) {
            cameraPermission.requestPermissions()
            return
        }
        val outputFile = runCatching {
            context.cacheDir.resolve("camera_${Uuid.random()}.jpg")
        }.getOrNull()
        val outputUri = runCatching {
            if (outputFile == null) error("cache unavailable")
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile,
            )
        }.getOrNull()
        if (outputFile == null || outputUri == null) {
            Log.w("MinimalChatInput", "Failed to create camera output file")
            toaster.show(context.getString(R.string.chat_input_selected_image_failed))
            runCatching { outputFile?.delete() }
            cameraOutputFile = null
            cameraOutputUri = null
            return
        }
        cameraOutputFile = outputFile
        cameraOutputUri = outputUri
        cameraLauncher.launch(outputUri)
    }

    // Photo picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { selectedUris ->
        if (selectedUris.isNotEmpty()) {
            plusMenu.close()
            importImages(uris = selectedUris)
        }
    }

    // File picker launcher - categorizes files by type
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { selectedUris ->
        if (selectedUris.isNotEmpty()) {
            plusMenu.close()
            val isWorkspaceEnabled = assistant.workspaceId != null
            onAttachmentImportStarted()
            scope.launch {
                try {
                    val importedFiles = withContext(Dispatchers.IO) {
                        context.prepareImportedPickerFiles(
                            selectedUris = selectedUris,
                            isWorkspaceEnabled = isWorkspaceEnabled,
                        )
                    }

                    importedFiles.unsupportedFileNames.forEach { fileName ->
                        toaster.show(
                            context.getString(
                                R.string.chat_input_unsupported_file_type,
                                fileName
                            )
                        )
                    }
                    importedFiles.failedFileNames.forEach { fileName ->
                        toaster.show(context.getString(R.string.chat_input_add_file_failed, fileName))
                    }

                    if (importedFiles.imageUris.isNotEmpty()) {
                        state.addImages(importedFiles.imageUris)
                    }
                    if (importedFiles.documents.isNotEmpty()) {
                        state.addFiles(importedFiles.documents)
                    }
                } finally {
                    onAttachmentImportFinished()
                }
            }
        }
    }

    // Sub-sheet states (hoisted from the old picker content)
    var showModelPicker by remember { mutableStateOf(false) }
    var showSkillsPicker by remember { mutableStateOf(false) }
    var showLorebooksPicker by remember { mutableStateOf(false) }
    var showPluginsPicker by remember { mutableStateOf(false) }
    var showContextRefreshDialog by remember { mutableStateOf(false) }
    var showContextSummaryEditDialog by remember { mutableStateOf(false) }
    var editableContextSummary by remember(conversation.contextSummary) {
        mutableStateOf(conversation.contextSummary.orEmpty())
    }
    var showSearchPicker by remember { mutableStateOf(false) }

    // Skill id derivations (same formulae as SkillsPickerSheet)
    val assistantDefaultSkillIds = assistant.enabledSkillIds.intersect(assistantAvailableSkillIds)
    val alwaysEnabledSkillIds = availableSkills
        .filter { it.alwaysEnabled && assistantAvailableSkillIds.contains(it.id) }
        .map { it.id }
        .toSet()
    val effectiveActiveSkillIds = if (conversation.enabledModeIds.hasManualSkillSelectionOverride() || conversation.enabledModeIds.isNotEmpty()) {
        conversation.enabledModeIds.withoutSkillSelectionOverride()
    } else {
        assistantDefaultSkillIds + alwaysEnabledSkillIds
    }.intersect(availableSkillIds)

    // Track the last valid search provider index so selection persists when search is disabled
    val initialProviderIndex = when (val mode = assistant.searchMode) {
        is me.rerere.rikkahub.data.model.AssistantSearchMode.Provider -> mode.index
        else -> settings.searchServiceSelected.coerceAtLeast(0)
    }
    var lastValidProviderIndex by rememberSaveable(initialProviderIndex) { mutableStateOf(initialProviderIndex) }

    // Update lastValidProviderIndex when a valid external index is set
    val currentProviderIndex = when (val mode = assistant.searchMode) {
        is me.rerere.rikkahub.data.model.AssistantSearchMode.Provider -> mode.index
        else -> -1
    }
    // Sync immediately when currentProviderIndex changes (no LaunchedEffect delay to prevent flickering)
    if (currentProviderIndex >= 0 && currentProviderIndex < settings.searchServices.size && currentProviderIndex != lastValidProviderIndex) {
        lastValidProviderIndex = currentProviderIndex
    }

    // Calculate effective provider index (use tracked value when current is invalid)
    val effectiveProviderIndex = if (currentProviderIndex >= 0 && currentProviderIndex < settings.searchServices.size) {
        currentProviderIndex
    } else {
        lastValidProviderIndex.coerceIn(0, (settings.searchServices.size - 1).coerceAtLeast(0))
    }

    LaunchedEffect(questionnaireToolCallId, questionnaire?.questions?.size) {
        if (questionnaire == null) {
            questionnaireIndex = 0
            questionnaireTextState.setTextAndPlaceCursorAtEnd("")
        } else {
            questionnaireIndex = questionnaireIndex.coerceIn(0, questionnaire.questions.lastIndex)
            val initialText = questionnaire.questions
                .getOrNull(questionnaireIndex)
                ?.let { question -> questionnaireCustomAnswers[question.id].orEmpty() }
                .orEmpty()
            questionnaireTextState.setTextAndPlaceCursorAtEnd(initialText)
        }
    }

    LaunchedEffect(currentQuestion?.id) {
        val nextText = currentQuestion?.let { questionnaireCustomAnswers[it.id].orEmpty() }.orEmpty()
        if (questionnaireTextState.text.toString() != nextText) {
            questionnaireTextState.setTextAndPlaceCursorAtEnd(nextText)
        }
    }
    LaunchedEffect(currentQuestion?.id, questionnaireTextState.text.toString()) {
        val question = currentQuestion ?: return@LaunchedEffect
        questionnaireCustomAnswers = questionnaireCustomAnswers + (question.id to questionnaireTextState.text.toString())
    }
    LaunchedEffect(pendingToolApproval?.toolCallId) {
        toolApprovalTextState.setTextAndPlaceCursorAtEnd("")
    }

    // Collapse plus menu when keyboard opens
    val imeVisible = WindowInsets.isImeVisible
    val focusManager = LocalFocusManager.current
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            plusMenu.close()
        } else {
            focusManager.clearFocus()
        }
    }
    LaunchedEffect(isQuestionnaireActive, isToolApprovalActive) {
        if (isQuestionnaireActive || isToolApprovalActive) {
            plusMenu.close()
        }
    }

    fun buildQuestionnairePayload(dismissed: Boolean): String? {
        val activeQuestionnaire = questionnaire ?: return null
        val payload = AskUserAnswerPayload(
            answers = activeQuestionnaire.questions.map { question ->
                val custom = questionnaireCustomAnswers[question.id]?.trim().orEmpty()
                val selectedOption = questionnaireSelectedOptions[question.id]?.trim().orEmpty()
                when {
                    custom.isNotBlank() -> AskUserAnswer(
                        id = question.id,
                        status = "answered",
                        source = "custom",
                        value = custom,
                    )

                    selectedOption.isNotBlank() -> AskUserAnswer(
                        id = question.id,
                        status = "answered",
                        source = "option",
                        value = selectedOption,
                    )

                    else -> AskUserAnswer(
                        id = question.id,
                        status = "skipped",
                    )
                }
            },
            dismissed = dismissed,
        )
        return JsonInstantPretty.encodeToString(
            kotlinx.serialization.json.JsonElement.serializer(),
            payload.toJsonElement()
        )
    }

    fun submitQuestionnaire(dismissed: Boolean) {
        val toolCallId = questionnaireToolCallId ?: return
        val payload = buildQuestionnairePayload(dismissed) ?: return
        keyboardController?.hide()
        haptics.perform(if (dismissed) HapticPattern.Pop else HapticPattern.Send)
        onToolApproval(toolCallId, true, "", payload)
    }

    fun approvePendingTool(alwaysApproveWorkspace: Boolean) {
        val pending = pendingToolApproval ?: return
        keyboardController?.hide()
        haptics.perform(if (alwaysApproveWorkspace) HapticPattern.Success else HapticPattern.Send)
        if (alwaysApproveWorkspace && pending.isWorkspaceTool) {
            val workspaceId = assistant.workspaceId?.toString()
            if (workspaceId != null) {
                scope.launch {
                    workspaceRepository.setToolApproval(workspaceId, pending.toolName, needsApproval = false)
                    onToolApproval(pending.toolCallId, true, "", null)
                }
                return
            }
        }
        onToolApproval(pending.toolCallId, true, "", null)
    }

    fun denyPendingToolWithInstruction() {
        val pending = pendingToolApproval ?: return
        val reason = toolApprovalTextState.text.toString().trim()
        if (reason.isBlank()) {
            haptics.perform(HapticPattern.Pop)
            state.focusRequester.requestFocus()
            keyboardController?.show()
            return
        }
        keyboardController?.hide()
        haptics.perform(HapticPattern.Send)
        onToolApproval(pending.toolCallId, false, reason, null)
    }

    fun advanceQuestionnaire() {
        val activeQuestionnaire = questionnaire ?: return
        if (isFinalQuestion) {
            submitQuestionnaire(dismissed = false)
            return
        }
        questionnaireIndex = (questionnaireIndex + 1).coerceAtMost(activeQuestionnaire.questions.lastIndex)
        haptics.perform(HapticPattern.Pop)
    }

    fun sendMessage() {
        if (isQuestionnaireActive) {
            val question = currentQuestion
            if (question != null) {
                val trimmed = questionnaireTextState.text.toString().trim()
                questionnaireCustomAnswers = questionnaireCustomAnswers + (question.id to trimmed)
            }
            advanceQuestionnaire()
            return
        }
        if (isToolApprovalActive) {
            denyPendingToolWithInstruction()
            return
        }
        keyboardController?.hide()
        haptics.perform(HapticPattern.Send)
        if (state.loading) onCancelClick() else onSendClick()
    }

    fun replaceCroppedImage(original: PendingImageCrop, croppedUri: Uri) {
        scope.launch {
            val importedUri = withContext(Dispatchers.IO) {
                ChatAttachmentManager.importChatFiles(listOf(croppedUri)).firstOrNull()
            }
            if (importedUri == null) {
                Log.w("MinimalChatInput", "Failed to import cropped image: $croppedUri")
                toaster.show(context.getString(R.string.chat_input_selected_image_failed))
                withContext(Dispatchers.IO) {
                    if (croppedUri.scheme == "file") {
                        runCatching { File(croppedUri.path.orEmpty()).delete() }
                    }
                }
                return@launch
            }

            state.replaceAttachment(
                instanceId = original.instanceId,
                part = UIMessagePart.Image(importedUri.toString()),
            )
            withContext(Dispatchers.IO) {
                if (croppedUri.scheme == "file") {
                    runCatching { File(croppedUri.path.orEmpty()).delete() }
                }
            }
            haptics.perform(HapticPattern.Success)
        }
    }

    imageToCrop?.let { image ->
        CropImageScreen(
            sourceUri = image.image.url.toUri(),
            onCropComplete = { croppedUri ->
                imageToCrop = null
                replaceCroppedImage(image, croppedUri)
            },
            onCancel = {
                imageToCrop = null
                haptics.perform(HapticPattern.Pop)
            }
        )
    }

    // Capsule metrics, px-spec driven and converted with the device density
    // (at 3.0x: 120px = 40dp, 44px ≈ 14.5dp).
    // Inactive (keyboard closed): 120px side insets, bottom edge 120px from
    //   the physical screen bottom INCLUDING the gesture inset, so the bottom
    //   padding is 120px minus the nav-bar inset -- never stacked on top of it.
    // Active (keyboard open): 44px side insets, 44px gap above the keyboard.
    // Thickness matches the hamburger button container (AppSize.ChromePill)
    // in both states; insets/gap animate (~200ms).
    val capsuleDensity = LocalDensity.current
    val capsuleActiveInset = with(capsuleDensity) { 44f.toDp() }
    val capsuleInactiveInset = with(capsuleDensity) { 120f.toDp() }
    val capsulePxHeight = with(capsuleDensity) { 166f.toDp() }
    val navBottomInset = with(capsuleDensity) {
        WindowInsets.navigationBars.getBottom(capsuleDensity).toDp()
    }
    val capsuleInactiveBottom =
        (with(capsuleDensity) { 120f.toDp() } - navBottomInset).coerceAtLeast(0.dp)
    val animatedCapsuleHInset by animateDpAsState(
        targetValue = if (imeVisible) capsuleActiveInset else capsuleInactiveInset,
        animationSpec = tween(
            durationMillis = 200,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "capsule_h_inset"
    )
    val animatedCapsuleBottomGap by animateDpAsState(
        targetValue = if (imeVisible) capsuleActiveInset else capsuleInactiveBottom,
        animationSpec = tween(
            durationMillis = 200,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "capsule_bottom_gap"
    )
    // Lowest suggestion row: vertical center 129px above the capsule top.
    // Row is 36.dp tall with 8.dp column spacing, so the extra pad below it
    // is 129px minus half the row minus the column spacing.
    val aboveCapsulePad = (
        with(capsuleDensity) { 129f.toDp() } - 18.dp - 8.dp
        ).coerceAtLeast(0.dp)

    // Root stays wrap-content: the composer column sizes itself and the caller
    // anchors it at the bottom. The plus menu below renders in its own
    // full-screen dialog window, so it is unaffected by this root's size.
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            // Capsule spacing, px-spec driven:
            // keyboard OPEN   -> imePadding() follows the keyboard, then the
            //   44px gap sits AFTER it; nothing else (no nav-bars inset, no
            //   spacer) is stacked on top or the gap would exceed 44px.
            // keyboard CLOSED -> resting position: ONLY the 120px-minus-inset
            //   bottom gap, no imePadding/navigationBarsPadding stacked on it.
            // dp only, no hardcoded pixels.
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = animatedCapsuleHInset)
                .then(if (imeVisible) Modifier.imePadding() else Modifier)
                .padding(bottom = animatedCapsuleBottomGap)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val showSuggestions = !isQuestionnaireActive && !isToolApprovalActive && chatSuggestions.isNotEmpty()
            val showScrollToBottom = !isQuestionnaireActive && !isToolApprovalActive && showScrollToBottomButton

            // Suggestions and scroll-to-bottom affordance share a row so they never overlap.
            // Lowest row center sits 129px above the capsule top (via aboveCapsulePad).
            androidx.compose.animation.AnimatedVisibility(
                visible = showSuggestions || showScrollToBottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = aboveCapsulePad),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showSuggestions) {
                        ChatSuggestionsRow(
                            modifier = Modifier.weight(1f),
                            suggestions = chatSuggestions,
                            onClickSuggestion = onClickSuggestion
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showScrollToBottom,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End),
                    ) {
                        ChatScrollToBottomButton(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onScrollToBottomClick()
                            }
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isWorkspaceRequiredCardVisible && !isQuestionnaireActive && !isToolApprovalActive,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                activeUnsupportedArchive?.let { doc ->
                    WorkspaceRequiredCard(
                        fileName = doc.fileName,
                        onDismiss = {
                            dismissedWorkspaceRequiredCardForAttachments = dismissedWorkspaceRequiredCardForAttachments + doc.url
                            haptics.perform(HapticPattern.Pop)
                        }
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isQuestionnaireActive && questionnaire != null,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                questionnaire?.let { activeQuestionnaire ->
                    CharacterQuestionsCard(
                        questionnaire = activeQuestionnaire,
                        currentIndex = questionnaireIndex,
                        selectedOptionLabel = currentQuestion?.let { question ->
                            questionnaireSelectedOptions[question.id]
                        },
                        onPrevious = {
                            questionnaireIndex = (questionnaireIndex - 1).coerceAtLeast(0)
                            haptics.perform(HapticPattern.Pop)
                        },
                        onNext = {
                            questionnaireIndex = (questionnaireIndex + 1)
                                .coerceAtMost(activeQuestionnaire.questions.lastIndex)
                            haptics.perform(HapticPattern.Pop)
                        },
                        onDismiss = {
                            currentQuestion?.let { question ->
                                questionnaireCustomAnswers =
                                    questionnaireCustomAnswers + (question.id to questionnaireTextState.text.toString())
                            }
                            submitQuestionnaire(dismissed = true)
                        },
                        onSelectOption = { option ->
                            val question = currentQuestion ?: return@CharacterQuestionsCard
                            questionnaireSelectedOptions = questionnaireSelectedOptions + (question.id to option.label)
                            questionnaireCustomAnswers = questionnaireCustomAnswers + (question.id to "")
                            questionnaireTextState.setTextAndPlaceCursorAtEnd("")
                            haptics.perform(HapticPattern.Pop)
                            if (!isFinalQuestion) {
                                questionnaireIndex = (questionnaireIndex + 1)
                                    .coerceAtMost(activeQuestionnaire.questions.lastIndex)
                            }
                        }
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isToolApprovalActive && pendingToolApproval != null,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                pendingToolApproval?.let { pending ->
                    ToolApprovalCard(
                        approval = pending,
                        onApprove = { approvePendingTool(alwaysApproveWorkspace = false) },
                        onAlwaysApproveWorkspace = { approvePendingTool(alwaysApproveWorkspace = true) },
                        onDenyWithInstruction = {
                            state.focusRequester.requestFocus()
                            keyboardController?.show()
                            haptics.perform(HapticPattern.Pop)
                        }
                    )
                }
            }

            // Content receiver for clipboard image paste (must be outside Surface lambda)
            val receiveContentListener = remember(isQuestionnaireActive) {
                ReceiveContentListener { transferableContent ->
                    when {
                        isQuestionnaireActive -> transferableContent
                        transferableContent.hasMediaType(MediaType.Image) -> {
                            transferableContent.consume { item ->
                                item.uri?.let { uri ->
                                    onAttachmentImportStarted()
                                    scope.launch {
                                        try {
                                            val importedUris = withContext(Dispatchers.IO) {
                                                ChatAttachmentManager.importChatFiles(listOf(uri))
                                            }
                                            if (importedUris.isNotEmpty()) {
                                                state.addImages(importedUris)
                                            }
                                        } finally {
                                            onAttachmentImportFinished()
                                        }
                                    }
                                }
                                item.uri != null
                            }
                        }
                        else -> transferableContent
                    }
                }
            }

            // Message capsule: sides come from the outer container, so the
            // row adds no extra horizontal padding (no double margin).
            // Fixed 166px pill (radius = height / 2) in the inactive/empty
            // state; grows only once focused with content. Height never
            // animates between states -- only insets/gap do.
            // All contents sit centered between the upper and lower edges:
            // the row is CenterVertically aligned, the action button is
            // CenterEnd, and the text field uses zero vertical padding.
            // Text field capsule with embedded 37.dp action button
            val inputShape = RoundedCornerShape(capsulePxHeight / 2)
            // Growth gate: inactive/empty stays exactly 166px; focused with
            // content may grow upward via min-height instead of fixed height.
            val capsuleHeightModifier =
                if (isFocused && !state.isEmpty()) Modifier.heightIn(min = capsulePxHeight)
                else Modifier.height(capsulePxHeight)
            LastChatComposerRow {
                LastChatComposerCapsule(
                    containerColor = blurredContainerColor(MaterialTheme.colorScheme.surfaceContainer),
                    shape = inputShape,
                    modifier = Modifier
                        .weight(1f)
                        .then(capsuleHeightModifier)
                        .animateContentSize(
                            tween(
                                MessageCapsuleBehavior.SizeAnimationMs,
                                easing = MessageCapsuleBehavior.Easing
                            )
                        )
                        .lastChatBlurEffect(MaterialTheme.colorScheme.surfaceContainer, inputShape),
                ) {
                    Column(
                        // Fills the capsule min height so short content sits
                        // centered between the upper and lower edges.
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = capsulePxHeight),
                        verticalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isQuestionnaireActive && !isToolApprovalActive && state.messageContent.isNotEmpty(),
                            enter = fadeIn(
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                            ) + expandVertically(
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                                expandFrom = Alignment.Top
                            ),
                            exit = fadeOut(
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 360f)
                            ) + shrinkVertically(
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 360f),
                                shrinkTowards = Alignment.Top
                            )
                        ) {
                            MediaFileInputRow(
                                state = state,
                                onDelete = { uri ->
                                    haptics.perform(HapticPattern.Pop)
                                    onDeleteFile(uri)
                                },
                                onCropImage = { instanceId, image ->
                                    haptics.perform(HapticPattern.Pop)
                                    imageToCrop = PendingImageCrop(instanceId, image)
                                }
                            )
                        }

                        // Editing indicator - shown when editing a message
                        if (!isQuestionnaireActive && !isToolApprovalActive && state.isEditing()) {
                            Surface(
                                color = if (LocalDarkMode.current)
                                    MaterialTheme.colorScheme.surfaceContainerLowest  // Darker in dark mode
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHighest,  // Darker in light mode
                                shape = RoundedCornerShape(16.dp),  // Optical roundness: 24dp outer - 8dp padding = 16dp
                                modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 8.dp, bottom = 4.dp)  // Aligned with text
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.editing),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            state.editingMessage = null
                                            state.clearInput()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.cancel),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Single row: [+] [field ............]
                        // Vertically centered (upper/lower middle), not bottom-anchored,
                        // so the + button and placeholder sit mid-capsule.
                        // Inner px spec: row starts 28px in, so the 112px-wide +
                        // centers at 84px from the capsule's left edge.
                        // (action button stays overlaid end-side, expand stays top-end)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = with(LocalDensity.current) { 28f.toDp() }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Plus button - dedicated container matching the Send
                            // container size/shape/alignment, former + background
                            // color. Border appears only while active/open.
                            if (!isQuestionnaireActive && !isToolApprovalActive) {
                                LastChatComposerPlusButton(
                                    active = plusMenu.isOpen,
                                    onLongClick = {
                                        if (!sttRecording && !sttFinalizing && hasSelectedSttProvider) {
                                            haptics.perform(HapticPattern.Pop)
                                            startSttRecording()
                                        }
                                    },
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        if (sttRecording) {
                                            stopSttRecording(accept = true)
                                        } else {
                                            plusMenu.toggle()
                                            keyboardController?.hide()
                                        }
                                    },
                                    containerColor = blurredContainerColor(MaterialTheme.colorScheme.surfaceContainer),
                                    modifier = Modifier
                                        .size(37.dp)
                                        .lastChatBlurEffect(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                                ) {
                                    Icon(
                                        imageVector = if (sttRecording) Icons.Rounded.Stop else Icons.Rounded.Add,
                                        contentDescription = null,
                                        // + glyph ~63px wide per spec.
                                        modifier = Modifier.size(with(LocalDensity.current) { 63f.toDp() }),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // + (28px pad + 112px) ends 140px in; a compact 12px
                                // gap keeps decent spacing while pulling the
                                // placeholder closer to the left.
                                Spacer(modifier = Modifier.width(with(LocalDensity.current) { 12f.toDp() }))
                            }
                        // Text input with content receiver for paste + overlaid action button
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = (sttRecording || sttFinalizing) && hasSelectedSttProvider,
                                enter = fadeIn(spring(dampingRatio = 0.6f, stiffness = 300f)),
                                exit = fadeOut(tween(140)),
                                modifier = Modifier
                                    .matchParentSize()
                                    .zIndex(10f)
                            ) {
                                Surface(
                                    color = blurredContainerColor(MaterialTheme.colorScheme.surfaceContainer),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .lastChatBlurEffect(MaterialTheme.colorScheme.surfaceContainer, inputShape)
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (sttRecording || sttFinalizing) {
                                                haptics.perform(HapticPattern.Pop)
                                                stopSttRecording(accept = true)
                                            }
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(
                                            top = 12.dp,
                                            bottom = 12.dp,
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = true,
                                            enter = slideInHorizontally(
                                                initialOffsetX = { it / 2 },
                                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                                            ),
                                            exit = slideOutHorizontally(
                                                targetOffsetX = { it / 2 },
                                                animationSpec = tween(140)
                                            )
                                        ) {
                                            if (sttFinalizing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            } else {
                                                STTWaveformLine(
                                                    amplitudes = sttState.amplitudes,
                                                    active = sttRecording,
                                                    modifier = Modifier.fillMaxWidth().height(24.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            val activeTextState = when {
                                isQuestionnaireActive -> questionnaireTextState
                                isToolApprovalActive -> toolApprovalTextState
                                else -> state.textContent
                            }
                            var visualLineCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(1) }
                            // Remembered so the subscription survives recomposition; a fresh
                            // derivedStateOf per recomposition would lose tracking and go stale.
                            val lineCount by androidx.compose.runtime.remember {
                                androidx.compose.runtime.derivedStateOf {
                                    maxOf(activeTextState.text.toString().lines().size, visualLineCount)
                                }
                            }
                            // Bottom-anchored upward expansion: the default line stays
                            // the bottom line. Once text wraps (>= 2 rendered lines),
                            // reserve one text-line height below the field so wrapped
                            // text occupies the lines above and the default bottom
                            // line stays empty. Single-line state keeps zero spacer.
                            val isMultilineInput = lineCount >= 2
                            val reservedDefaultLineHeight = with(LocalDensity.current) {
                                MaterialTheme.typography.bodyLarge.lineHeight.toDp()
                            }
                            // Hoisted so the animation state survives recomposition instead of
                            // being recreated inside the padding calculation on every frame.
                            val inputEndPadding by androidx.compose.animation.core.animateDpAsState(
                                targetValue = if ((sttRecording || sttFinalizing) && hasSelectedSttProvider) 150.dp else 52.dp,
                                animationSpec = tween(220),
                                label = "input_padding"
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.Start,
                            ) {
                            TextField(
                                state = activeTextState,
                                onTextLayout = { getResult ->
                                    val result = getResult()
                                    if (result != null) {
                                        visualLineCount = result.lineCount
                                    }
                                },
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 1.dp)  // Override internal min height (56dp)
                                    .focusRequester(state.focusRequester)
                                    .then(
                                        if (isQuestionnaireActive || isToolApprovalActive) {
                                            Modifier
                                        } else {
                                            Modifier.contentReceiver(receiveContentListener)
                                        }
                                    )
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .sendOnHardwareEnter { sendMessage() },
                                placeholder = {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = !((sttRecording || sttFinalizing) && hasSelectedSttProvider),
                                        enter = fadeIn(tween(220)),
                                        exit = fadeOut(tween(140))
                                    ) {
                                        Text(
                                            text = if (isQuestionnaireActive) {
                                                stringResource(R.string.character_questions_custom_answer_placeholder)
                                            } else if (isToolApprovalActive) {
                                                stringResource(R.string.tool_approval_input_placeholder)
                                            } else {
                                                stringResource(R.string.minimal_chat_input_placeholder, assistant.name)
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                // Up to 10 actual text lines; the reserved empty
                                // default bottom line below the field is excluded
                                // from this limit. At line 11 the field scrolls
                                // internally at a stable max height with the caret
                                // kept visible by the text input itself.
                                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 10),
                                // Zero vertical padding: the fixed-height capsule
                                // centers the field, so padding would push it off-center.
                                // Slim start inset pulls the placeholder closer to
                                // the + button while keeping decent spacing.
                                contentPadding = PaddingValues(
                                    start = 8.dp,
                                    top = 0.dp,
                                    end = inputEndPadding,
                                    bottom = 0.dp,
                                ),
                                colors = TextFieldDefaults.colors().copy(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )
                            // Empty default bottom line: present only in
                            // multiline state; zero height when single-line so
                            // the original appearance and height return exactly.
                            if (isMultilineInput) {
                                Spacer(modifier = Modifier.height(reservedDefaultLineHeight))
                            }
                            } // bottom-anchored text column ends

                            ExpandButtonOverlay(
                                isVisible = !isExpandedFullScreen && lineCount >= 5 && !isQuestionnaireActive && !isToolApprovalActive,
                                onExpand = {
                                    haptics.perform(HapticPattern.Pop)
                                    isExpandedFullScreen = true
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 4.dp, top = 4.dp)
                            )

                            // Action button: 112px circle, 34px from the capsule's
                            // right edge, vertically centered (27px above/below).
                            // No vertical padding inside the row: it would push
                            // the button off-center in the fixed-height capsule.
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(
                                        start = 6.dp,
                                        end = with(LocalDensity.current) { 34f.toDp() }
                                    )
                            ) {
                                AttachmentImportAction(
                                    isImporting = isImportingAttachments,
                                    modifier = Modifier.size(37.dp),
                                ) {
                                    val currentAction = when {
                                    isQuestionnaireActive && isFinalQuestion ->
                                        LastChatComposerAction.QuestionnaireSubmit
                                    isQuestionnaireActive -> LastChatComposerAction.QuestionnaireNext
                                    isToolApprovalActive -> LastChatComposerAction.ToolApprovalDeny
                                    state.loading -> LastChatComposerAction.Loading
                                    !state.isEmpty() -> LastChatComposerAction.Send
                                    hasSelectedSttProvider && sttRecording ->
                                        LastChatComposerAction.SttRecording
                                    hasSelectedSttProvider && sttFinalizing ->
                                        LastChatComposerAction.SttFinalizing
                                    hasSelectedSttProvider && settings.displaySetting.sttReplaceModelIcon ->
                                        LastChatComposerAction.Stt
                                    else -> LastChatComposerAction.Picker
                                }
                                LastChatComposerActionButton(
                                    action = currentAction,
                                    modifier = Modifier.size(37.dp),
                                    onClick = {
                                        when (currentAction) {
                                            LastChatComposerAction.Send,
                                            LastChatComposerAction.Loading,
                                            LastChatComposerAction.ToolApprovalDeny,
                                            LastChatComposerAction.QuestionnaireNext,
                                            LastChatComposerAction.QuestionnaireSubmit -> sendMessage()
                                            LastChatComposerAction.Stt -> {
                                                haptics.perform(HapticPattern.Pop)
                                                startSttRecording()
                                            }
                                            LastChatComposerAction.SttRecording -> {
                                                haptics.perform(HapticPattern.Pop)
                                                stopSttRecording(accept = true)
                                            }
                                            LastChatComposerAction.Picker,
                                            LastChatComposerAction.SttFinalizing -> {
                                                plusMenu.open()
                                            }
                                        }
                                    },
                                ) { action ->
                                    when (action) {
                                        LastChatComposerAction.Loading -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Stop,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        LastChatComposerAction.Send,
                                        LastChatComposerAction.QuestionnaireSubmit -> {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        LastChatComposerAction.QuestionnaireNext -> {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        LastChatComposerAction.ToolApprovalDeny -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        LastChatComposerAction.Stt,
                                        LastChatComposerAction.SttRecording -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Mic,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (action == LastChatComposerAction.SttRecording) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            )
                                        }
                                        LastChatComposerAction.SttFinalizing -> Unit
                                        LastChatComposerAction.Picker -> {
                                            ModelSelector(
                                                modelId = assistant.chatModelId ?: settings.chatModelId,
                                                providers = settings.providers,
                                                onSelect = { onUpdateChatModel(it) },
                                                type = me.rerere.ai.provider.ModelType.CHAT,
                                                onlyIcon = true,
                                                modifier = Modifier.size(34.dp),
                                            )
                                        }
                                    }
                                }
                                }
                            }
                        }  // Box for TextField + Action button ends
                        }  // capsule content Row ends
                    }  // Column ends
                }  // Surface ends
            }  // Row ends

            Box(modifier = Modifier.fillMaxWidth()) {
                bottomAccessory?.invoke()
            }
        }  // Column ends
    }  // Box ends

    if (isExpandedFullScreen) {
        Dialog(
            onDismissRequest = { isExpandedFullScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val dialogView = LocalView.current
            val isDarkTheme = LocalDarkMode.current
            val backgroundColor = MaterialTheme.colorScheme.background

            SideEffect {
                val dialogWindow = findDialogWindow(dialogView)
                dialogWindow?.let { window ->
                    window.setDimAmount(0f)
                    @Suppress("DEPRECATION")
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    @Suppress("DEPRECATION")
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = backgroundColor.luminance() > 0.5f
                        isAppearanceLightNavigationBars = !isDarkTheme
                    }
                }
            }

            val transitionState = remember { MutableTransitionState(false) }
            LaunchedEffect(Unit) {
                transitionState.targetState = true
            }

            // Share the same activeTextState so the text is preserved
            val activeTextState = when {
                isQuestionnaireActive -> questionnaireTextState
                isToolApprovalActive -> toolApprovalTextState
                else -> state.textContent
            }

            // Request focus when dialog opens
            val expandedFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            LaunchedEffect(transitionState.currentState) {
                if (transitionState.currentState) {
                    expandedFocusRequester.requestFocus()
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeIn(tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                ) + fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .statusBarsPadding()
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top bar with minimize button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    isExpandedFullScreen = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FullscreenExit,
                                    contentDescription = "Minimize",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Text input field taking up remaining space
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            TextField(
                                state = activeTextState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .focusRequester(expandedFocusRequester)
                                    .sendOnHardwareEnter {
                                        sendMessage()
                                        isExpandedFullScreen = false
                                    },
                                placeholder = {
                                    Text(
                                        text = if (isQuestionnaireActive) {
                                            stringResource(R.string.character_questions_custom_answer_placeholder)
                                        } else if (isToolApprovalActive) {
                                            stringResource(R.string.tool_approval_input_placeholder)
                                        } else {
                                            stringResource(R.string.minimal_chat_input_placeholder, assistant.name)
                                        }
                                    )
                                },
                                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 5),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    top = 16.dp,
                                    end = 16.dp,
                                    bottom = 80.dp, // Leave space for send button
                                ),
                                colors = TextFieldDefaults.colors().copy(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )

                            // Floating send button at the bottom right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                AttachmentImportAction(
                                    isImporting = isImportingAttachments,
                                    modifier = Modifier.size(AppSize.ChromePill),
                                ) {
                                    val currentAction = when {
                                    isQuestionnaireActive && isFinalQuestion ->
                                        LastChatComposerAction.QuestionnaireSubmit
                                    isQuestionnaireActive -> LastChatComposerAction.QuestionnaireNext
                                    isToolApprovalActive -> LastChatComposerAction.ToolApprovalDeny
                                    state.loading -> LastChatComposerAction.Loading
                                    !state.isEmpty() -> LastChatComposerAction.Send
                                    hasSelectedSttProvider && sttRecording ->
                                        LastChatComposerAction.SttRecording
                                    hasSelectedSttProvider && sttFinalizing ->
                                        LastChatComposerAction.SttFinalizing
                                    hasSelectedSttProvider && settings.displaySetting.sttReplaceModelIcon ->
                                        LastChatComposerAction.Stt
                                    else -> LastChatComposerAction.Picker
                                }
                                LastChatComposerActionButton(
                                    action = currentAction,
                                    onClick = {
                                        when (currentAction) {
                                            LastChatComposerAction.Send,
                                            LastChatComposerAction.Loading,
                                            LastChatComposerAction.ToolApprovalDeny,
                                            LastChatComposerAction.QuestionnaireNext,
                                            LastChatComposerAction.QuestionnaireSubmit -> {
                                                sendMessage()
                                                isExpandedFullScreen = false
                                            }
                                            LastChatComposerAction.Stt -> {
                                                haptics.perform(HapticPattern.Pop)
                                                isExpandedFullScreen = false
                                                startSttRecording()
                                            }
                                            LastChatComposerAction.SttRecording -> {
                                                haptics.perform(HapticPattern.Pop)
                                                stopSttRecording(accept = true)
                                            }
                                            LastChatComposerAction.Picker,
                                            LastChatComposerAction.SttFinalizing -> {
                                                isExpandedFullScreen = false
                                                plusMenu.open()
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(AppSize.ChromePill),
                                    containerColorOverride = if (currentAction == LastChatComposerAction.Picker) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        null
                                    },
                                ) { action ->
                                    when (action) {
                                        LastChatComposerAction.Loading -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Stop,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        LastChatComposerAction.Send,
                                        LastChatComposerAction.QuestionnaireSubmit -> {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        LastChatComposerAction.QuestionnaireNext -> {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        LastChatComposerAction.ToolApprovalDeny -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        LastChatComposerAction.Stt,
                                        LastChatComposerAction.SttRecording -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Mic,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (action == LastChatComposerAction.SttRecording) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            )
                                        }
                                        LastChatComposerAction.SttFinalizing -> Unit
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Plus menu sheet: replaces every old + menu. Hosted last so it draws
    // above the composer. All rows wire into the same handlers the old
    // picker content used; sub-sheets below stay as the detail panes.
    fun effortToBudget(level: EffortLevel): Int = when (level) {
        EffortLevel.Low -> 1024
        EffortLevel.Medium -> 16000
        EffortLevel.High -> 32000
        EffortLevel.Extra -> 48000
        EffortLevel.Max -> 64000
    }

    fun budgetToEffort(budget: Int): EffortLevel = when {
        budget <= 1024 -> EffortLevel.Low
        budget <= 16000 -> EffortLevel.Medium
        budget <= 32000 -> EffortLevel.High
        budget <= 48000 -> EffortLevel.Extra
        else -> EffortLevel.Max
    }

    // Keep menu selection state in sync with the assistant/settings source of truth.
    LaunchedEffect(assistant.thinkingBudget) {
        val budget = assistant.thinkingBudget
        if (budget == null || budget < 0) {
            plusMenu.thinking = true
        } else if (budget == 0) {
            plusMenu.thinking = false
        } else {
            plusMenu.thinking = true
            plusMenu.effort = budgetToEffort(budget)
        }
    }
    LaunchedEffect(assistant.enableAutomaticSkillInvocation) {
        plusMenu.skillMode =
            if (assistant.enableAutomaticSkillInvocation) SkillMode.Auto else SkillMode.Manual
    }
    LaunchedEffect(enableSearch) {
        plusMenu.webSearchMode =
            if (enableSearch) WebSearchMode.SmartOnline else WebSearchMode.Offline
    }
    // LobeHub parity: Agent is the default; only an explicit false (persisted
    // on the assistant) collapses to Chat.
    LaunchedEffect(assistant.agentMode) {
        plusMenu.agentMode =
            if (assistant.agentMode) AgentMode.Agent else AgentMode.Chat
    }

    fun toggleSkillSelection(skillId: String) {
        val parsed = runCatching { Uuid.parse(skillId) }.getOrNull() ?: return
        val base = if (conversation.enabledModeIds.hasManualSkillSelectionOverride() || conversation.enabledModeIds.isNotEmpty()) {
            conversation.enabledModeIds.withoutSkillSelectionOverride()
        } else {
            assistantDefaultSkillIds + alwaysEnabledSkillIds
        }.intersect(availableSkillIds)
        val newIds = if (base.contains(parsed)) base - parsed else base + parsed
        onUpdateConversation(conversation.copy(enabledModeIds = newIds + SKILL_SELECTION_OVERRIDE_ID))
    }

    fun applyApprovalMode(mode: ApprovalMode) {
        // Real effect: workspace per-tool approval overrides. Without a
        // workspace there is nothing to approve, so this is a no-op.
        val workspaceId = assistant.workspaceId?.toString() ?: return
        scope.launch {
            val tools = listOf(
                "workspace_read_file",
                "workspace_write_file",
                "workspace_edit_file",
                "workspace_shell",
            )
            when (mode) {
                ApprovalMode.Auto -> tools.forEach {
                    workspaceRepository.setToolApproval(workspaceId, it, needsApproval = false)
                }
                ApprovalMode.Manual -> tools.forEach {
                    workspaceRepository.setToolApproval(workspaceId, it, needsApproval = true)
                }
                ApprovalMode.AllowList -> Unit // keep previously remembered per-tool choices
            }
        }
    }

    val plusMenuSkills = remember(settings.skills, assistant.id) {
        settings.skills
            .filter { it.userInvocable && it.isAvailableForAssistant(assistant.id) }
            .map { skill ->
                val description = when {
                    skill.description.isNotBlank() -> skill.description
                    skill.instructions.isNotBlank() -> skill.instructions.take(50) +
                        if (skill.instructions.length > 50) "..." else ""
                    else -> ""
                }
                PlusMenuSkill(
                    id = skill.id.toString(),
                    name = skill.name.ifEmpty { "Unnamed skill" },
                    description = description,
                    trailing = SkillTrailing.Pinned,
                    toast = skill.name.ifEmpty { "Skill" },
                )
            }
    }
    val searchService = settings.searchServices.getOrNull(effectiveProviderIndex)
    val searchProviderName = searchService?.let { SearchServiceOptions.TYPES[it::class] }
    val lorebookIds = remember(settings.lorebooks) { settings.lorebooks.map { it.id }.toSet() }
    val lorebooksSubtitle: String? = if (settings.lorebooks.isNotEmpty()) {
        val active = (conversation.enabledLorebookIds ?: assistant.enabledLorebookIds).intersect(lorebookIds).size
        if (active > 0) "$active active" else stringResource(R.string.minimal_input_lorebooks_desc)
    } else {
        null
    }
    val pluginsSubtitle: String? = if (settings.mcpServers.isNotEmpty()) {
        val available = settings.mcpServers.filter { it.commonOptions.enable }.map { it.id }.toSet()
        val active = assistant.mcpServers.intersect(available).size
        if (active > 0) {
            stringResource(R.string.plugins_picker_active_count, active)
        } else {
            stringResource(R.string.minimal_input_plugins_desc)
        }
    } else {
        null
    }
    val summarizeSubtitle: String? = if (assistant.canManuallySummarizeConversation(conversation.currentMessages.size)) {
        stringResource(R.string.minimal_input_summarize_desc)
    } else {
        null
    }

    // Plus menu dialog: its own full-screen window so sheet metrics track the
    // screen in every host (chat page, overlay) without disturbing the
    // wrap-content composer root. Gated on isVisible, which open() sets
    // synchronously and close animations clear at 0, so open/close motion
    // stays composed. Dim is off (the sheet draws its own scrim); outside
    // taps and back follow the same pane-aware rule as the sheet's BackHandler.
    if (plusMenu.isVisible) {
        Dialog(
            onDismissRequest = {
                if (plusMenu.pane != PlusMenuPane.Main) {
                    plusMenu.showPane(PlusMenuPane.Main)
                } else {
                    plusMenu.close()
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            val plusMenuDialogView = LocalView.current
            SideEffect {
                val dialogWindow = findDialogWindow(plusMenuDialogView)
                dialogWindow?.let { window ->
                    window.setDimAmount(0f)
                    @Suppress("DEPRECATION")
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    @Suppress("DEPRECATION")
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                }
            }
    PlusMenu(
        state = plusMenu,
        modelName = currentChatModel?.displayName,
        agentMode = if (assistant.agentMode) AgentMode.Agent else AgentMode.Chat,
        pinnedSkills = plusMenuSkills,
        lorebooksSubtitle = lorebooksSubtitle,
        pluginsSubtitle = pluginsSubtitle,
        summarizeSubtitle = summarizeSubtitle,
        searchProviderName = searchProviderName,
        actions = PlusMenuActions(
            onUploadFile = { filePickerLauncher.launch("*/*") },
            onTakePhoto = { launchCamera() },
            onSelectFromGallery = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            onOpenProject = { onNavigateToWorkspace(assistant.workspaceId?.toString()) },
            onModelClick = { showModelPicker = true },
            onSkillClick = { skill -> toggleSkillSelection(skill.id) },
            onAddSkills = { showSkillsPicker = true },
            onSkillModeChange = { mode ->
                onUpdateAssistant(assistant.copy(enableAutomaticSkillInvocation = mode == SkillMode.Auto))
            },
            onWebSearchModeChange = { mode ->
                onToggleSearch(mode == WebSearchMode.SmartOnline)
            },
            onOpenSearchSettings = { showSearchPicker = true },
            onEffortChange = { level ->
                onUpdateAssistant(assistant.copy(thinkingBudget = effortToBudget(level)))
            },
            onAgentModeChange = { mode ->
                // LobeHub useToggleAgentMode parity: persist the mode on the
                // assistant; saved tool/skill config is never mutated — chat
                // mode is enforced at generation time. Flows through
                // onUpdateAssistant so chat + overlay share the same path.
                onUpdateAssistant(assistant.copy(agentMode = mode == AgentMode.Agent))
            },
            onApprovalModeChange = { mode -> applyApprovalMode(mode) },
            onThinkingChange = { enabled ->
                onUpdateAssistant(
                    assistant.copy(
                        thinkingBudget = if (enabled) effortToBudget(plusMenu.effort) else 0
                    )
                )
            },
            onOpenLorebooks = { showLorebooksPicker = true },
            onOpenPlugins = { showPluginsPicker = true },
            onSummarize = { showContextRefreshDialog = true },
        ),
    )
        }
    }

    // Model picker sheet - direct ModalBottomSheet (not ModelSelector which shows its own button)
    if (showModelPicker) {
        val modelPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val filteredProviders = settings.providers.filter {
            it.enabled && it.models.any { model -> model.type == me.rerere.ai.provider.ModelType.CHAT }
        }

        ModalBottomSheet(
containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
            onDismissRequest = { showModelPicker = false },
            sheetState = modelPickerSheetState,
            sheetGesturesEnabled = false,
            dragHandle = {
                IconButton(
                    onClick = {
                        scope.launch {
                            modelPickerSheetState.hide()
                            showModelPicker = false
                        }
                    }
                ) {
                    Icon(Icons.Rounded.KeyboardArrowDown, null)
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModelList(
                    currentModel = assistant.chatModelId ?: settings.chatModelId,
                    providers = filteredProviders,
                    modelType = me.rerere.ai.provider.ModelType.CHAT,
                    onSelect = { selectedModel: Model ->
                        onUpdateChatModel(selectedModel)
                        scope.launch {
                            modelPickerSheetState.hide()
                            showModelPicker = false
                        }
                    },
                    onDismiss = {
                        scope.launch {
                            modelPickerSheetState.hide()
                            showModelPicker = false
                        }
                    }
                )
            }
        }
    }

    // Skills picker sheet
    if (showSkillsPicker) {
        SkillsPickerSheet(
            settings = settings,
            assistant = assistant,
            conversation = conversation,
            onUpdateConversation = onUpdateConversation,
            onDismiss = { showSkillsPicker = false }
        )
    }

    // Lorebooks picker sheet
    if (showLorebooksPicker) {
        LorebooksPickerSheet(
            settings = settings,
            assistant = assistant,
            conversation = conversation,
            onUpdateConversation = onUpdateConversation,
            onNavigateToLorebook = { lorebookId ->
                showLorebooksPicker = false
                onNavigateToLorebook(lorebookId)
            },
            onDismiss = { showLorebooksPicker = false }
        )
    }

    if (showPluginsPicker) {
        PluginsPickerSheet(
            settings = settings,
            assistant = assistant,
            onUpdateAssistant = onUpdateAssistant,
            onDismiss = { showPluginsPicker = false },
        )
    }

    // Context Refresh dialog (same as floating toolbar)
    if (showContextRefreshDialog) {
        ContextRefreshDialog(
            conversation = conversation,
            onRefresh = onRefreshContext,
            onEditSummary = {
                editableContextSummary = conversation.contextSummary.orEmpty()
                showContextSummaryEditDialog = true
            },
            onRevertSummary = {
                onUpdateConversation(
                    conversation.copy(
                        contextSummary = null,
                        contextSummaryUpToIndex = -1,
                        lastRefreshTime = 0L,
                        updateAt = Instant.now()
                    )
                )
            },
            onDismiss = { showContextRefreshDialog = false }
        )
    }

    if (showContextSummaryEditDialog) {
        AlertDialog(
            onDismissRequest = { showContextSummaryEditDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            title = {
                Text(
                    text = stringResource(R.string.context_refresh_edit_summary),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                TextField(
                    value = editableContextSummary,
                    onValueChange = { editableContextSummary = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                    placeholder = {
                        Text(stringResource(R.string.context_refresh_edit_summary_placeholder))
                    }
                )
            },
            confirmButton = {
                IconButton(
                    onClick = {
                        haptics.perform(HapticPattern.Success)
                        val trimmedSummary = editableContextSummary.trim()
                        onUpdateConversation(
                            conversation.copy(
                                contextSummary = trimmedSummary.takeIf { it.isNotBlank() },
                                contextSummaryUpToIndex = if (trimmedSummary.isBlank()) {
                                    -1
                                } else {
                                    conversation.contextSummaryUpToIndex
                                },
                                lastRefreshTime = if (trimmedSummary.isBlank()) 0L else System.currentTimeMillis(),
                                updateAt = Instant.now()
                            )
                        )
                        showContextSummaryEditDialog = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = stringResource(R.string.context_refresh_save_summary)
                    )
                }
            },
            dismissButton = {
                IconButton(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        showContextSummaryEditDialog = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(android.R.string.cancel)
                    )
                }
            }
        )
    }

    // Search picker sheet (same as floating toolbar) - direct content, no intermediate button
    if (showSearchPicker) {
        val chatModel = currentChatModel

        ModalBottomSheet(
containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
            onDismissRequest = { showSearchPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.search_picker_title),
                    style = MaterialTheme.typography.titleLarge
                )

                // Direct SearchPicker content
                SearchPicker(
                    enableSearch = enableSearch,
                    settings = settings,
                    model = chatModel,
                    onToggleSearch = { enabled ->
                        if (enabled) {
                            // When turning on, restore the last known valid provider index
                            onUpdateSearchService(effectiveProviderIndex)
                        }
                        onToggleSearch(enabled)
                    },
                    onUpdateSearchService = { index ->
                        // Track this selection
                        lastValidProviderIndex = index
                        onUpdateSearchService(index)
                    },
                    selectedProviderIndex = effectiveProviderIndex,  // Use effective index so selection persists when off
                    preferBuiltInSearch = assistant.preferBuiltInSearch,
                    onTogglePreferBuiltInSearch = { enabled ->
                        onUpdateAssistant(assistant.copy(preferBuiltInSearch = enabled))
                    },
                    onDismiss = { showSearchPicker = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AttachmentImportAction(
    isImporting: Boolean,
    modifier: Modifier = Modifier.size(36.dp),
    idleContent: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = isImporting,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(150)) + scaleIn(tween(250), initialScale = 0.6f)) togetherWith
                (fadeOut(tween(150)) + scaleOut(tween(250), targetScale = 0.6f))
        },
        contentAlignment = Alignment.Center,
        label = "AttachmentImportAction",
    ) { importing ->
        if (importing) {
            // Centered at icon scale: fillMaxSize would blow the spinner up to
            // the full 37-48dp action slot instead of matching the 18-24dp icons.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ContainedLoadingIndicator(modifier = Modifier.size(20.dp))
            }
        } else {
            idleContent()
        }
    }
}

@Composable
private fun WorkspaceRequiredCard(
    fileName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = AppShapes.CardMedium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.workspace_required_card_title, fileName),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.workspace_required_card_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CharacterQuestionsCard(
    questionnaire: AskUserQuestionnaire,
    currentIndex: Int,
    selectedOptionLabel: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    onSelectOption: (AskUserOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = questionnaire.questions.getOrNull(currentIndex) ?: return
    val canGoPrevious = currentIndex > 0
    val canGoNext = currentIndex < questionnaire.questions.lastIndex

    Surface(
        shape = AppShapes.CardMedium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.previous),
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.character_questions_progress,
                            currentIndex + 1,
                            questionnaire.questions.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onNext,
                        enabled = canGoNext,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.next),
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.banner_dismiss),
                    )
                }
            }

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium
            )

            question.options.forEach { option ->
                CharacterQuestionOptionRow(
                    option = option,
                    selected = selectedOptionLabel == option.label,
                    onClick = { onSelectOption(option) }
                )
            }
        }
    }
}

@Composable
private fun CharacterQuestionOptionRow(
    option: AskUserOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "question_option_scale"
    )

    Surface(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            option.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class PendingToolApproval(
    val toolCallId: String,
    val toolName: String,
    val arguments: String,
) {
    val isWorkspaceTool: Boolean
        get() = toolName.startsWith("workspace_")
}

private fun List<me.rerere.ai.ui.UIMessage>.findPendingToolApproval(): PendingToolApproval? {
    return firstNotNullOfOrNull { message ->
        message.getToolCalls().firstOrNull { toolCall ->
            toolCall.toolName != ASK_USER_TOOL_NAME &&
                toolCall.approvalState is ToolApprovalState.Pending
        }?.let { toolCall ->
            PendingToolApproval(
                toolCallId = toolCall.toolCallId,
                toolName = toolCall.toolName,
                arguments = toolCall.arguments,
            )
        }
    }
}

@Composable
private fun ToolApprovalCard(
    approval: PendingToolApproval,
    onApprove: () -> Unit,
    onAlwaysApproveWorkspace: () -> Unit,
    onDenyWithInstruction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = AppShapes.CardMedium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tool_approval_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = approval.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val summary = approval.summary()
            if (summary.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            ToolApprovalActionRow(
                label = stringResource(R.string.tool_approval_approve),
                description = stringResource(R.string.tool_approval_approve_desc),
                icon = Icons.Rounded.Check,
                selected = true,
                onClick = onApprove,
            )
            if (approval.isWorkspaceTool) {
                ToolApprovalActionRow(
                    label = stringResource(R.string.tool_approval_always_workspace),
                    description = stringResource(R.string.tool_approval_always_workspace_desc),
                    icon = Icons.Rounded.Save,
                    selected = false,
                    onClick = onAlwaysApproveWorkspace,
                )
            }
            ToolApprovalActionRow(
                label = stringResource(R.string.tool_approval_deny_with_instruction),
                description = stringResource(R.string.tool_approval_deny_with_instruction_desc),
                icon = Icons.Rounded.Edit,
                selected = false,
                onClick = onDenyWithInstruction,
            )
        }
    }
}

@Composable
private fun ToolApprovalActionRow(
    label: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "tool_approval_action_scale"
    )

    Surface(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PendingToolApproval.displayName(): String = when (toolName) {
    "workspace_shell" -> stringResource(R.string.activity_timeline_tool_workspace_shell)
    "workspace_read_file" -> stringResource(R.string.activity_timeline_tool_workspace_read_file)
    "workspace_write_file" -> stringResource(R.string.activity_timeline_tool_workspace_write_file)
    "workspace_edit_file" -> stringResource(R.string.activity_timeline_tool_workspace_edit_file)
    else -> toolName.replace("_", " ").replaceFirstChar { it.uppercase() }
}

private fun PendingToolApproval.summary(): String {
    val args = runCatching { JsonInstantPretty.parseToJsonElement(arguments).jsonObject }.getOrNull()
    val key = when (toolName) {
        "workspace_shell" -> "command"
        "workspace_read_file", "workspace_write_file", "workspace_edit_file" -> "path"
        else -> null
    }
    return key?.let { args?.get(it)?.jsonPrimitiveOrNull?.contentOrNull }
        ?: arguments.take(240)
}

@Composable
private fun STTWaveformLine(
    amplitudes: List<Float>,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val count = 21
    val center = count / 2
    val historyNeeded = center + 1

    val bars = remember(amplitudes, active) {
        val source = amplitudes.takeLast(historyNeeded).reversed()
        List(count) { index ->
            val distFromCenter = kotlin.math.abs(index - center)
            if (source.isEmpty()) {
                if (active) 0.15f + ((distFromCenter % 3) * 0.05f) else 0.08f
            } else {
                val rawValue = source.getOrNull(distFromCenter) ?: 0.08f
                rawValue
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        bars.forEachIndexed { index, value ->
            val animatedHeight by animateFloatAsState(
                targetValue = value.coerceIn(0.08f, 1f),
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                label = "stt_waveform_$index",
            )
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = (4.dp + 20.dp * animatedHeight))
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (active) 0.85f else 0.45f)),
            )
        }
    }
}

@Composable
private fun MinimalFileButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Compact file button for use inside grouped container (24dp inner radius for optical roundness)
@Composable
private fun MinimalFileButtonCompact(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.CardMedium,  // Optically round with 40dp outer container
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.height(72.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Grouped file button with custom shape for grouped appearance (same as floating toolbar)
@Composable
private fun MinimalFileButtonGrouped(
    icon: ImageVector,
    label: String,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // OLED-aware button color (same as floating toolbar's BigIconTextButton)
    val amoledMode by me.rerere.rikkahub.ui.hooks.rememberAmoledDarkMode()
    val isDarkMode = me.rerere.rikkahub.ui.theme.LocalDarkMode.current
    val isAmoled = amoledMode && isDarkMode
    val buttonColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Surface(
        onClick = onClick,
        shape = shape,
        color = buttonColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class PendingImageCrop(
    val instanceId: String,
    val image: UIMessagePart.Image,
)

private data class IndexedAttachment<T : UIMessagePart>(
    val id: String,
    val part: T,
)

private fun UIMessagePart.attachmentUrl(): String? = when (this) {
    is UIMessagePart.Image -> url
    is UIMessagePart.Video -> url
    is UIMessagePart.Audio -> url
    is UIMessagePart.Document -> url
    else -> null
}

@Composable
private fun MediaFileInputRow(
    state: ChatInputState,
    onDelete: (Uri) -> Unit,
    onCropImage: (String, UIMessagePart.Image) -> Unit,
) {
    val images = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Image)?.let { image ->
                IndexedAttachment(attachment.id, image)
            }
        }
    }
    val videos = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Video)?.let { video ->
                IndexedAttachment(attachment.id, video)
            }
        }
    }
    val audios = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Audio)?.let { audio ->
                IndexedAttachment(attachment.id, audio)
            }
        }
    }
    val documents = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Document)?.let { document ->
                IndexedAttachment(attachment.id, document)
            }
        }
    }

    fun removePart(instanceId: String): Uri? {
        val removedPart = state.removeAttachment(instanceId) ?: return null
        val removedUrl = removedPart.attachmentUrl() ?: return null
        return if (state.messageContent.none { it.attachmentUrl() == removedUrl }) {
            removedUrl.toUri()
        } else {
            null
        }
    }

    LastChatComposerAttachmentRow {
        items(
            items = images,
            key = { attachment -> "image:${attachment.id}" }
        ) { attachment ->
            val image = attachment.part
            LastChatComposerImageAttachment(
                onClick = { onCropImage(attachment.id, image) },
                onRemove = { removePart(attachment.id)?.let(onDelete) },
                removeContentDescription = stringResource(R.string.delete),
            ) {
                AsyncImage(
                    model = image.url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        items(
            items = videos,
            key = { attachment -> "video:${attachment.id}" }
        ) { attachment ->
            LastChatComposerMediaAttachment(
                onRemove = { removePart(attachment.id)?.let(onDelete) },
            ) {
                LastChatComposerVideoIcon()
            }
        }
        items(
            items = audios,
            key = { attachment -> "audio:${attachment.id}" }
        ) { attachment ->
            LastChatComposerMediaAttachment(
                onRemove = { removePart(attachment.id)?.let(onDelete) },
            ) {
                LastChatComposerAudioIcon()
            }
        }
        items(
            items = documents,
            key = { attachment -> "document:${attachment.id}" }
        ) { attachment ->
            val document = attachment.part
            LastChatDocumentAttachmentTile(
                fileName = document.fileName,
                modifier = Modifier.size(60.dp),
                onRemove = {
                    removePart(attachment.id)?.let(onDelete)
                }
            )
        }
    }
}

@Composable
private fun ChatScrollToBottomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "scroll_to_bottom_scale"
    )

    Surface(
        shape = CircleShape,
        color = blurredContainerColor(MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .lastChatBlurEffect(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onClick()
                },
            )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.chat_page_scroll_to_bottom),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChatSuggestionsRow(
    modifier: Modifier = Modifier,
    suggestions: List<String>,
    onClickSuggestion: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var pressedSuggestionIndex by remember { mutableStateOf<Int?>(null) }
    var selectedSuggestionIndex by remember { mutableStateOf<Int?>(null) }

    val canScrollLeft by remember { androidx.compose.runtime.derivedStateOf { scrollState.value > 0 } }
    val canScrollRight by remember { androidx.compose.runtime.derivedStateOf { scrollState.value < scrollState.maxValue } }
    val leftFadeAlpha by animateFloatAsState(
        targetValue = if (canScrollLeft) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "left_fade"
    )
    val rightFadeAlpha by animateFloatAsState(
        targetValue = if (canScrollRight) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "right_fade"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                if (leftFadeAlpha > 0f || rightFadeAlpha > 0f) {
                    val fadeWidthPx = 24.dp.toPx()
                    val leftEnd = (fadeWidthPx / size.width).coerceAtMost(0.4f)
                    val rightStart = (1f - fadeWidthPx / size.width).coerceAtLeast(0.6f)
                    val colorStops = arrayOf(
                        0f to Color.Black.copy(alpha = 1f - leftFadeAlpha),
                        leftEnd to Color.Black,
                        rightStart to Color.Black,
                        1f to Color.Black.copy(alpha = 1f - rightFadeAlpha)
                    )
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(colorStops = colorStops),
                        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
        suggestions.forEachIndexed { index, suggestion ->
            var visible by remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            val isInteractionPressed by interactionSource.collectIsPressedAsState()

            LaunchedEffect(isInteractionPressed) {
                if (isInteractionPressed) {
                    pressedSuggestionIndex = index
                } else if (pressedSuggestionIndex == index) {
                    pressedSuggestionIndex = null
                }
            }

            LaunchedEffect(suggestion) {
                kotlinx.coroutines.delay(index * 50L)
                visible = true
            }

            val isSelected = selectedSuggestionIndex == index
            val isPressed = pressedSuggestionIndex == index
            val isAnythingSelected = selectedSuggestionIndex != null
            val isAnythingPressed = pressedSuggestionIndex != null

            val targetScale = when {
                isSelected -> 1.05f
                isPressed -> 0.9f
                else -> 1f
            }

            val targetAlpha = when {
                isSelected -> 0f
                isAnythingSelected -> 0f
                isAnythingPressed && !isPressed -> 0.5f
                visible -> 1f
                else -> 0f
            }

            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                label = "suggestion_scale"
            )

            val alpha by animateFloatAsState(
                targetValue = targetAlpha,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "suggestion_alpha"
            )

            LaunchedEffect(isSelected) {
                if (isSelected) {
                    kotlinx.coroutines.delay(200)
                    onClickSuggestion(suggestion)
                }
            }

            if (visible || targetAlpha > 0f) {
                val suggestionShape = RoundedCornerShape(16.dp)
                Surface(
                    shape = suggestionShape,
                    color = blurredContainerColor(MaterialTheme.colorScheme.surfaceContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .lastChatBlurEffect(MaterialTheme.colorScheme.surfaceContainer, suggestionShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                                selectedSuggestionIndex = index
                        }
                ) {
                    Text(
                        text = suggestion,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ExpandButtonOverlay(
    isVisible: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)),
        exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)),
        modifier = modifier
    ) {
        androidx.compose.material3.IconButton(
            onClick = onExpand,
            modifier = Modifier.size(36.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.Fullscreen,
                contentDescription = "Expand",
                modifier = Modifier.size(20.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun findDialogWindow(view: View): Window? {
    var current: Any? = view
    while (current != null) {
        if (current is DialogWindowProvider) {
            return current.window
        }
        current = (current as? View)?.parent
    }
    return null
}
