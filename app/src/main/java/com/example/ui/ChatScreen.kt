package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PersonaEntity
import com.example.ui.components.ChatBubble
import com.example.ui.components.ChatDrawerContent
import com.example.ui.components.PersonaSelector
import com.example.ui.components.TypingIndicatorBubble
import kotlinx.coroutines.launch
import java.util.Locale

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
    var inputText by remember { mutableStateOf("") }
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
            onSubmitReport = viewModel::submitReport
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
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
                        onOpenProfileSettings = {
                            viewModel.openProfileSettings()
                            scope.launch { drawerState.close() }
                        },
                        onClearAllHistory = viewModel::clearAllHistory
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Nexus AI",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = selectedPersona?.displayName ?: "Multi-Model Assistant",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("drawer_toggle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Chat History Drawer"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.createNewSession() }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Start New Chat"
                                )
                            }
                            IconButton(onClick = viewModel::toggleAdminMode) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Settings",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                modifier = modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Chat Messages Body or Empty State
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (messages.isEmpty()) {
                            EmptyChatState(
                                selectedPersona = selectedPersona,
                                onPromptSelected = { prompt ->
                                    inputText = prompt
                                    viewModel.sendMessage(prompt)
                                    inputText = ""
                                }
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
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
                        onSendClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
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
                        },
                        selectedPersona = selectedPersona,
                        activePersonas = activePersonas,
                        onSelectPersona = viewModel::selectPersona,
                        onOpenAdmin = viewModel::toggleAdminMode
                    )
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
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        Text(
            text = "TRY ASKING:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val samplePrompts = listOf(
            "Write a Kotlin Coroutines helper function",
            "Draft a professional project launch email",
            "Explain Quantum Computing in simple terms",
            "Brainstorm 5 innovative startup ideas"
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            samplePrompts.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onPromptSelected(prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isGenerating: Boolean,
    onSendClick: () -> Unit,
    onVoiceInputClick: () -> Unit,
    selectedPersona: PersonaEntity?,
    activePersonas: List<PersonaEntity>,
    onSelectPersona: (PersonaEntity) -> Unit,
    onOpenAdmin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 40dp Width AI Selector Button (Left)
            PersonaSelector(
                selectedPersona = selectedPersona,
                activePersonas = activePersonas,
                onSelectPersona = onSelectPersona,
                onOpenAdmin = onOpenAdmin,
                isCompact = true,
                modifier = Modifier.padding(end = 6.dp)
            )

            // Text Input Field (Middle)
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = { Text("Message...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Voice Microphone Input
            IconButton(
                onClick = onVoiceInputClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    .clickable(enabled = inputText.isNotBlank() && !isGenerating) { onSendClick() }
                    .testTag("send_message_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_blank_custom(): Boolean = this == null || this.trim().isEmpty()
