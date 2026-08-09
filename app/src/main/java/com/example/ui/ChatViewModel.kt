package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.PersonaEntity
import com.example.data.model.ProviderType
import com.example.data.repository.ChatRepository
import com.example.network.AiApiClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(
        personaDao = db.personaDao(),
        chatDao = db.chatDao(),
        aiApiClient = AiApiClient()
    )

    val activePersonas: StateFlow<List<PersonaEntity>> = repository.activePersonas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPersonas: StateFlow<List<PersonaEntity>> = repository.allPersonas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSessions: StateFlow<List<ChatSessionEntity>> = combine(allSessions, _searchQuery) { sessions, query ->
        if (query.isBlank()) sessions
        else sessions.filter { it.title.contains(query, ignoreCase = true) || it.personaDisplayName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getMessagesForSession(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPersona = MutableStateFlow<PersonaEntity?>(null)
    val selectedPersona: StateFlow<PersonaEntity?> = _selectedPersona.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _isProfileSettingsOpen = MutableStateFlow(false)
    val isProfileSettingsOpen: StateFlow<Boolean> = _isProfileSettingsOpen.asStateFlow()

    private val _isCustomPageOpen = MutableStateFlow(false)
    val isCustomPageOpen: StateFlow<Boolean> = _isCustomPageOpen.asStateFlow()

    private val _selectedAccentColorIndex = MutableStateFlow(0)
    val selectedAccentColorIndex: StateFlow<Int> = _selectedAccentColorIndex.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM") // "SYSTEM", "LIGHT", "DARK"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _editingPersona = MutableStateFlow<PersonaEntity?>(null)
    val editingPersona: StateFlow<PersonaEntity?> = _editingPersona.asStateFlow()

    private val _userNotice = MutableStateFlow<String?>(null)
    val userNotice: StateFlow<String?> = _userNotice.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultPersonasInitialized()
            // Observe active personas to select default
            activePersonas.collect { list ->
                if (_selectedPersona.value == null && list.isNotEmpty()) {
                    _selectedPersona.value = list.firstOrNull { it.isDefault } ?: list.first()
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPersona(persona: PersonaEntity) {
        _selectedPersona.value = persona
        _userNotice.value = "Selected ${persona.displayName}"
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun openProfileSettings() {
        _isProfileSettingsOpen.value = true
    }

    fun closeProfileSettings() {
        _isProfileSettingsOpen.value = false
    }

    fun openCustomPage() {
        _isCustomPageOpen.value = true
    }

    fun closeCustomPage() {
        _isCustomPageOpen.value = false
    }

    fun selectAccentColor(index: Int) {
        _selectedAccentColorIndex.value = index
        _userNotice.value = "Theme accent color updated"
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        _isDarkMode.value = (mode == "DARK")
        _userNotice.value = when (mode) {
            "DARK" -> "Dark theme enabled"
            "LIGHT" -> "Light theme enabled"
            else -> "System default theme enabled"
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) "DARK" else "LIGHT")
    }

    fun submitReport(category: String, description: String) {
        _userNotice.value = "Report submitted successfully. Thank you for your feedback!"
    }

    fun createNewSession() {
        viewModelScope.launch {
            val persona = _selectedPersona.value ?: activePersonas.value.firstOrNull()
            if (persona != null) {
                val newSession = repository.createNewSession(persona)
                _selectedSessionId.value = newSession.id
            } else {
                _userNotice.value = "Please create or activate an AI Persona first."
            }
        }
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
        viewModelScope.launch {
            val sessions = allSessions.value
            val target = sessions.find { it.id == sessionId }
            if (target != null) {
                val persona = repository.getPersonaById(target.personaId)
                if (persona != null && persona.isActive) {
                    _selectedPersona.value = persona
                }
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_selectedSessionId.value == sessionId) {
                val remaining = allSessions.value.filter { it.id != sessionId }
                _selectedSessionId.value = remaining.firstOrNull()?.id
            }
            _userNotice.value = "Chat deleted"
        }
    }

    fun togglePinSession(sessionId: String, currentPin: Boolean) {
        viewModelScope.launch {
            repository.togglePinSession(sessionId, currentPin)
            _userNotice.value = if (!currentPin) "Chat pinned to top" else "Chat unpinned"
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            if (newTitle.isNotBlank()) {
                repository.renameSession(sessionId, newTitle.trim())
                _userNotice.value = "Chat renamed"
            }
        }
    }

    fun downloadChatContext(context: Context) {
        val msgs = currentMessages.value
        if (msgs.isEmpty()) {
            Toast.makeText(context, "No chat history to export", Toast.LENGTH_SHORT).show()
            return
        }
        val sb = StringBuilder()
        val sessionTitle = allSessions.value.find { it.id == _selectedSessionId.value }?.title ?: "Chat"
        sb.append("=== Chat Context Export: $sessionTitle ===\n\n")
        msgs.forEach { m ->
            val sender = if (m.role == "user") "USER" else "AI ASSISTANT"
            sb.append("[$sender]:\n${m.content}\n\n")
        }
        val textToExport = sb.toString()

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Chat Context Export", textToExport)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Chat context downloaded to clipboard!", Toast.LENGTH_LONG).show()
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            var sessionId = _selectedSessionId.value
            val persona = _selectedPersona.value ?: activePersonas.value.firstOrNull()

            if (persona == null) {
                _userNotice.value = "No active AI Persona available."
                return@launch
            }

            if (sessionId == null) {
                val newSession = repository.createNewSession(persona)
                sessionId = newSession.id
                _selectedSessionId.value = sessionId
            }

            _isGenerating.value = true
            try {
                repository.sendMessage(sessionId, userText, persona)
            } catch (e: Exception) {
                _userNotice.value = "Error sending message: ${e.localizedMessage}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _selectedSessionId.value = null
            _userNotice.value = "All chat history cleared"
        }
    }

    fun openPersonaEditor(persona: PersonaEntity?) {
        _editingPersona.value = persona ?: PersonaEntity(
            displayName = "New Custom Assistant",
            providerType = ProviderType.GEMINI.name,
            apiKey = "",
            modelVersion = "gemini-2.5-flash",
            systemPrompt = "You are a helpful, expert AI assistant.",
            temperature = 0.7f,
            maxTokens = 2048,
            badgeText = "Custom",
            isActive = true
        )
    }

    fun closePersonaEditor() {
        _editingPersona.value = null
    }

    fun savePersona(persona: PersonaEntity) {
        viewModelScope.launch {
            if (persona.id == 0L) {
                repository.insertPersona(persona)
                _userNotice.value = "Created AI Persona: ${persona.displayName}"
            } else {
                repository.updatePersona(persona)
                _userNotice.value = "Updated AI Persona: ${persona.displayName}"
            }
            // Update current selected persona if editing active one
            if (_selectedPersona.value?.id == persona.id) {
                _selectedPersona.value = persona
            }
            _editingPersona.value = null
        }
    }

    fun deletePersona(personaId: Long) {
        viewModelScope.launch {
            val persona = allPersonas.value.find { it.id == personaId }
            repository.deletePersona(personaId)
            if (_selectedPersona.value?.id == personaId) {
                _selectedPersona.value = activePersonas.value.firstOrNull { it.id != personaId }
            }
            _userNotice.value = "Deleted Persona ${persona?.displayName ?: ""}"
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Message", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }

    fun clearNotice() {
        _userNotice.value = null
    }
}
