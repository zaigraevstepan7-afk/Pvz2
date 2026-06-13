package com.aiagent

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiagent.ui.AgentViewModel
import com.aiagent.ui.AppTheme
import com.aiagent.ui.ChatScreen
import com.aiagent.ui.SettingsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: AgentViewModel by viewModels()

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AppTheme {
                AppRoot(vm)
            }
        }
    }
}

@Composable
private fun AppRoot(vm: AgentViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showSettings by remember { mutableStateOf(false) }
    val chats by vm.chats.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Chats", Modifier.padding(16.dp))

                TextButton(
                    onClick = {
                        vm.newChat()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                ) { Text("+ New chat") }

                LazyColumn(Modifier.weight(1f)) {
                    items(chats, key = { it.id }) { chat ->
                        NavigationDrawerItem(
                            label = { Text(chat.title) },
                            selected = false,
                            onClick = {
                                vm.selectChat(chat.id)
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                IconButton(onClick = { vm.deleteChat(chat.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }

                TextButton(
                    onClick = { grantAllFilesAccess(context) },
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                ) { Text("Grant full file access") }
            }
        },
    ) {
        if (showSettings) {
            SettingsScreen(vm, onBack = { showSettings = false })
        } else {
            ChatScreen(
                vm = vm,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onOpenSettings = { showSettings = true },
            )
        }
    }
}

private fun grantAllFilesAccess(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
        }
    }
}
