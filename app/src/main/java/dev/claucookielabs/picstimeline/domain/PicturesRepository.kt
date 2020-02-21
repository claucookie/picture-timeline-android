package dev.claucookielabs.picstimeline.domain

import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import dev.claucookielabs.picstimeline.presentation.Image

interface PicturesRepository {
    suspend fun getPictureByLocation(
        lat: Double,
        long: Double,
        distance: Float
    ): ResultWrapper<Image>

    fun getSavedImages(): List<Image>
    fun isTracking(): Boolean
    fun getLastSavedLocation(): DeviceLocation?
}
