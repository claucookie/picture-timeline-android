package dev.claucookielabs.picstimeline.domain

import dev.claucookielabs.picstimeline.presentation.Image

interface PicturesRepository {
    suspend fun getPictureByLocation(lat: Double, long: Double): ResultWrapper<Image>
}
