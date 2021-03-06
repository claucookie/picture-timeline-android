package dev.claucookielabs.picstimeline.data.datasource.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {

    @GET("rest/?method=flickr.photos.search&extras=url_m&format=json&nojsoncallback=1&media=photo")
    suspend fun getPicturesByLocation(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("api_key") apiKey: String,
        @Query("radius") distance: Float // in KM
    ): GetPicturesResponse
}


data class GetPicturesResponse(
    @field:Json(name = "photos")
    val picturesResponse: ApiPictures
)

data class ApiPictures(
    @field:Json(name = "photo")
    val photos: List<ApiImage>?
)

data class ApiImage(
    @field:Json(name = "url_m")
    val url: String?
)
