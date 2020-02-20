package dev.claucookielabs.picstimeline.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Geocoder
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BADGE_ICON_LARGE
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.util.PlatformVersion
import com.google.android.gms.location.*
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.presentation.MainActivity
import org.koin.android.ext.android.get


class LocationUpdatesService : Service() {

    private var lastLocation: Location? = null
    private val fusedLocationProvider: FusedLocationProviderClient = get()
    private var configurationChanged = false
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
        Log.i("Info", "Location updates service Binded")
        configurationChanged = false
        stopForeground(true)
        Log.i("Info", "Location updates service to background")
        return binder
    }

    override fun onRebind(intent: Intent?) {
        Log.i("Info", "Location updates service reBinded")
        configurationChanged = false
        stopForeground(true)
        Log.i("Info", "Location updates service to background")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i("Info", "Location updates service unBinded")
        if (!configurationChanged) {
            Log.i("Info", "Location updates service to foreground")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i("Info", "Location updates service changed orientation")
        configurationChanged = true
    }

    fun getPeriodicLocationUpdates() {
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
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
            lastLocation = fetchAreaForLocation(locationResult.lastLocation)
            broadcastLocation(lastLocation!!)
        }
    }

    private fun broadcastLocation(currentLocation: Location) {
        val intent = Intent(LOCATION_BROADCAST)
        intent.putExtra(LOCATION_EXTRA, currentLocation)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        Log.i("Info", "Broadcast sent:  $LOCATION_BROADCAST")
    }

    private fun handleLocationAvailability(locationAvailability: LocationAvailability?) {
        Log.i("Info", "Location available = " + locationAvailability.toString())
    }


    private fun userHasWalkedEnoughDistance(currentLocation: Location): Boolean {
        return currentLocation.distanceTo(lastLocation) > MIN_WALKED_DISTANCE_METERS
    }


    private fun fetchAreaForLocation(currentLocation: Location): Location {
        val addresses = geocoder.getFromLocation(
            currentLocation.latitude,
            currentLocation.longitude,
            MAX_GEOCODER_RESULTS
        )
        val firstAddress = addresses.first()
        val areaName = firstAddress?.thoroughfare ?: firstAddress.postalCode
        currentLocation.extras.putString(
            AREA_EXTRA,
            areaName
        )
        return currentLocation
    }

    private fun getNotification(): Notification {
        val title = "Timeline is tracking your location"
        val content = "Current location: ${lastLocation?.extras?.get(AREA_EXTRA) ?: "Unknown"}"
        val intent = Intent(this, MainActivity::class.java)
        val builder = NotificationCompat.Builder(this, createNotificationChannel())
            .setContentText(content)
            .setContentTitle(title)
            .setBadgeIconType(BADGE_ICON_LARGE)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
            .setColorized(true)
            .setOngoing(true)
            .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
            .setSmallIcon(R.drawable.ic_android_notif)
            .setTicker(content)
            .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
            .setWhen(System.currentTimeMillis());

        return builder.build();
    }

    private fun createNotificationChannel(): String {
        if (PlatformVersion.isAtLeastO()) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
            channel.lightColor = R.color.colorPrimaryLight
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            return channel.id
        }
        return NOTIFICATION_CHANNEL_ID
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
private const val NOTIFICATION_CHANNEL_ID = "location"
private const val NOTIFICATION_CHANNEL_NAME = "Location Notifications"
private const val NOTIFICATION_ID: Int = 398422093
const val LOCATION_BROADCAST = "location_broadcast"
const val AREA_EXTRA = "area"
const val LOCATION_EXTRA = "location"

