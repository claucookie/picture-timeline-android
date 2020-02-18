package dev.claucookielabs.picstimeline.presentation

import android.location.Location
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.GetPictureRequest
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val getPictureByLocation: GetPictureByLocation,
    private val fusedLocationProvider: FusedLocationProviderClient
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

    fun toggleTracking() {
        if (_tracking.value == true) stopTracking()
        else startTracking()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
            super.onLocationAvailability(locationAvailability)
            Log.i(
                "Info",
                "Location availability Updated: " + locationAvailability.toString()
            )
        }

        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation ?: return
            Log.i("Info", "Location Updated")
            _location.value = locationResult.lastLocation
            fetchPictureForLocation(locationResult.lastLocation)
        }
    }

    private fun getPeriodicLocationUpdates() {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.i("Info", "Requesting location updates")
                fusedLocationProvider.requestLocationUpdates(
                    LocationRequest(), locationCallback, Looper.getMainLooper()
                )
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProvider.removeLocationUpdates(locationCallback)
        Log.i("Info", "Stopping location updates")
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

    private fun startTracking() {
        _tracking.value = true
        getPeriodicLocationUpdates()
    }

    private fun stopTracking() {
        stopLocationUpdates()
        _tracking.value = false
    }
}

@Parcelize
data class Image(
    val url: String
) : Parcelable
