package com.aiagent.data

import android.content.Context

/**
 * Plain SharedPreferences wrapper for user configuration. The Claude API key
 * is supplied by the user and never leaves the device except in calls to the
 * Anthropic API.
 */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString(KEY_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API, value).apply()

    var model: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var allowRoot: Boolean
        get() = prefs.getBoolean(KEY_ROOT, true)
        set(value) = prefs.edit().putBoolean(KEY_ROOT, value).apply()

    /** When false, every state-changing tool call waits for the user's OK. */
    var autoApprove: Boolean
        get() = prefs.getBoolean(KEY_AUTO, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO, value).apply()

    var maxTokens: Int
        get() = prefs.getInt(KEY_MAX_TOKENS, 8000)
        set(value) = prefs.edit().putInt(KEY_MAX_TOKENS, value).apply()

    companion object {
        const val DEFAULT_MODEL = "claude-opus-4-8"
        private const val KEY_API = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_ROOT = "allow_root"
        private const val KEY_AUTO = "auto_approve"
        private const val KEY_MAX_TOKENS = "max_tokens"
    }
}
