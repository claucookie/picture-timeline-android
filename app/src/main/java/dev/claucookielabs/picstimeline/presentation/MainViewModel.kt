package dev.claucookielabs.picstimeline.presentation

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.claucookielabs.picstimeline.data.datasource.local.SharedPrefsDataSource
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.GetPictureRequest
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel (
    private val getPictureByLocation: GetPictureByLocation,
    private val sharedPrefsDataSource: SharedPrefsDataSource
) : ViewModel() {
    private val _images = MutableLiveData<MutableList<Image>>()
    val images: LiveData<MutableList<Image>>
        get() = _images

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean>
        get() = _tracking

    private val _lastLocation = MutableLiveData<DeviceLocation>()
    val lastLocation: LiveData<DeviceLocation>
        get() = _lastLocation

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun toggleTracking() {
        _tracking.value = _tracking.value != true
    }

    fun fetchPictureForLocation(location: DeviceLocation) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                getPictureByLocation.execute(
                    GetPictureRequest(
                        location.latitude,
                        location.longitude
                    )
                )
            withContext(Dispatchers.Main) {
                handleImageResult(result)
                _loading.value = false
                _lastLocation.value = location
            }
        }
    }

    fun restorePreviousState(
        isTracking: Boolean,
        lastLocation: DeviceLocation?,
        images: MutableList<Image>
    ) {
        _tracking.value = isTracking
        _lastLocation.value = lastLocation
        _images.value = images
    }

    private fun handleImageResult(result: ResultWrapper<Image>) {
        when (result) {
            is ResultWrapper.Success -> {
                val images = _images.value ?: mutableListOf()
                images.add(0, result.value)
                sharedPrefsDataSource.saveImages(images)
                _images.value = images
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
