package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProviderType(val displayName: String) {
    GEMINI("Google Gemini"),
    CLAUDE("Anthropic Claude"),
    MISTRAL("Mistral AI"),
    OPENAI("OpenAI / ChatGPT"),
    CUSTOM_REST("Custom REST / OpenRouter")
}

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String, // Name shown to end user (e.g., "Smart Logic Pro")
    val providerType: String, // GEMINI, CLAUDE, MISTRAL, OPENAI, CUSTOM_REST
    val apiKey: String = "",
    val modelVersion: String = "", // Model identifier (e.g. gemini-2.5-flash, claude-3-5-sonnet, mistral-large-latest)
    val baseUrl: String = "", // Optional custom endpoint
    val systemPrompt: String = "You are a helpful, intelligent, and concise AI assistant.",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val badgeText: String = "", // e.g. "Fast", "Pro", "Coding"
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
