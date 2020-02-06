package dev.claucookielabs.picstimeline.data.datasource

import com.squareup.moshi.Json
import dev.claucookielabs.picstimeline.data.repository.PicturesDataSource

class RemoteFlickerDataSource : PicturesDataSource {
    override fun getPictureByLocation(lat: Double, long: Double): ApiImage {
        return ApiImage("https://live.staticflickr.com/5552/14595415219_2b506690f2_b.jpg")
    }
}

data class ApiImage(
    @Json(name = "url_l")
    val url: String
)

data class PhotosResponse(
    val photos: PhotoResponse
)

class PhotoResponse(
    val photo: List<ApiImage>
)
