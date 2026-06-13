package com.aiagent.agent

import com.aiagent.data.MessageDao
import com.aiagent.data.MemoryDao
import com.aiagent.data.Message
import com.aiagent.data.ROLE_ASSISTANT
import com.aiagent.data.ROLE_TOOL
import com.aiagent.data.SettingsStore
import com.aiagent.network.AnthropicClient
import com.aiagent.network.AnthropicException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Drives the agentic loop for one user turn: call the model, run any requested
 * tools, feed the results back, repeat until the model stops asking for tools.
 *
 * Persisted display history (user + assistant text) is replayed to the model
 * on each turn. The detailed tool_use / tool_result blocks live only inside the
 * current turn's working message list; long-term continuity comes from the
 * memory tools instead.
 */
class AgentEngine(
    private val messageDao: MessageDao,
    memoryDao: MemoryDao,
    private val settings: SettingsStore,
) {
    private val executor = ToolExecutor(memoryDao, settings)
    private val memoryDao = memoryDao

    /** @param confirm asked before each state-changing tool when auto-approve is off. */
    suspend fun runTurn(
        chatId: Long,
        confirm: suspend (toolName: String, input: JSONObject) -> Boolean,
    ) = withContext(Dispatchers.IO) {
        val apiKey = settings.apiKey
        if (apiKey.isBlank()) {
            save(chatId, ROLE_ASSISTANT, "⚠️ Add your Claude API key in Settings first.")
            return@withContext
        }

        val client = AnthropicClient(apiKey)
        val tools = toolDefinitions()
        val system = buildSystemPrompt()
        val working = buildHistory(chatId)

        var iterations = 0
        try {
            while (iterations++ < MAX_ITERATIONS) {
                val response = client.createMessage(
                    model = settings.model,
                    system = system,
                    messages = working,
                    tools = tools,
                    maxTokens = settings.maxTokens,
                )

                val content = response.optJSONArray("content") ?: JSONArray()
                working.put(JSONObject().put("role", "assistant").put("content", content))

                val toolUses = ArrayList<JSONObject>()
                val text = StringBuilder()
                for (i in 0 until content.length()) {
                    val block = content.getJSONObject(i)
                    when (block.optString("type")) {
                        "text" -> text.append(block.optString("text"))
                        "tool_use" -> toolUses.add(block)
                    }
                }
                if (text.isNotBlank()) save(chatId, ROLE_ASSISTANT, text.toString().trim())

                if (response.optString("stop_reason") != "tool_use" || toolUses.isEmpty()) break

                val results = JSONArray()
                for (use in toolUses) {
                    val name = use.optString("name")
                    val id = use.optString("id")
                    val args = use.optJSONObject("input") ?: JSONObject()

                    save(chatId, ROLE_TOOL, "🔧 $name ${compact(args)}")

                    val allowed = settings.autoApprove || !isMutatingTool(name) || confirm(name, args)
                    val result = if (allowed) {
                        executor.execute(name, args)
                    } else {
                        ToolResult("User denied this action.", isError = true)
                    }

                    results.put(
                        JSONObject()
                            .put("type", "tool_result")
                            .put("tool_use_id", id)
                            .put("content", result.content)
                            .put("is_error", result.isError)
                    )
                }
                working.put(JSONObject().put("role", "user").put("content", results))
            }

            if (iterations >= MAX_ITERATIONS) {
                save(chatId, ROLE_ASSISTANT, "⚠️ Stopped after $MAX_ITERATIONS steps to avoid a loop.")
            }
        } catch (e: AnthropicException) {
            save(chatId, ROLE_ASSISTANT, "⚠️ API error ${e.code}: ${e.body.take(500)}")
        } catch (e: Exception) {
            save(chatId, ROLE_ASSISTANT, "⚠️ Error: ${e.message}")
        }
    }

    private suspend fun buildHistory(chatId: Long): JSONArray {
        val array = JSONArray()
        for (m in messageDao.replayHistory(chatId)) {
            array.put(JSONObject().put("role", m.role).put("content", m.content))
        }
        return array
    }

    private suspend fun buildSystemPrompt(): String {
        val memories = memoryDao.recent(40)
        val memText = if (memories.isEmpty()) "(none yet)"
        else memories.joinToString("\n") { "- ${it.content}" }
        return """
            You are an autonomous AI agent living inside an Android app on the user's
            own phone. You can control the device through your tools: run shell
            commands (including as root via su), read/write/delete files anywhere,
            inspect the device, and keep long-term memory across conversations.

            Operating principles:
            - Act on what the user asks. When a task needs several steps, do them with
              tool calls rather than only describing them.
            - Prefer the dedicated file tools for simple file work; use run_shell (with
              root=true when needed) for system files, settings, packages, or anything
              the plain file tools can't reach.
            - Save durable facts (preferences, ongoing projects, things you were told to
              remember) with save_memory, and check search_memory when relevant.
            - Be careful with destructive or system-wide actions. Briefly say what you
              are about to do before doing it.
            - Reply in the same language the user writes in.
            - When you are done, give a short, clear summary of what happened.

            Long-term memory:
            $memText
        """.trimIndent()
    }

    private suspend fun save(chatId: Long, role: String, content: String) {
        messageDao.insert(Message(chatId = chatId, role = role, content = content))
    }

    private fun compact(args: JSONObject): String {
        val s = args.toString()
        return if (s.length > 160) s.take(157) + "…" else s
    }

    companion object {
        private const val MAX_ITERATIONS = 30
    }
}
