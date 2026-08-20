package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PersonaEntity
import com.example.ui.components.ChatBubble
import com.example.ui.components.ChatDrawerContent
import com.example.ui.components.PersonaSelector
import com.example.ui.components.TypingIndicatorBubble
import kotlinx.coroutines.launch
import java.util.Locale

private val EditSquareVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "EditSquare",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        // Rounded box outline
        moveTo(12f, 3f)
        horizontalLineTo(7f)
        curveTo(4.79f, 3f, 3f, 4.79f, 3f, 7f)
        verticalLineTo(17f)
        curveTo(3f, 19.21f, 4.79f, 21f, 7f, 21f)
        horizontalLineTo(17f)
        curveTo(19.21f, 21f, 21f, 19.21f, 21f, 17f)
        verticalLineTo(12f)
        horizontalLineTo(19f)
        verticalLineTo(17f)
        curveTo(19f, 18.1f, 18.1f, 19f, 17f, 19f)
        horizontalLineTo(7f)
        curveTo(5.9f, 19f, 5f, 18.1f, 5f, 17f)
        verticalLineTo(7f)
        curveTo(5f, 5.9f, 5.9f, 5f, 7f, 5f)
        horizontalLineTo(12f)
        verticalLineTo(3f)
        close()

        // Diagonal pencil inside box
        moveTo(20.71f, 5.04f)
        curveTo(21.1f, 4.65f, 21.1f, 4.02f, 20.71f, 3.63f)
        lineTo(19.37f, 2.29f)
        curveTo(18.98f, 1.9f, 18.35f, 1.9f, 17.96f, 2.29f)
        lineTo(9f, 11.25f)
        verticalLineTo(15f)
        horizontalLineTo(12.75f)
        lineTo(20.71f, 5.04f)
        close()
        moveTo(10.5f, 13.5f)
        horizontalLineTo(10.5f)
        lineTo(17.25f, 6.75f)
        lineTo(17.25f, 6.75f)
        close()
    }.build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val activePersonas by viewModel.activePersonas.collectAsStateWithLifecycle()
    val allPersonas by viewModel.allPersonas.collectAsStateWithLifecycle()
    val sessions by viewModel.filteredSessions.collectAsStateWithLifecycle()
    val selectedSessionId by viewModel.selectedSessionId.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val selectedPersona by viewModel.selectedPersona.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val isProfileSettingsOpen by viewModel.isProfileSettingsOpen.collectAsStateWithLifecycle()
    val isCustomPageOpen by viewModel.isCustomPageOpen.collectAsStateWithLifecycle()
    val selectedAccentColorIndex by viewModel.selectedAccentColorIndex.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val editingPersona by viewModel.editingPersona.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val userNotice by viewModel.userNotice.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputText by remember { mutableStateOf("") }
    var showTopMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Handle User Notices in Snackbar
    LaunchedEffect(userNotice) {
        userNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotice()
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Voice Speech Recognizer Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

    if (isAdminMode) {
        AdminSettingsScreen(
            allPersonas = allPersonas,
            editingPersona = editingPersona,
            onOpenEditor = viewModel::openPersonaEditor,
            onCloseEditor = viewModel::closePersonaEditor,
            onSavePersona = viewModel::savePersona,
            onDeletePersona = viewModel::deletePersona,
            onCloseAdmin = viewModel::toggleAdminMode
        )
    } else if (isProfileSettingsOpen) {
        ProfileSettingsScreen(
            onCloseProfile = viewModel::closeProfileSettings,
            onOpenAdmin = {
                viewModel.closeProfileSettings()
                viewModel.toggleAdminMode()
            },
            selectedAccentColorIndex = selectedAccentColorIndex,
            onSelectAccentColor = viewModel::selectAccentColor,
            themeMode = themeMode,
            onSetThemeMode = viewModel::setThemeMode,
            isDarkMode = isDarkMode,
            onToggleDarkMode = viewModel::toggleDarkMode,
            onSubmitReport = viewModel::submitReport,
            onOpenCustomPage = {
                viewModel.closeProfileSettings()
                viewModel.openCustomPage()
            }
        )
    } else if (isCustomPageOpen) {
        CustomBlankPageScreen(
            onClose = viewModel::closeCustomPage
        )
    } else {
        val isDrawerOpen = drawerState.isOpen || drawerState.isAnimationRunning

        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = Color.Black.copy(alpha = 0.40f),
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    drawerContentColor = MaterialTheme.colorScheme.onSurface,
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.width(310.dp)
                ) {
                    ChatDrawerContent(
                        sessions = sessions,
                        selectedSessionId = selectedSessionId,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onNewChatClick = {
                            viewModel.createNewSession()
                            scope.launch { drawerState.close() }
                        },
                        onSelectSession = { id ->
                            viewModel.selectSession(id)
                            scope.launch { drawerState.close() }
                        },
                        onDeleteSession = viewModel::deleteSession,
                        onTogglePinSession = viewModel::togglePinSession,
                        onRenameSession = viewModel::renameSession,
                        onOpenProfileSettings = {
                            viewModel.openProfileSettings()
                            scope.launch { drawerState.close() }
                        },
                        onClearAllHistory = viewModel::clearAllHistory,
                        onOpenCustomPage = {
                            viewModel.openCustomPage()
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isDrawerOpen) {
                            Modifier.blur(10.dp)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = selectedPersona?.displayName ?: "OmniChat",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        scope.launch { drawerState.open() }
                                    },
                                    modifier = Modifier.testTag("drawer_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Chat History Drawer",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        actions = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    // 1. ChatGPT style New Chat icon
                                    IconButton(
                                        onClick = {
                                            focusManager.clearFocus()
                                            viewModel.createNewSession()
                                        },
                                        modifier = Modifier.testTag("new_chat_button")
                                    ) {
                                        Icon(
                                            imageVector = EditSquareVector,
                                            contentDescription = "New Chat",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // 2. Model selector icon (compact size matching new chat icon)
                                    PersonaSelector(
                                        selectedPersona = selectedPersona,
                                        activePersonas = activePersonas,
                                        onSelectPersona = viewModel::selectPersona,
                                        onOpenAdmin = viewModel::toggleAdminMode,
                                        isCompact = true
                                    )

                                    // 3. Three vertical dots icon
                                    Box {
                                        IconButton(
                                            onClick = {
                                                focusManager.clearFocus()
                                                showTopMenu = true
                                            },
                                            modifier = Modifier.testTag("top_more_options")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "More Options",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showTopMenu,
                                            onDismissRequest = { showTopMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("New Chat") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    showTopMenu = false
                                                    viewModel.createNewSession()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Chat History") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Menu,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    showTopMenu = false
                                                    scope.launch { drawerState.open() }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Download Context") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Download,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    showTopMenu = false
                                                    viewModel.downloadChatContext(context)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Settings & Personas") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Settings,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    showTopMenu = false
                                                    viewModel.toggleAdminMode()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                modifier = modifier.fillMaxSize()
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                ) {
                    // Chat Messages Body or Empty State
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    focusManager.clearFocus()
                                })
                            }
                    ) {
                        if (messages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = innerPadding.calculateTopPadding())
                            ) {
                                EmptyChatState(
                                    selectedPersona = selectedPersona,
                                    onPromptSelected = { prompt ->
                                        inputText = prompt
                                        viewModel.sendMessage(prompt)
                                        inputText = ""
                                    }
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = innerPadding.calculateTopPadding() + 8.dp,
                                    bottom = 16.dp,
                                    start = 16.dp,
                                    end = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(messages, key = { it.id }) { msg ->
                                    ChatBubble(
                                        message = msg,
                                        personaDisplayName = selectedPersona?.displayName ?: "AI Assistant",
                                        onCopyText = { text ->
                                            viewModel.copyToClipboard(context, text)
                                        }
                                    )
                                }

                                if (isGenerating) {
                                    item {
                                        TypingIndicatorBubble(
                                            personaDisplayName = selectedPersona?.displayName ?: "Assistant"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Input Bar
                    ChatInputBar(
                        inputText = inputText,
                        onInputTextChange = { inputText = it },
                        isGenerating = isGenerating,
                        onSendClick = { attachment ->
                            val fullPrompt = if (attachment != null) {
                                if (inputText.isBlank()) "[Attached: $attachment]" else "$inputText [Attached: $attachment]"
                            } else {
                                inputText
                            }
                            if (fullPrompt.isNotBlank()) {
                                viewModel.sendMessage(fullPrompt)
                                inputText = ""
                            }
                        },
                        onVoiceInputClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your message...")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (_: Exception) {}
                        }
                    )
                }
            }
        }
    }
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyChatState(
    selectedPersona: PersonaEntity?,
    onPromptSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hello! I am ${selectedPersona?.displayName ?: "Nexus AI"}",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Ask me anything. Powered by custom AI persona models.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isGenerating: Boolean,
    onSendClick: (String?) -> Unit,
    onVoiceInputClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var showPlusMenu by remember { mutableStateOf(false) }
    var attachedFile by remember { mutableStateOf<String?>(null) }
    var previewFile by remember { mutableStateOf<String?>(null) }

    // Attachment Full View Dialog Modal
    if (previewFile != null) {
        Dialog(onDismissRequest = { previewFile = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attachment Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { previewFile = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close preview",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = previewFile ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previewFile = null }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Main Input Container Card
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(
                width = 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Image/File Attachment Preview Thumbnail inside Container
                if (attachedFile != null) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .size(width = 110.dp, height = 100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f))
                            .clickable { previewFile = attachedFile }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = attachedFile ?: "Photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Close (X) button on top-right of image thumbnail
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .clickable { attachedFile = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove attachment",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Middle Multiline Text Field with "Ask anything..." placeholder
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        }
                        .testTag("chat_input_field"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    maxLines = 5,
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty() && attachedFile == null) {
                            Text(
                                text = "Ask anything...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        } else if (inputText.isEmpty() && attachedFile != null) {
                            Text(
                                text = "Ask about this photo...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom straight horizontal row: Plus (+) Icon on Left, Mic & Single Action/Send Button on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Far Left: Plus (+) Icon Button
                    Box {
                        IconButton(
                            onClick = { showPlusMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showPlusMenu,
                            onDismissRequest = { showPlusMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Camera") },
                                leadingIcon = {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                },
                                onClick = {
                                    showPlusMenu = false
                                    attachedFile = "Camera_Photo.jpg"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Upload Photo") },
                                leadingIcon = {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                },
                                onClick = {
                                    showPlusMenu = false
                                    attachedFile = "Photo_Attachment.jpg"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Upload File") },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachFile, contentDescription = null)
                                },
                                onClick = {
                                    showPlusMenu = false
                                    attachedFile = "Document.pdf"
                                }
                            )
                        }
                    }

                    // Far Right: Microphone Icon & Single Dynamic Action (GraphicEq / Send) Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Microphone button
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                onVoiceInputClick()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Single Dynamic Button: GraphicEq when empty, Send button when text/file present
                        val isUserTypingOrAttached = inputText.isNotBlank() || attachedFile != null

                        if (!isUserTypingOrAttached) {
                            // GraphicEq (Live Voice Mode) button
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        focusManager.clearFocus()
                                        onVoiceInputClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Live Voice Mode",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Send Button
                            val canSend = !isGenerating
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (canSend) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    )
                                    .clickable(enabled = canSend) {
                                        focusManager.clearFocus()
                                        onSendClick(attachedFile)
                                        attachedFile = null
                                    }
                                    .testTag("send_message_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Send Message",
                                        tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
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

private fun String?.isNull_or_blank_custom(): Boolean = this == null || this.trim().isEmpty()
