package dev.claucookielabs.picstimeline.presentation

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.GetPictureRequest
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch

class MainViewModel(val getPictureByLocation: GetPictureByLocation) : ViewModel() {
    private val _image = MutableLiveData<Image>()
    val image: LiveData<Image>
        get() = _image

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean>
        get() = _tracking

    fun startTracking() {
        _tracking.value = true
        viewModelScope.launch {
            val result = getPictureByLocation.execute((GetPictureRequest(10.0, 11.0)))
            handleResult(result)
        }
    }

    private fun handleResult(result: ResultWrapper<Image>) {
        when (result) {
            is ResultWrapper.Success -> _image.value = result.value
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

    fun stopTracking() {
        _tracking.value = false
    }
}

@Parcelize
data class Image(
    val url: String
) : Parcelable
