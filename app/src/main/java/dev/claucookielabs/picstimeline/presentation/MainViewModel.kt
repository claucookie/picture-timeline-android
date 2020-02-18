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
    private val _images = MutableLiveData<MutableList<Image>>()
    val images: LiveData<MutableList<Image>>
        get() = _images

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean>
        get() = _tracking

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location>
        get() = _lastLocation

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
            Log.i(
                "Info",
                "Location Updated" + locationResult.lastLocation.latitude.toString() + " " + locationResult.lastLocation.longitude.toString()
            )
            if (_lastLocation.value == null || userHasWalkedEnoughDistance(locationResult.lastLocation)) {
                _lastLocation.value = locationResult.lastLocation
                fetchPictureForLocation(locationResult.lastLocation)
            }
        }
    }

    private fun userHasWalkedEnoughDistance(currentLocation: Location): Boolean {
        return currentLocation.distanceTo(_lastLocation.value) > MIN_WALKED_DISTANCE_METERS
    }

    private fun getPeriodicLocationUpdates() {
        val locationRequest = LocationRequest()
        locationRequest.fastestInterval = 30000 // 30 SEC
        locationRequest.interval = 60000 // 60 SEC
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.i("Info", "Requesting location updates")
                fusedLocationProvider.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper()
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
                getPictureByLocation.execute(
                    (GetPictureRequest(
                        it.latitude,
                        it.longitude,
                        SEARCH_DISTANCE_KMS
                    ))
                )
            handleResult(result)
        }
    }

    private fun handleResult(result: ResultWrapper<Image>) {
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

private const val MIN_WALKED_DISTANCE_METERS = 100F
private const val SEARCH_DISTANCE_KMS = 0.06F
