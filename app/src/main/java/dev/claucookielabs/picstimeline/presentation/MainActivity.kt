package dev.claucookielabs.picstimeline.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.databinding.ActivityMainBinding
import dev.claucookielabs.picstimeline.presentation.ui.ImagesAdapter
import dev.claucookielabs.picstimeline.services.LocationUpdatesService
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by currentScope.viewModel(this)
    private val locationPermissionsChecker = LocationPermissionsChecker()
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: LocationUpdatesService.LocalBinder =
                service as LocationUpdatesService.LocalBinder
            locationUpdatesService = binder.service
            isLocationUpdatesServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationUpdatesService = null
            isLocationUpdatesServiceBound = false
        }

    }
    private lateinit var binding: ActivityMainBinding
    private var locationUpdatesService: LocationUpdatesService? = null
    private var isLocationUpdatesServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        bindLocationUpdatesService()
        mainViewModel.tracking.observe(this, Observer { isTracking ->
            if (isTracking) mainViewModel.getPeriodicLocationUpdates()
            else mainViewModel.stopLocationUpdates()
        })
    }

    override fun onResume() {
        super.onResume()
        locationPermissionsChecker.checkLocationPermissions(
            this,
            coordinator_view
        ) { binding.trackingFab.isEnabled = true }
    }

    override fun onStop() {
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
            unbindService(serviceConnection)
            isLocationUpdatesServiceBound = false
        }
    }

    private fun bindLocationUpdatesService() {
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(Intent(this, LocationUpdatesService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }
}
