package dev.claucookielabs.picstimeline.data.datasource.local

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import dev.claucookielabs.picstimeline.presentation.Image

class SharedPrefsDataSource(
    private val sharedPreferences: SharedPreferences,
    private val moshiBuilder: Moshi
) {
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

    fun getLastLocation(): DeviceLocation? {
        val locationSerialized = sharedPreferences.getString("location", "{}")
        return moshiBuilder.adapter<DeviceLocation>(DeviceLocation::class.java)
            .fromJson(locationSerialized)
    }

    fun saveLastLocation(location: DeviceLocation) {
        val locationSerialized =
            moshiBuilder.adapter<DeviceLocation>(DeviceLocation::class.java).toJson(location)
        sharedPreferences.edit().putString("location", locationSerialized).apply()
    }

    fun saveImages(images: List<Image>) {
        val imagesSerialized =
            moshiBuilder.adapter<List<String>>(List::class.java)
                .toJson(images.map { it.url })
        sharedPreferences.edit().putString("images", imagesSerialized).apply()
    }

    fun getImages(): List<Image> {
        val imagesSerialized = sharedPreferences.getString("images", "[]")
        return moshiBuilder.adapter<List<String>>(List::class.java)
            .fromJson(imagesSerialized)
            ?.map { Image(it) }
            ?: emptyList()
    }
}
