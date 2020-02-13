package dev.claucookielabs.picstimeline.presentation

import android.location.Location
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.GetPictureRequest
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(
    private val getPictureByLocation: GetPictureByLocation,
    val fusedLocationProvider: FusedLocationProviderClient
) : ViewModel() {
    private val _image = MutableLiveData<Image>()
    val image: LiveData<Image>
        get() = _image

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean>
        get() = _tracking

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() = _location

    fun startTracking() {
        _tracking.value = true
        if (location.value == null) {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        val locationTask = fusedLocationProvider.lastLocation
        locationTask.addOnCompleteListener { task ->
            Log.i(this.javaClass.simpleName, "Location Retrieved")
            task.result?.let {
                _location.value = it
                fetchPictureForLocation(it)
            }
        }
        locationTask.addOnFailureListener {
            Log.e(this.javaClass.simpleName, it.message ?: "Location not found, Unknown reason.")
            _location.value = null
        }
    }

    private fun fetchPictureForLocation(it: Location): Job {
        return viewModelScope.launch {
            val result =
                getPictureByLocation.execute((GetPictureRequest(it.latitude, it.longitude)))
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
