package dev.claucookielabs.picstimeline.services

import android.app.Service
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import org.koin.android.ext.android.get


class LocationUpdatesService : Service() {

    private var lastLocation: Location? = null
    private val fusedLocationProvider: FusedLocationProviderClient = get()
    private val geocoder: Geocoder = get()
    private val binder = LocalBinder()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
            super.onLocationAvailability(locationAvailability)
            handleLocationAvailability(locationAvailability)
        }

        override fun onLocationResult(locationResult: LocationResult?) {
            handleLocationResult(locationResult)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun getPeriodicLocationUpdates() {
        val locationRequest = LocationRequest()
        locationRequest.fastestInterval = MIN_LOC_REQUEST_INTERVAL_MILLIS
        locationRequest.interval = MIN_LOC_REQUEST_INTERVAL_MILLIS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        Log.i("Info", "Requesting location updates")
        fusedLocationProvider.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationProvider.removeLocationUpdates(locationCallback)
        Log.i("Info", "Stopping location updates")

    }

    private fun handleLocationResult(locationResult: LocationResult?) {
        locationResult?.lastLocation ?: return
        if (lastLocation == null || userHasWalkedEnoughDistance(locationResult.lastLocation)) {
            fetchAreaAndUpdateLocation(locationResult.lastLocation)
            // Broadcast location to Activity
        }
    }

    private fun handleLocationAvailability(locationAvailability: LocationAvailability?) {
        Log.i("Info", "Location available = " + locationAvailability.toString())
    }


    private fun userHasWalkedEnoughDistance(currentLocation: Location): Boolean {
        return currentLocation.distanceTo(lastLocation) > MIN_WALKED_DISTANCE_METERS
    }


    private fun fetchAreaAndUpdateLocation(currentLocation: Location) {
        val addresses = geocoder.getFromLocation(
            currentLocation.latitude,
            currentLocation.longitude,
            MAX_GEOCODER_RESULTS
        )
        val firstAddress = addresses.first()
        val areaName = firstAddress?.thoroughfare ?: firstAddress.postalCode
        currentLocation.extras.putString(
            "area",
            areaName
        )
        lastLocation = currentLocation
        Log.i("Info", "Location Updated: $areaName")
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: LocationUpdatesService
            get() {
                return this@LocationUpdatesService
            }
    }
}

private const val MIN_WALKED_DISTANCE_METERS = 100F
private const val MAX_GEOCODER_RESULTS = 1
private const val MIN_LOC_REQUEST_INTERVAL_MILLIS = 60000L // 60 sec
