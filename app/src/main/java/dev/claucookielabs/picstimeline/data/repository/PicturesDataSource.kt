package dev.claucookielabs.picstimeline.data.repository

import dev.claucookielabs.picstimeline.data.datasource.remote.ApiImage

interface PicturesDataSource {
    suspend fun getPictureByLocation(lat: Double, long: Double): ApiImage?

}
