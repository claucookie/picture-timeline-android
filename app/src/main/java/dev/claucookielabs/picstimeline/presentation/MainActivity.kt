package dev.claucookielabs.picstimeline.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.databinding.ActivityMainBinding
import dev.claucookielabs.picstimeline.presentation.ui.ImagesAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by currentScope.viewModel(this)
    private lateinit var binding: ActivityMainBinding
    private val locationPermissionsChecker = LocationPermissionsChecker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
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
        locationPermissionsChecker.checkLocationPermissions(
            this,
            coordinator_view
        ) { binding.trackingFab.isEnabled = true }
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
}
