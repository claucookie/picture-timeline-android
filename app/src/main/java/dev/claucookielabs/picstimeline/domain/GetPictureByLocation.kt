package dev.claucookielabs.picstimeline.domain

import dev.claucookielabs.picstimeline.presentation.Image

class GetPictureByLocation(private val picturesRepository: PicturesRepository) :
    UseCase<GetPictureRequest, ResultWrapper<Image>> {
    override suspend fun execute(request: GetPictureRequest): ResultWrapper<Image> {
        return picturesRepository.getPictureByLocation(request.lat, request.long, request.distance)
    }
}

class GetPictureRequest(
    val lat: Double,
    val long: Double,
    val distance: Float // in KM
) : BaseRequest()
