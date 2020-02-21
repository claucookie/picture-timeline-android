package dev.claucookielabs.picstimeline

import android.app.Application
import dev.claucookielabs.picstimeline.data.datasource.local.SharedPrefsDataSource
import org.koin.android.ext.android.get

class App : Application() {

    private lateinit var sharedPreferences: SharedPrefsDataSource

    override fun onCreate() {
        super.onCreate()
        initKoin()
        sharedPreferences = get()
        if (!sharedPreferences.isTracking()) sharedPreferences.clearAll()
    }
}
