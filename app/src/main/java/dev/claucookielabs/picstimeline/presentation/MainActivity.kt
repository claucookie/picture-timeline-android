package dev.claucookielabs.picstimeline.presentation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.databinding.ActivityMainBinding
import dev.claucookielabs.picstimeline.presentation.ui.ImagesAdapter
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by currentScope.viewModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        startTracking()
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

        Dexter.withActivity(this)
            .withPermissions(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {

                }
            }).check();
    }

}
