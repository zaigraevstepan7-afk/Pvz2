package com.aiagent.agent

import com.aiagent.data.MemoryDao
import com.aiagent.data.MemoryEntry
import com.aiagent.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class ToolResult(val content: String, val isError: Boolean = false)

/**
 * Returns whether a tool changes device state. State-changing tools can be
 * gated behind user confirmation (see SettingsStore.autoApprove).
 */
fun isMutatingTool(name: String): Boolean = name in setOf(
    "run_shell", "write_file", "delete_path", "make_dir",
)

/** Tool schemas advertised to the model, as an Anthropic `tools` JSON array. */
fun toolDefinitions(): JSONArray {
    val tools = JSONArray()

    tools.put(
        tool(
            "run_shell",
            "Execute a shell command on the Android device and return its output. " +
                "Set root=true to run as superuser via su (needed for system files, " +
                "settings, other apps' data, package management, etc.).",
            obj(
                "command" to strProp("The shell command to run."),
                "root" to JSONObject().put("type", "boolean")
                    .put("description", "Run as root via su. Default false."),
            ),
            required = listOf("command"),
        )
    )

    tools.put(
        tool(
            "read_file",
            "Read a text file from the device filesystem and return its contents.",
            obj("path" to strProp("Absolute file path, e.g. /sdcard/notes.txt")),
            required = listOf("path"),
        )
    )

    tools.put(
        tool(
            "write_file",
            "Create or overwrite a text file (or append to it). Parent directories " +
                "are created automatically.",
            obj(
                "path" to strProp("Absolute file path to write."),
                "content" to strProp("Text content to write."),
                "append" to JSONObject().put("type", "boolean")
                    .put("description", "Append instead of overwrite. Default false."),
            ),
            required = listOf("path", "content"),
        )
    )

    tools.put(
        tool(
            "list_dir",
            "List the entries of a directory with type and size.",
            obj("path" to strProp("Absolute directory path, e.g. /sdcard")),
            required = listOf("path"),
        )
    )

    tools.put(
        tool(
            "make_dir",
            "Create a directory (and any missing parents).",
            obj("path" to strProp("Absolute directory path to create.")),
            required = listOf("path"),
        )
    )

    tools.put(
        tool(
            "delete_path",
            "Delete a file or directory.",
            obj(
                "path" to strProp("Absolute path to delete."),
                "recursive" to JSONObject().put("type", "boolean")
                    .put("description", "Delete directories recursively. Default false."),
            ),
            required = listOf("path"),
        )
    )

    tools.put(
        tool(
            "save_memory",
            "Save a fact to long-term memory so you can recall it in future " +
                "conversations. Use for user preferences, important context, or " +
                "things you were asked to remember.",
            obj(
                "content" to strProp("The fact to remember."),
                "tags" to strProp("Optional comma-separated tags."),
            ),
            required = listOf("content"),
        )
    )

    tools.put(
        tool(
            "search_memory",
            "Search long-term memory for previously saved facts.",
            obj("query" to strProp("Keyword(s) to search for.")),
            required = listOf("query"),
        )
    )

    tools.put(
        tool(
            "device_info",
            "Get basic device information (model, Android version, storage, root).",
            obj(),
            required = emptyList(),
        )
    )

    return tools
}

private fun tool(name: String, desc: String, props: JSONObject, required: List<String>): JSONObject {
    val schema = JSONObject()
        .put("type", "object")
        .put("properties", props)
        .put("required", JSONArray(required))
    return JSONObject()
        .put("name", name)
        .put("description", desc)
        .put("input_schema", schema)
}

private fun obj(vararg pairs: Pair<String, JSONObject>): JSONObject {
    val o = JSONObject()
    pairs.forEach { (k, v) -> o.put(k, v) }
    return o
}

private fun strProp(description: String): JSONObject =
    JSONObject().put("type", "string").put("description", description)

/**
 * Executes a tool call and returns a textual result for the model. All file
 * and shell work runs off the main thread.
 */
class ToolExecutor(
    private val memoryDao: MemoryDao,
    private val settings: SettingsStore,
) {
    suspend fun execute(name: String, input: JSONObject): ToolResult = withContext(Dispatchers.IO) {
        try {
            when (name) {
                "run_shell" -> runShell(input)
                "read_file" -> readFile(input)
                "write_file" -> writeFile(input)
                "list_dir" -> listDir(input)
                "make_dir" -> makeDir(input)
                "delete_path" -> deletePath(input)
                "save_memory" -> saveMemory(input)
                "search_memory" -> searchMemory(input)
                "device_info" -> deviceInfo()
                else -> ToolResult("Unknown tool: $name", isError = true)
            }
        } catch (e: Exception) {
            ToolResult("Tool '$name' failed: ${e.message}", isError = true)
        }
    }

    private fun runShell(input: JSONObject): ToolResult {
        val command = input.getString("command")
        val asRoot = input.optBoolean("root", false)
        if (asRoot && !settings.allowRoot) {
            return ToolResult("Root execution is disabled in settings.", isError = true)
        }
        val r = RootShell.run(command, asRoot)
        val sb = StringBuilder()
        sb.append("exit code: ").append(r.exitCode)
        if (r.timedOut) sb.append(" (timed out)")
        if (r.stdout.isNotEmpty()) sb.append("\n--- stdout ---\n").append(r.stdout)
        if (r.stderr.isNotEmpty()) sb.append("\n--- stderr ---\n").append(r.stderr)
        return ToolResult(sb.toString(), isError = r.exitCode != 0)
    }

    private fun readFile(input: JSONObject): ToolResult {
        val file = File(input.getString("path"))
        if (!file.exists()) return ToolResult("File not found: ${file.path}", isError = true)
        if (file.length() > 1_000_000) {
            return ToolResult("File too large (${file.length()} bytes). Use run_shell with head/tail.", isError = true)
        }
        return ToolResult(file.readText())
    }

    private fun writeFile(input: JSONObject): ToolResult {
        val file = File(input.getString("path"))
        val content = input.getString("content")
        val append = input.optBoolean("append", false)
        file.parentFile?.mkdirs()
        if (append) file.appendText(content) else file.writeText(content)
        return ToolResult("Wrote ${content.length} chars to ${file.path}")
    }

    private fun listDir(input: JSONObject): ToolResult {
        val dir = File(input.getString("path"))
        if (!dir.exists()) return ToolResult("Not found: ${dir.path}", isError = true)
        val entries = dir.listFiles()
            ?: return ToolResult("Cannot list ${dir.path} (permission denied?). Try run_shell with root.", isError = true)
        val sb = StringBuilder("${dir.path} (${entries.size} entries):\n")
        entries.sortedBy { it.name }.forEach {
            val type = if (it.isDirectory) "dir " else "file"
            sb.append("[$type] ${it.name}").append(if (it.isFile) "  ${it.length()}B" else "").append('\n')
        }
        return ToolResult(sb.toString().trim())
    }

    private fun makeDir(input: JSONObject): ToolResult {
        val dir = File(input.getString("path"))
        return if (dir.mkdirs() || dir.isDirectory) ToolResult("Created ${dir.path}")
        else ToolResult("Failed to create ${dir.path}", isError = true)
    }

    private fun deletePath(input: JSONObject): ToolResult {
        val file = File(input.getString("path"))
        val recursive = input.optBoolean("recursive", false)
        if (!file.exists()) return ToolResult("Not found: ${file.path}", isError = true)
        val ok = if (file.isDirectory && recursive) file.deleteRecursively() else file.delete()
        return if (ok) ToolResult("Deleted ${file.path}")
        else ToolResult("Failed to delete ${file.path} (try recursive=true or run_shell with root).", isError = true)
    }

    private suspend fun saveMemory(input: JSONObject): ToolResult {
        val content = input.getString("content")
        val tags = input.optString("tags", "")
        memoryDao.insert(MemoryEntry(content = content, tags = tags))
        return ToolResult("Saved to memory.")
    }

    private suspend fun searchMemory(input: JSONObject): ToolResult {
        val query = input.getString("query")
        val hits = memoryDao.search(query, limit = 20)
        if (hits.isEmpty()) return ToolResult("No memories match \"$query\".")
        val sb = StringBuilder("Found ${hits.size} memories:\n")
        hits.forEach { sb.append("- ").append(it.content).append('\n') }
        return ToolResult(sb.toString().trim())
    }

    private fun deviceInfo(): ToolResult {
        val sb = StringBuilder()
        sb.append("Model: ").append(android.os.Build.MANUFACTURER).append(' ')
            .append(android.os.Build.MODEL).append('\n')
        sb.append("Android: ").append(android.os.Build.VERSION.RELEASE)
            .append(" (API ").append(android.os.Build.VERSION.SDK_INT).append(")\n")
        val data = android.os.Environment.getDataDirectory()
        val stat = android.os.StatFs(data.path)
        val freeGb = stat.availableBytes / (1024.0 * 1024 * 1024)
        val totalGb = stat.totalBytes / (1024.0 * 1024 * 1024)
        sb.append(String.format("Storage: %.1f GB free of %.1f GB\n", freeGb, totalGb))
        sb.append("Root available: ").append(RootShell.isRootAvailable())
        return ToolResult(sb.toString())
    }
}
