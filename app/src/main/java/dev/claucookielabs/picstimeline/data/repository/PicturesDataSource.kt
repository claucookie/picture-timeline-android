package dev.claucookielabs.picstimeline.data.repository

import dev.claucookielabs.picstimeline.data.datasource.ApiImage

interface PicturesDataSource {
    fun getPictureByLocation(lat: Double, long: Double): ApiImage

}
