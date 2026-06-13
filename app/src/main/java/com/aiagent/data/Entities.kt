package com.aiagent.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A conversation thread. */
@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

const val ROLE_USER = "user"
const val ROLE_ASSISTANT = "assistant"
const val ROLE_TOOL = "tool"

/**
 * A single message shown in the UI. Tool calls/results that happen inside one
 * agent turn are persisted with role = [ROLE_TOOL] so the transcript is
 * auditable, but only user/assistant text is replayed to the model.
 */
@Entity(
    tableName = "messages",
    indices = [Index("chatId")],
)
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val role: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Long-term memory: facts the agent chose to remember, surfaced into the
 * system prompt of every future conversation.
 */
@Entity(tableName = "memories")
data class MemoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
