package dev.claucookielabs.picstimeline.presentation

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.data.datasource.local.SharedPrefsDataSource
import dev.claucookielabs.picstimeline.databinding.ActivityMainBinding
import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import dev.claucookielabs.picstimeline.presentation.ui.ImagesAdapter
import dev.claucookielabs.picstimeline.services.LOCATION_BROADCAST
import dev.claucookielabs.picstimeline.services.LOCATION_EXTRA
import dev.claucookielabs.picstimeline.services.LocationUpdatesService
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.get
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by currentScope.viewModel(this)
    private val locationPermissionsChecker: LocationPermissionsChecker = get()
    private val sharedPreferences: SharedPrefsDataSource = get()
    private var locationUpdatesService: LocationUpdatesService? = null
    private var isLocationUpdatesServiceBound = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        bindLocationUpdatesService()
        registerLocationUpdatesBroadcast()
        observeTrackingChanges()
    }

    override fun onResume() {
        super.onResume()
        locationPermissionsChecker.checkLocationPermissions(
            this,
            coordinator_view
        ) { binding.trackingFab.isEnabled = true }
    }

    override fun onDestroy() {
        unregisterLocationUpdatesBroadcast()
        unbindLocationUpdatesService()
        super.onDestroy()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            viewmodel = mainViewModel
            lifecycleOwner = this@MainActivity
            picturesRv.adapter = ImagesAdapter()
            trackingFab.isEnabled = false
        }
    }

    private fun unbindLocationUpdatesService() {
        if (isLocationUpdatesServiceBound) {
            Log.i("Info", "Unbinding location updates service")
            unbindService(serviceConnection)
            isLocationUpdatesServiceBound = false
        }
    }

    private fun bindLocationUpdatesService() {
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(
            Intent(this, LocationUpdatesService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        Log.i("Info", "Bind location updates service")
    }

    private fun observeTrackingChanges() {
        mainViewModel.tracking.observe(this, Observer { isTracking ->
            if (isTracking) locationUpdatesService?.getPeriodicLocationUpdates()
            else locationUpdatesService?.stopLocationUpdates()
        })
    }

    private fun restoreStateFromService() {
        if (sharedPreferences.wasActivityClosed()) {
            sharedPreferences.saveActivityClosed(false)
            mainViewModel.restorePreviousState(
                sharedPreferences.isTracking(),
                sharedPreferences.getLastLocation(),
                sharedPreferences.getImages().toMutableList()
            )
        }
    }

    private fun registerLocationUpdatesBroadcast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LOCATION_BROADCAST)
        )
        Log.i("Info", "Register to Broadcast:  $LOCATION_BROADCAST")
    }

    private fun unregisterLocationUpdatesBroadcast() {
        Log.i("Info", "Unregistering Broadcast:  $LOCATION_BROADCAST")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver)
    }

    private val locationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("Info", "Broadcast received:  $LOCATION_BROADCAST")
            intent?.getParcelableExtra<DeviceLocation>(LOCATION_EXTRA) ?: return

            val location: DeviceLocation = intent.getParcelableExtra(LOCATION_EXTRA)!!
            mainViewModel.fetchPictureForLocation(location)
        }

    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: LocationUpdatesService.LocalBinder =
                service as LocationUpdatesService.LocalBinder
            locationUpdatesService = binder.service
            isLocationUpdatesServiceBound = true
            restoreStateFromService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationUpdatesService = null
            isLocationUpdatesServiceBound = false
        }

    }
}
