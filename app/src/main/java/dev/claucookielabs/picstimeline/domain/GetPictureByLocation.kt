package dev.claucookielabs.picstimeline.domain

import dev.claucookielabs.picstimeline.presentation.Image

class GetPictureByLocation(val picturesRepository: PicturesRepository) :
    UseCase<GetPictureRequest, ResultWrapper<Image>> {
    override suspend fun execute(request: GetPictureRequest): ResultWrapper<Image> {
        return picturesRepository.getPictureByLocation(request.lat, request.long)
    }

}

class GetPictureRequest(
    val lat: Double,
    val long: Double
) : BaseRequest()
