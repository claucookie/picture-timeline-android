package dev.claucookielabs.picstimeline.data.repository

import dev.claucookielabs.picstimeline.data.datasource.ApiImage
import dev.claucookielabs.picstimeline.domain.PicturesRepository
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import dev.claucookielabs.picstimeline.presentation.Image
import retrofit2.HttpException
import java.io.IOException

class FlickrRepository(val remoteDataSource: PicturesDataSource) : PicturesRepository {
    override fun getPictureByLocation(lat: Double, long: Double): ResultWrapper<Image> {
        return try {
            ResultWrapper.Success(remoteDataSource.getPictureByLocation(lat, long).toDomain())
        } catch (throwable: IOException) {
            ResultWrapper.NetworkError
        } catch (throwable: HttpException) {
            val code = throwable.code()
            val errorResponse = throwable.message()
            ResultWrapper.GenericError(code, errorResponse)
        }
    }
}

private fun ApiImage.toDomain(): Image {
    return Image(url = url)
}
