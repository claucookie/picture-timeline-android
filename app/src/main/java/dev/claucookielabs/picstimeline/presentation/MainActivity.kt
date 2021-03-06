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
import dev.claucookielabs.picstimeline.databinding.ActivityMainBinding
import dev.claucookielabs.picstimeline.presentation.ui.ImagesAdapter
import dev.claucookielabs.picstimeline.services.LOCATION_BROADCAST
import dev.claucookielabs.picstimeline.services.LocationUpdatesService
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.get
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by currentScope.viewModel(this)
    private val locationPermissionsChecker: LocationPermissionsChecker = get()
    private var locationUpdatesService: LocationUpdatesService? = null
    private var isLocationUpdatesServiceBound = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStart() {
        super.onStart()
        bindLocationUpdatesService()
        registerLocationUpdatesBroadcast()
        checkLocationPermissions()
    }

    override fun onStop() {
        unregisterLocationUpdatesBroadcast()
        unbindLocationUpdatesService()
        super.onStop()
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

    private fun checkLocationPermissions() {
        locationPermissionsChecker.checkLocationPermissions(
            this,
            coordinator_view
        ) {
            binding.trackingFab.isEnabled = true
            mainViewModel.fetchTimeline()
        }
    }

    private fun observeTrackingChanges() {
        mainViewModel.tracking.observe(this, Observer { isTracking ->
            if (isTracking) locationUpdatesService?.getPeriodicLocationUpdates()
            else locationUpdatesService?.stopLocationUpdates()
        })
    }

    private fun registerLocationUpdatesBroadcast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LOCATION_BROADCAST)
        )
        Log.i("Info", "Register to Broadcast:  $LOCATION_BROADCAST")
    }

    private fun unregisterLocationUpdatesBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver)
        Log.i("Info", "Unregistering Broadcast:  $LOCATION_BROADCAST")
    }

    private val locationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("Info", "Broadcast received:  $LOCATION_BROADCAST")
            mainViewModel.fetchTimeline()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: LocationUpdatesService.LocalBinder =
                service as LocationUpdatesService.LocalBinder
            locationUpdatesService = binder.service
            isLocationUpdatesServiceBound = true
            observeTrackingChanges()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationUpdatesService = null
            isLocationUpdatesServiceBound = false
        }

    }
}
