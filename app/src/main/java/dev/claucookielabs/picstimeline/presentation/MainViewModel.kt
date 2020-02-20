package dev.claucookielabs.picstimeline.presentation

import android.location.Location
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.GetPictureRequest
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val getPictureByLocation: GetPictureByLocation
) : ViewModel() {
    private val _images = MutableLiveData<MutableList<Image>>()
    val images: LiveData<MutableList<Image>>
        get() = _images

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean>
        get() = _tracking

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location>
        get() = _lastLocation

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun toggleTracking() {
        _tracking.value = _tracking.value != true
    }

    fun fetchPictureForLocation(location: Location, searchRadiusKms: Float = SEARCH_DISTANCE_KMS) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                getPictureByLocation.execute(
                    (GetPictureRequest(
                        location.latitude,
                        location.longitude,
                        searchRadiusKms
                    ))
                )
            withContext(Dispatchers.Main) {
                handleImageResult(result, location, searchRadiusKms)
                _loading.value = false
                _lastLocation.value = location
            }
        }
    }

    fun restorePreviousState(isTracking: Boolean) {
        _tracking.value = isTracking
    }

    private fun handleImageResult(
        result: ResultWrapper<Image>,
        location: Location,
        searchRadiusKms: Float
    ) {
        when (result) {
            is ResultWrapper.Success -> {
                if (_images.value == null) {
                    _images.value = mutableListOf(result.value)
                } else {
                    val images = _images.value
                    images?.add(0, result.value)
                    _images.value = images
                }
            }
            is ResultWrapper.NoPicFoundError -> {
                if (searchRadiusKms < MAX_SEARCH_DISTANCE_KMS) {
                    fetchPictureForLocation(location, MAX_SEARCH_DISTANCE_KMS)
                }
            }
            is ResultWrapper.GenericError -> {
                // Show Error view
                _tracking.value = false
            }
            is ResultWrapper.NetworkError -> {
                // Show Network Error view
                _tracking.value = false
            }
        }
    }
}

@Parcelize
data class Image(
    val url: String
) : Parcelable

private const val SEARCH_DISTANCE_KMS = 0.06F
private const val MAX_SEARCH_DISTANCE_KMS = 0.2F
