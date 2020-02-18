package dev.claucookielabs.picstimeline.presentation

import android.Manifest.permission.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
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

    /**
     * @startuml
     * start
     * :onResume();
     * :check Permissions;
     * if (Granted?) then (yes)
     * :Connect GoogleApiClient;
     * else (no)
     * :Show Snackbar message;
     * endif
     * stop
     * @enduml
     **/
    override fun onResume() {
        super.onResume()
        checkLocationPermissions()
    }

    override fun onDestroy() {
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
            Log.i("Info", "Google Play Services disconnected")
        }
        super.onDestroy()
    }

    override fun onConnected(p0: Bundle?) {
        Log.i("Info", "Google Play Services are connected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i("Info", "Google Play Services connection suspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("Info", "Google Play Services connection failed")
    }

    private fun setupGooglePlayClient() {
        googleApiClient =
            GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
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
        if (!googleApiClient.isConnected) googleApiClient.connect()
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
                    coordinator_view, "Location permission is required for this app to work. "
                            + "You can give permission in the Settings of the app."
                ).withDuration(LENGTH_INDEFINITE)
                .withOpenSettingsButton("Settings")
                .build()

        Dexter.withActivity(this)
            .withPermissions(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)
            .withListener(
                CompositeMultiplePermissionsListener(
                    onPermissionsCheckedListener,
                    snackbarMultiplePermissionsListener
                )
            ).check()
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
