package com.aiagent.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.App
import com.aiagent.agent.AgentEngine
import com.aiagent.data.Chat
import com.aiagent.data.Message
import com.aiagent.data.ROLE_USER
import com.aiagent.data.SettingsStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

data class PendingConfirmation(
    val toolName: String,
    val summary: String,
    private val deferred: CompletableDeferred<Boolean>,
) {
    fun resolve(allow: Boolean) = deferred.complete(allow)
}

class SettingsSnapshot(store: SettingsStore) {
    val apiKey = store.apiKey
    val model = store.model
    val allowRoot = store.allowRoot
    val autoApprove = store.autoApprove
    val maxTokens = store.maxTokens
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgentViewModel(app: Application) : AndroidViewModel(app) {

    private val db = (app as App).database
    private val settings = (app as App).settings
    private val engine = AgentEngine(db.messageDao(), db.memoryDao(), settings)

    private val _currentChatId = MutableStateFlow<Long?>(null)
    val currentChatId: StateFlow<Long?> = _currentChatId.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _pending = MutableStateFlow<PendingConfirmation?>(null)
    val pending: StateFlow<PendingConfirmation?> = _pending.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsSnapshot(settings))
    val settingsState: StateFlow<SettingsSnapshot> = _settingsState.asStateFlow()

    val chats: StateFlow<List<Chat>> = db.chatDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<Message>> = _currentChatId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else db.messageDao().observeForChat(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectChat(id: Long) { _currentChatId.value = id }

    fun newChat() { _currentChatId.value = null }

    fun deleteChat(id: Long) {
        viewModelScope.launch {
            db.messageDao().deleteForChat(id)
            db.chatDao().delete(id)
            if (_currentChatId.value == id) _currentChatId.value = null
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isRunning.value) return
        viewModelScope.launch {
            var chatId = _currentChatId.value
            if (chatId == null) {
                chatId = db.chatDao().insert(Chat(title = deriveTitle(trimmed)))
                _currentChatId.value = chatId
            } else {
                db.chatDao().touch(chatId)
            }
            db.messageDao().insert(Message(chatId = chatId, role = ROLE_USER, content = trimmed))

            _isRunning.value = true
            try {
                engine.runTurn(chatId) { name, input -> requestConfirmation(name, input) }
            } finally {
                _isRunning.value = false
            }
        }
    }

    private suspend fun requestConfirmation(name: String, input: JSONObject): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        _pending.value = PendingConfirmation(name, input.toString(), deferred)
        val result = deferred.await()
        _pending.value = null
        return result
    }

    // --- Settings mutations -------------------------------------------------

    fun saveApiKey(value: String) { settings.apiKey = value.trim(); refreshSettings() }
    fun saveModel(value: String) { settings.model = value.trim(); refreshSettings() }
    fun setAllowRoot(value: Boolean) { settings.allowRoot = value; refreshSettings() }
    fun setAutoApprove(value: Boolean) { settings.autoApprove = value; refreshSettings() }
    fun setMaxTokens(value: Int) { settings.maxTokens = value; refreshSettings() }

    private fun refreshSettings() { _settingsState.value = SettingsSnapshot(settings) }

    private fun deriveTitle(text: String): String =
        text.replace("\n", " ").take(40).ifBlank { "New chat" }
}
