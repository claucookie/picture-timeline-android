package dev.claucookielabs.picstimeline.domain

interface UseCase<in R : BaseRequest, T> {

    suspend fun execute(request: R): T
}

open class BaseRequest
