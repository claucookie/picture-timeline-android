package dev.claucookielabs.picstimeline.data.datasource.local

import android.content.SharedPreferences

class SharedPrefsDataSource(private val sharedPreferences: SharedPreferences) {
    fun saveTracking(isTracking: Boolean) {
        sharedPreferences.edit().putBoolean("isTracking", isTracking).apply()
    }

    fun isTracking(): Boolean = sharedPreferences.getBoolean("isTracking", false)

    fun saveActivityClosed(isTracking: Boolean) {
        sharedPreferences.edit().putBoolean("isActivityClosed", isTracking).apply()
    }

    fun wasActivityClosed(): Boolean = sharedPreferences.getBoolean("isActivityClosed", false)

    fun clearAll() {
        sharedPreferences.edit().clear().commit()
    }
}