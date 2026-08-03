package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: String, // "user", "assistant", "system", "error"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokensUsed: Int = 0,
    val isError: Boolean = false,
    val modelVersionUsed: String = ""
)
