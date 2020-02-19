package dev.claucookielabs.picstimeline.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.google.android.gms.common.util.PlatformVersion
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dev.claucookielabs.picstimeline.BuildConfig
import dev.claucookielabs.picstimeline.R

class LocationPermissionsChecker {

    fun checkLocationPermissions(activity: Activity, coordinatorView: View, onGranted: () -> Unit) {
        val onPermissionsCheckedListener: MultiplePermissionsListener =
            object : BaseMultiplePermissionsListener() {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) onGranted()
                    else showSnackbar(coordinatorView)

                }
            }

        if (PlatformVersion.isAtLeastM()) {
            requestPermissionsForM(activity, onPermissionsCheckedListener)
        } else {
            requestPermissionsPriorM(activity, onPermissionsCheckedListener)
        }
    }

    private fun showSnackbar(coordinatorView: View) {
        Snackbar.make(
            coordinatorView,
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.settings) {
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            coordinatorView.context.startActivity(intent)
        }.show();
    }

    private fun requestPermissionsPriorM(
        activity: Activity,
        onPermissionsCheckedListener: MultiplePermissionsListener
    ) {
        Dexter.withActivity(activity)
            .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(onPermissionsCheckedListener)
            .check()
    }

    private fun requestPermissionsForM(
        activity: Activity,
        onPermissionsCheckedListener: MultiplePermissionsListener
    ) {
        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            .withListener(onPermissionsCheckedListener)
            .check()
    }
}
