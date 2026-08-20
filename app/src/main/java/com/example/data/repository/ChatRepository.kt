package com.example.data.repository

import com.example.data.dao.ChatDao
import com.example.data.dao.PersonaDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.PersonaEntity
import com.example.data.model.ProviderType
import com.example.network.AiApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class ChatRepository(
    private val personaDao: PersonaDao,
    private val chatDao: ChatDao,
    private val aiApiClient: AiApiClient
) {
    val activePersonas: Flow<List<PersonaEntity>> = personaDao.getActivePersonas()
    val allPersonas: Flow<List<PersonaEntity>> = personaDao.getAllPersonas()
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    suspend fun ensureDefaultPersonasInitialized() {
        val defaults = listOf(
            PersonaEntity(
                id = 1,
                displayName = "Omni-1.2",
                providerType = ProviderType.GEMINI.name,
                apiKey = "",
                modelVersion = "gemini-2.5-flash",
                systemPrompt = "You are Omni-1.2, a fast and smart general AI assistant.",
                temperature = 0.7f,
                maxTokens = 2048,
                badgeText = "Fast",
                isActive = true,
                isDefault = true
            ),
            PersonaEntity(
                id = 2,
                displayName = "Omni-1.4",
                providerType = ProviderType.CLAUDE.name,
                apiKey = "",
                modelVersion = "claude-3-5-sonnet-20241022",
                systemPrompt = "You are Omni-1.4, specialized in writing clean code, logic, and structured problem solving.",
                temperature = 0.2f,
                maxTokens = 4096,
                badgeText = "Coding",
                isActive = true,
                isDefault = false
            ),
            PersonaEntity(
                id = 3,
                displayName = "Omni-1.5",
                providerType = ProviderType.MISTRAL.name,
                apiKey = "",
                modelVersion = "mistral-large-latest",
                systemPrompt = "You are Omni-1.5, an advanced model for creative, high-speed writing and analysis.",
                temperature = 0.8f,
                maxTokens = 2048,
                badgeText = "Creative",
                isActive = true,
                isDefault = false
            ),
            PersonaEntity(
                id = 4,
                displayName = "Omni-2",
                providerType = ProviderType.OPENAI.name,
                apiKey = "",
                modelVersion = "gpt-4o-mini",
                systemPrompt = "You are Omni-2, a flagship reasoning engine with deep technical intelligence.",
                temperature = 0.5f,
                maxTokens = 2048,
                badgeText = "Logic Pro",
                isActive = true,
                isDefault = false
            )
        )
        personaDao.insertAll(defaults)
    }

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun getPersonaById(id: Long): PersonaEntity? {
        return personaDao.getPersonaById(id)
    }

    suspend fun createNewSession(persona: PersonaEntity): ChatSessionEntity {
        val session = ChatSessionEntity(
            id = UUID.randomUUID().toString(),
            title = "New Chat",
            personaId = persona.id,
            personaDisplayName = persona.displayName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertSession(session)
        return session
    }

    suspend fun sendMessage(
        sessionId: String,
        userText: String,
        persona: PersonaEntity
    ): Result<ChatMessageEntity> {
        // 1. Insert User Message
        val userMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = "user",
            content = userText.trim(),
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMsg)

        // Update session updatedAt and auto title if default "New Chat"
        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            val newTitle = if (session.title == "New Chat") {
                val words = userText.trim().split("\\s+".toRegex()).take(5).joinToString(" ")
                if (words.length > 30) words.take(30) + "..." else words.ifBlank { "Chat Session" }
            } else {
                session.title
            }
            chatDao.updateSessionTitle(sessionId, newTitle, System.currentTimeMillis())
            chatDao.updateSessionPersona(sessionId, persona.id, persona.displayName)
        }

        // 2. Fetch History for Context
        val history = chatDao.getMessagesForSessionOnce(sessionId)

        // 3. Call AI API Client
        val apiResult = aiApiClient.generateResponse(
            persona = persona,
            chatHistory = history.dropLast(1), // Exclude the user message we just inserted
            userMessage = userText
        )

        val assistantText = apiResult.getOrDefault(
            "An error occurred while communicating with ${persona.displayName}. Please verify settings."
        )

        // 4. Insert Assistant Message
        val assistantMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = "assistant",
            content = assistantText,
            timestamp = System.currentTimeMillis(),
            modelVersionUsed = persona.modelVersion
        )
        chatDao.insertMessage(assistantMsg)

        return Result.success(assistantMsg)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun togglePinSession(sessionId: String, currentPin: Boolean) {
        chatDao.updateSessionPin(sessionId, !currentPin)
    }

    suspend fun renameSession(sessionId: String, newTitle: String) {
        chatDao.updateSessionTitle(sessionId, newTitle)
    }

    suspend fun clearAllHistory() {
        chatDao.deleteAllSessions()
    }

    suspend fun updatePersona(persona: PersonaEntity) {
        personaDao.updatePersona(persona)
    }

    suspend fun insertPersona(persona: PersonaEntity): Long {
        return personaDao.insertPersona(persona)
    }

    suspend fun deletePersona(id: Long) {
        personaDao.deletePersona(id)
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessageById(messageId)
    }
}
