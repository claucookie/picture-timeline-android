package dev.claucookielabs.picstimeline.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Geocoder
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BADGE_ICON_LARGE
import androidx.core.app.NotificationCompat.FLAG_FOREGROUND_SERVICE
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.util.PlatformVersion
import com.google.android.gms.location.*
import dev.claucookielabs.picstimeline.BuildConfig
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.data.datasource.local.SharedPrefsDataSource
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.GetPictureRequest
import dev.claucookielabs.picstimeline.domain.ResultWrapper
import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import dev.claucookielabs.picstimeline.domain.model.toAndroidLocation
import dev.claucookielabs.picstimeline.domain.model.toDeviceLocation
import dev.claucookielabs.picstimeline.presentation.Image
import dev.claucookielabs.picstimeline.presentation.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get


class LocationUpdatesService : Service() {

    private val fusedLocationProvider: FusedLocationProviderClient = get()
    private val sharedPrefsDataSource: SharedPrefsDataSource = get()
    private val getPictureByLocation: GetPictureByLocation = get()
    private var configurationChanged = false
    private val geocoder: Geocoder = get()
    private val binder = LocalBinder()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
            super.onLocationAvailability(locationAvailability)
            Log.i("Info", "Location available = " + locationAvailability.toString())
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
        if (appWentToBackgroundWhileTracking()) {
            Log.i("Info", "Location updates service to foreground")
            startForeground(NOTIFICATION_ID, getNotification())
            sharedPrefsDataSource.saveActivityClosed(true)
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i("Info", "Location updates service changed orientation")
        configurationChanged = true
    }

    override fun onLowMemory() {
        sharedPrefsDataSource.clearAll()
        super.onLowMemory()
    }

    fun getPeriodicLocationUpdates() {
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
        sharedPrefsDataSource.saveTracking(true)
        val locationRequest = LocationRequest()
        locationRequest.fastestInterval = MIN_LOC_REQUEST_INTERVAL_MILLIS
        locationRequest.interval = MIN_LOC_REQUEST_INTERVAL_MILLIS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProvider.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
        Log.i("Info", "Requesting location updates")
    }

    fun stopLocationUpdates() {
        sharedPrefsDataSource.saveTracking(false)
        fusedLocationProvider.removeLocationUpdates(locationCallback)
        Log.i("Info", "Stopping location updates")
    }

    private fun appWentToBackgroundWhileTracking() =
        !configurationChanged && sharedPrefsDataSource.isTracking()

    private fun handleLocationResult(locationResult: LocationResult?) {
        locationResult?.lastLocation ?: return
        val currentLocation = locationResult.lastLocation.toDeviceLocation()
        if (userHasWalkedEnoughDistance(currentLocation)) {
            sharedPrefsDataSource.saveLastLocation(fetchAreaForLocation(currentLocation))
            if (isRunningInForeground()) {
                fetchPictureForLocation(currentLocation)
            } else {
                broadcastLocation(currentLocation)
            }
        }
    }

    private fun fetchPictureForLocation(location: DeviceLocation) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = getPictureByLocation.execute(
                (GetPictureRequest(
                    location.latitude,
                    location.longitude
                ))
            )
            handleImageResult(result)
        }
    }

    private fun handleImageResult(result: ResultWrapper<Image>) {
        if (result is ResultWrapper.Success) {
            // We dont care about the errors cause the app is not open
            val images = sharedPrefsDataSource.getImages().toMutableList()
            images.add(0, result.value)
            sharedPrefsDataSource.saveImages(images)
            Log.i("Info", "Saving picture while service is in foreground")
        }
    }

    private fun broadcastLocation(currentLocation: DeviceLocation) {
        val intent = Intent(LOCATION_BROADCAST)
        intent.putExtra(LOCATION_EXTRA, currentLocation)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        Log.i("Info", "Broadcast sent:  $LOCATION_BROADCAST")
    }

    private fun userHasWalkedEnoughDistance(currentLocation: DeviceLocation): Boolean {
        val lastLocation = sharedPrefsDataSource.getLastLocation()
        lastLocation ?: return true
        return currentLocation.toAndroidLocation().distanceTo(
            lastLocation.toAndroidLocation()
        ) > MIN_WALKED_DISTANCE_METERS
    }


    private fun fetchAreaForLocation(currentLocation: DeviceLocation): DeviceLocation {
        val addresses = geocoder.getFromLocation(
            currentLocation.latitude,
            currentLocation.longitude,
            MAX_GEOCODER_RESULTS
        )
        val firstAddress = addresses.first()
        val areaName = firstAddress?.thoroughfare ?: firstAddress.postalCode
        currentLocation.area = areaName
        return currentLocation
    }

    private fun getNotification(): Notification {
        val title = "Timeline is tracking your location"
        val content =
            "Current location: ${sharedPrefsDataSource.getLastLocation()?.area
                ?: "Unknown"}"
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
            .setWhen(System.currentTimeMillis())

        return builder.build()
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

    private fun isRunningInForeground(): Boolean {
        val manager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val isRunningForeground =
            manager.runningAppProcesses?.firstOrNull<ActivityManager.RunningAppProcessInfo> { it.processName == BuildConfig.APPLICATION_ID }
                ?.importance == FLAG_FOREGROUND_SERVICE
        Log.i("Info", "Service is in foreground = $isRunningForeground")
        return isRunningForeground
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
const val LOCATION_EXTRA = "location"

