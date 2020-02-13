package dev.claucookielabs.picstimeline.presentation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.databinding.ActivityMainBinding
import dev.claucookielabs.picstimeline.presentation.ui.ImagesAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private val mainViewModel: MainViewModel by currentScope.viewModel(this)
    private lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        setupGooglePlayClient()
    }

    override fun onStart() {
        super.onStart()
        checkLocationPermissions()
    }

    override fun onStop() {
        if (googleApiClient.isConnected) googleApiClient.disconnect()
        Log.i(this.javaClass.simpleName, "Google Play Services disconnected")
        super.onStop()
    }

    override fun onConnected(p0: Bundle?) {
        Log.i(this.javaClass.simpleName, "Google Play Services are connected")
        // Get current location
        val locationTask = FusedLocationProviderClient(this).lastLocation
        locationTask.addOnCompleteListener { task ->
            Log.i(this.javaClass.simpleName, "Location Retrieved")
            task.result?.let {
                location_text.text = String.format(
                    getString(R.string.current_location),
                    it.latitude.toString() + ", " + it.longitude.toString()
                )
            }
        }
        locationTask.addOnFailureListener {
            Log.e(this.javaClass.simpleName, it.message ?: "Location not found, Unknown reason.")
            location_text.text = getString(R.string.current_location_unavailable)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(this.javaClass.simpleName, "Google Play Services connection suspended")
        // Stop tracking tasks
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(this.javaClass.simpleName, "Google Play Services connection failed")
        // Show Error Message
    }

    private fun setupGooglePlayClient() {
        googleApiClient =
            GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private fun setupDataBinding() {
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            viewmodel = mainViewModel
            lifecycleOwner = this@MainActivity
            picturesRv.adapter = ImagesAdapter()
        }
    }

    private fun connectToGooglePlayServices() {
        if (!isPlayServicesAvailable()) {
            Snackbar.make(
                coordinator_view,
                R.string.common_google_play_services_unsupported_text,
                LENGTH_SHORT
            ).show()
            return
        }
        googleApiClient.connect()
    }

    private fun checkLocationPermissions() {
        val onPermissionsCheckedListener: MultiplePermissionsListener =
            object : BaseMultiplePermissionsListener() {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) connectToGooglePlayServices()
                }
            }
        val snackbarMultiplePermissionsListener: MultiplePermissionsListener =
            SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                .with(
                    coordinator_view,
                    "Location permission is required for this app to work. "
                            + "You can give permission in the Settings of the app."
                )
                .withOpenSettingsButton("Settings")
                .build()

        Dexter.withActivity(this)
            .withPermissions(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            ).withListener(
                CompositeMultiplePermissionsListener(
                    onPermissionsCheckedListener,
                    snackbarMultiplePermissionsListener
                )
            )
            .check()
    }

    private fun isPlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance();
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            }
            return false;
        }
        return true;
    }
}

const val PLAY_SERVICES_RESOLUTION_REQUEST = 12345
