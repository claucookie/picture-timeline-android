package dev.claucookielabs.picstimeline.domain

import dev.claucookielabs.picstimeline.presentation.Image

class GetPictureByLocation : UseCase<GetPictureRequest, ResultWrapper<Image>> {
    override suspend fun execute(request: GetPictureRequest): ResultWrapper<Image> {
        return ResultWrapper.Success(
            Image(
                "https://farm6.staticflickr.com/5824/20548482625_1331124660_b.jpg"
            )
        )
    }

}

class GetPictureRequest(
    lat: Double,
    long: Double
) : BaseRequest()
