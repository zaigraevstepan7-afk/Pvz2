package com.aiagent.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AnthropicException(val code: Int, val body: String) :
    RuntimeException("Anthropic API error $code: $body")

/**
 * Thin Messages API client over OkHttp. The Anthropic Java SDK is JVM-oriented
 * and not officially supported on Android, so we talk to /v1/messages directly.
 * One non-streaming request per call; the agent loop in AgentEngine drives the
 * tool-use cycle.
 */
class AnthropicClient(private val apiKey: String) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json".toMediaType()

    /**
     * Sends one Messages API request and returns the parsed response object.
     * @param messages full conversation as a JSON array of {role, content}.
     * @param tools tool definitions; omitted from the request when empty.
     */
    fun createMessage(
        model: String,
        system: String,
        messages: JSONArray,
        tools: JSONArray,
        maxTokens: Int,
    ): JSONObject {
        val body = JSONObject()
            .put("model", model)
            .put("max_tokens", maxTokens)
            .put("system", system)
            .put("messages", messages)
        if (tools.length() > 0) body.put("tools", tools)

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()

        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw AnthropicException(response.code, text)
            return JSONObject(text)
        }
    }
}
