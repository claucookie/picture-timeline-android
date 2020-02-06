package dev.claucookielabs.picstimeline.data.repository

import dev.claucookielabs.picstimeline.domain.PicturesRepository
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import dev.claucookielabs.picstimeline.presentation.Image

class FlickrRepository : PicturesRepository {
    override fun getPictureByLocation(lat: Double, long: Double): ResultWrapper<Image> {
        return ResultWrapper.Success(
            Image(
                "https://farm6.staticflickr.com/5824/20548482625_1331124660_b.jpg"
            )
        )
    }
}
