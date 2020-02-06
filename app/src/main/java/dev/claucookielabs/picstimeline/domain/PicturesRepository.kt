package dev.claucookielabs.picstimeline.domain

import dev.claucookielabs.picstimeline.presentation.Image

interface PicturesRepository {
    fun getPictureByLocation(lat: Double, long: Double): ResultWrapper<Image>
}
