package dev.claucookielabs.picstimeline.presentation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
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

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by currentScope.viewModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        checkLocationPermissions()
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

    private fun startTracking() {
        mainViewModel.startTracking()

    }

    private fun checkLocationPermissions() {
        val onPermissionsCheckedListener: MultiplePermissionsListener =
            object : BaseMultiplePermissionsListener() {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) startTracking()
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

}
