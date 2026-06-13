package com.aiagent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiagent.data.Message
import com.aiagent.data.ROLE_ASSISTANT
import com.aiagent.data.ROLE_TOOL
import com.aiagent.data.ROLE_USER
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: AgentViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val messages by vm.messages.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val pending by vm.pending.collectAsState()

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size, isRunning) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.lastIndex) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Agent") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Chats")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.newChat() }) {
                        Icon(Icons.Default.Add, contentDescription = "New chat")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (messages.isEmpty()) {
                    item {
                        Text(
                            "Ask the agent to do something on your phone.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
                items(messages, key = { it.id }) { msg -> MessageBubble(msg) }
                if (isRunning) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.padding(8.dp), strokeWidth = 2.dp)
                            Text("working…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message…") },
                    maxLines = 6,
                )
                IconButton(
                    enabled = !isRunning && input.isNotBlank(),
                    onClick = {
                        vm.send(input)
                        input = ""
                    },
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }

    pending?.let { p ->
        AlertDialog(
            onDismissRequest = { p.resolve(false) },
            title = { Text("Allow action?") },
            text = { Text("The agent wants to run:\n\n${p.toolName}\n${p.summary}") },
            confirmButton = { TextButton(onClick = { p.resolve(true) }) { Text("Allow") } },
            dismissButton = { TextButton(onClick = { p.resolve(false) }) { Text("Deny") } },
        )
    }
}

@Composable
private fun MessageBubble(msg: Message) {
    when (msg.role) {
        ROLE_TOOL -> Text(
            msg.content,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        else -> {
            val isUser = msg.role == ROLE_USER
            val bg = if (isUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
            val fg = if (isUser) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
            Box(Modifier.fillMaxWidth()) {
                Surface(
                    color = bg,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart),
                ) {
                    Text(msg.content, color = fg, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
