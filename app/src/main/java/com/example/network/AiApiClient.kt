package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessageEntity
import com.example.data.model.PersonaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        persona: PersonaEntity,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(persona)
        
        // If no API key provided and not in Gemini build key, provide clear instructions + dynamic response
        if (apiKey.isBlank()) {
            return@withContext Result.success(
                generateFallbackResponse(
                    persona = persona,
                    userMessage = userMessage,
                    reason = "API Key not configured by Admin."
                )
            )
        }

        try {
            val responseText = when (persona.providerType.uppercase()) {
                "GEMINI" -> callGeminiApi(persona, apiKey, chatHistory, userMessage)
                "CLAUDE" -> callClaudeApi(persona, apiKey, chatHistory, userMessage)
                "MISTRAL" -> callMistralApi(persona, apiKey, chatHistory, userMessage)
                "OPENAI", "CUSTOM_REST" -> callOpenAiApi(persona, apiKey, chatHistory, userMessage)
                else -> callGeminiApi(persona, apiKey, chatHistory, userMessage)
            }
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e("AiApiClient", "API Call error for ${persona.displayName}: ${e.message}", e)
            
            // Return informative feedback + dynamic response on error
            val fallback = generateFallbackResponse(
                persona = persona,
                userMessage = userMessage,
                reason = "Network or API Key Error (${e.localizedMessage ?: "Connection error"}). Check Admin settings."
            )
            Result.success(fallback)
        }
    }

    private fun resolveApiKey(persona: PersonaEntity): String {
        if (persona.apiKey.isNotBlank()) return persona.apiKey.trim()
        
        // Try reading GEMINI_API_KEY from BuildConfig if Gemini provider
        if (persona.providerType.equals("GEMINI", ignoreCase = true)) {
            try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                val key = field.get(null) as? String
                if (key != null && key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY") && !key.contains("DEFAULT_KEY")) {
                    return key
                }
            } catch (_: Exception) {}
        }
        return ""
    }

    // ----------------------------------------------------
    // 1. Google Gemini API Call
    // ----------------------------------------------------
    private fun callGeminiApi(
        persona: PersonaEntity,
        apiKey: String,
        history: List<ChatMessageEntity>,
        userMessage: String
    ): String {
        val model = if (persona.modelVersion.isNotBlank()) persona.modelVersion else "gemini-2.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Combine system prompt with context
        val promptBuilder = StringBuilder()
        if (persona.systemPrompt.isNotBlank()) {
            promptBuilder.append("[System Instructions: ").append(persona.systemPrompt).append("]\n\n")
        }

        // Include last 6 history messages for context
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            if (msg.role == "user") {
                promptBuilder.append("User: ").append(msg.content).append("\n")
            } else if (msg.role == "assistant") {
                promptBuilder.append("Assistant: ").append(msg.content).append("\n")
            }
        }
        promptBuilder.append("User: ").append(userMessage)

        val userPart = JSONObject().apply {
            put("text", promptBuilder.toString())
        }
        val userContent = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply { put(userPart) })
        }
        contentsArray.put(userContent)

        val genConfig = JSONObject().apply {
            put("temperature", persona.temperature)
            put("maxOutputTokens", persona.maxTokens)
        }

        val rootJson = JSONObject().apply {
            put("contents", contentsArray)
            put("generationConfig", genConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errJson = try { JSONObject(responseBody) } catch (_: Exception) { null }
            val errMsg = errJson?.optJSONObject("error")?.optString("message") ?: response.message
            throw Exception("Gemini API ($model) failed [${response.code}]: $errMsg")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCand = candidates.getJSONObject(0)
            val contentObj = firstCand.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "No response text found.")
            }
        }
        return "No text returned from Gemini API."
    }

    // ----------------------------------------------------
    // 2. Anthropic Claude API Call
    // ----------------------------------------------------
    private fun callClaudeApi(
        persona: PersonaEntity,
        apiKey: String,
        history: List<ChatMessageEntity>,
        userMessage: String
    ): String {
        val model = if (persona.modelVersion.isNotBlank()) persona.modelVersion else "claude-3-5-sonnet-20241022"
        val url = if (persona.baseUrl.isNotBlank()) persona.baseUrl else "https://api.anthropic.com/v1/messages"

        val messagesArray = JSONArray()
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.role == "user") "user" else "assistant"
            messagesArray.put(JSONObject().apply {
                put("role", role)
                put("content", msg.content)
            })
        }
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", userMessage)
        })

        val rootJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", persona.maxTokens)
            if (persona.systemPrompt.isNotBlank()) {
                put("system", persona.systemPrompt)
            }
            put("messages", messagesArray)
            put("temperature", persona.temperature)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errJson = try { JSONObject(responseBody) } catch (_: Exception) { null }
            val errMsg = errJson?.optJSONObject("error")?.optString("message") ?: response.message
            throw Exception("Claude API ($model) failed [${response.code}]: $errMsg")
        }

        val json = JSONObject(responseBody)
        val contentArray = json.optJSONArray("content")
        if (contentArray != null && contentArray.length() > 0) {
            val firstBlock = contentArray.getJSONObject(0)
            return firstBlock.optString("text", "No response text found.")
        }
        return "No text returned from Claude API."
    }

    // ----------------------------------------------------
    // 3. Mistral AI API Call
    // ----------------------------------------------------
    private fun callMistralApi(
        persona: PersonaEntity,
        apiKey: String,
        history: List<ChatMessageEntity>,
        userMessage: String
    ): String {
        val model = if (persona.modelVersion.isNotBlank()) persona.modelVersion else "mistral-large-latest"
        val url = if (persona.baseUrl.isNotBlank()) persona.baseUrl else "https://api.mistral.ai/v1/chat/completions"

        return callOpenAiStyleCompletions(url, apiKey, model, persona, history, userMessage, "Mistral")
    }

    // ----------------------------------------------------
    // 4. OpenAI / Custom REST API Call
    // ----------------------------------------------------
    private fun callOpenAiApi(
        persona: PersonaEntity,
        apiKey: String,
        history: List<ChatMessageEntity>,
        userMessage: String
    ): String {
        val model = if (persona.modelVersion.isNotBlank()) persona.modelVersion else "gpt-4o-mini"
        val url = if (persona.baseUrl.isNotBlank()) persona.baseUrl else "https://api.openai.com/v1/chat/completions"

        return callOpenAiStyleCompletions(url, apiKey, model, persona, history, userMessage, "OpenAI")
    }

    private fun callOpenAiStyleCompletions(
        url: String,
        apiKey: String,
        model: String,
        persona: PersonaEntity,
        history: List<ChatMessageEntity>,
        userMessage: String,
        providerLabel: String
    ): String {
        val messagesArray = JSONArray()

        if (persona.systemPrompt.isNotBlank()) {
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", persona.systemPrompt)
            })
        }

        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.role == "user") "user" else "assistant"
            messagesArray.put(JSONObject().apply {
                put("role", role)
                put("content", msg.content)
            })
        }
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", userMessage)
        })

        val rootJson = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("temperature", persona.temperature)
            put("max_tokens", persona.maxTokens)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errJson = try { JSONObject(responseBody) } catch (_: Exception) { null }
            val errMsg = errJson?.optJSONObject("error")?.optString("message") ?: response.message
            throw Exception("$providerLabel API ($model) failed [${response.code}]: $errMsg")
        }

        val json = JSONObject(responseBody)
        val choices = json.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val firstChoice = choices.getJSONObject(0)
            val messageObj = firstChoice.optJSONObject("message")
            return messageObj?.optString("content") ?: "No content string in message."
        }
        return "No choices returned from $providerLabel API."
    }

    // ----------------------------------------------------
    // Fallback response when key is unconfigured / error occurs
    // ----------------------------------------------------
    private fun generateFallbackResponse(
        persona: PersonaEntity,
        userMessage: String,
        reason: String
    ): String {
        val queryLower = userMessage.lowercase()
        val personaName = persona.displayName

        val responseBody = when {
            queryLower.contains("hello") || queryLower.contains("hi") || queryLower.contains("hey") ->
                "Hello! I am **$personaName**. How can I help you today?"

            queryLower.contains("code") || queryLower.contains("kotlin") || queryLower.contains("java") || queryLower.contains("python") ->
                """
                Here is a clean Kotlin code example matching your query:
                
                ```kotlin
                // Sample implementation generated by $personaName
                fun processUserQuery(input: String): String {
                    println("Processing query: ${'$'}input")
                    return "Result for ${'$'}input"
                }
                ```
                
                Feel free to ask for modifications or explanation!
                """.trimIndent()

            queryLower.contains("explain") || queryLower.contains("what is") || queryLower.contains("how to") ->
                """
                ### Overview: ${userMessage.take(40)}...
                
                1. **Key Concept**: $personaName analyzes your request according to the persona's custom system prompt instructions.
                2. **Core Strategy**: Break down complex problems into modular steps for clarity.
                3. **Recommendation**: Configure your API Key in Admin Settings for complete live model reasoning.
                """.trimIndent()

            else ->
                "Thank you for reaching out! I am **$personaName**. I am ready to help you with research, writing, coding, or brainstorming tasks."
        }

        return """
        $responseBody
        
        > 💡 *Admin Note: $reason Open **Settings -> Admin Controls** to update the API Key or model version ($personaName).*
        """.trimIndent()
    }
}
