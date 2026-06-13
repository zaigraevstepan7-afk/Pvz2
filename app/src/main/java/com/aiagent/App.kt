package com.aiagent

import android.app.Application
import com.aiagent.data.AppDatabase
import com.aiagent.data.SettingsStore

/**
 * Process-wide singletons. Kept deliberately simple (no DI framework) so the
 * whole app is easy to read end to end.
 */
class App : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.build(this)
        settings = SettingsStore(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
