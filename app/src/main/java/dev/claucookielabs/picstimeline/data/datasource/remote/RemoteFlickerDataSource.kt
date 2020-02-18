package dev.claucookielabs.picstimeline.data.datasource.remote

import dev.claucookielabs.picstimeline.BuildConfig
import dev.claucookielabs.picstimeline.data.repository.PicturesDataSource

class RemoteFlickerDataSource(private val flickrApi: FlickrApi) : PicturesDataSource {
    override suspend fun getPictureByLocation(lat: Double, long: Double, distance: Float): ApiImage? {
        return flickrApi.getPicturesByLocation(lat, long, BuildConfig.FLICKR_KEY, distance)
            .picturesResponse
            .photos
            ?.firstOrNull()
    }
}
