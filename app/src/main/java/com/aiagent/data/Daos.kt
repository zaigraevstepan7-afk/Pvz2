package com.aiagent.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(chat: Chat): Long

    @Query("UPDATE chats SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rename(id: Long, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chats SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touch(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Chat>>

    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun get(id: Long): Chat?
}

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message): Long

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteForChat(chatId: Long)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY id ASC")
    fun observeForChat(chatId: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND role IN ('user','assistant') ORDER BY id ASC")
    suspend fun replayHistory(chatId: Long): List<Message>
}

@Dao
interface MemoryDao {
    @Insert
    suspend fun insert(entry: MemoryEntry): Long

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MemoryEntry>>

    @Query("SELECT * FROM memories ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<MemoryEntry>

    @Query(
        "SELECT * FROM memories WHERE content LIKE '%' || :q || '%' OR tags LIKE '%' || :q || '%' " +
            "ORDER BY createdAt DESC LIMIT :limit"
    )
    suspend fun search(q: String, limit: Int): List<MemoryEntry>
}
