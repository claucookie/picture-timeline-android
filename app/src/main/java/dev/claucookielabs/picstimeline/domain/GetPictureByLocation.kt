package dev.claucookielabs.picstimeline.domain

import dev.claucookielabs.picstimeline.presentation.Image

class GetPictureByLocation(private val picturesRepository: PicturesRepository) :
    UseCase<GetPictureRequest, ResultWrapper<Image>> {
    override suspend fun execute(request: GetPictureRequest): ResultWrapper<Image> {
        var result =
            picturesRepository.getPictureByLocation(request.lat, request.long, SEARCH_DISTANCE_KMS)
        if (result is ResultWrapper.NoPicFoundError) {
            result = picturesRepository.getPictureByLocation(
                request.lat,
                request.long,
                MAX_SEARCH_DISTANCE_KMS
            )
        }
        return result
    }
}

class GetPictureRequest(
    val lat: Double,
    val long: Double
) : BaseRequest()

private const val SEARCH_DISTANCE_KMS = 0.06F
private const val MAX_SEARCH_DISTANCE_KMS = 0.2F
