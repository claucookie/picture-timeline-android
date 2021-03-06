package dev.claucookielabs.picstimeline.data.repository

import dev.claucookielabs.picstimeline.data.datasource.local.SharedPrefsDataSource
import dev.claucookielabs.picstimeline.data.datasource.remote.ApiImage
import dev.claucookielabs.picstimeline.domain.PicturesRepository
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import dev.claucookielabs.picstimeline.presentation.Image
import retrofit2.HttpException
import java.io.IOException

class FlickrRepository(
    private val remoteDataSource: PicturesDataSource,
    private val sharedPrefsDataSource: SharedPrefsDataSource
) : PicturesRepository {
    override suspend fun getPictureByLocation(
        lat: Double,
        long: Double,
        distance: Float
    ): ResultWrapper<Image> {
        return try {
            val pictureByLocation: ApiImage? =
                remoteDataSource.getPictureByLocation(lat, long, distance)
            if (pictureByLocation == null) ResultWrapper.NoPicFoundError
            else ResultWrapper.Success(pictureByLocation.toDomain())
        } catch (throwable: IOException) {
            ResultWrapper.NetworkError
        } catch (throwable: HttpException) {
            val code = throwable.code()
            val errorResponse = throwable.message()
            ResultWrapper.GenericError(code, errorResponse)
        }
    }

    override fun getSavedImages(): List<Image> = sharedPrefsDataSource.getImages()

    override fun isTracking(): Boolean = sharedPrefsDataSource.isTracking()

    override fun getLastSavedLocation(): DeviceLocation? = sharedPrefsDataSource.getLastLocation()
}

private fun ApiImage.toDomain(): Image {
    return Image(url = url!!)
}
