package dev.claucookielabs.picstimeline

import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.squareup.moshi.Moshi
import dev.claucookielabs.picstimeline.data.datasource.local.SharedPrefsDataSource
import dev.claucookielabs.picstimeline.data.datasource.remote.FlickrApi
import dev.claucookielabs.picstimeline.data.datasource.remote.FlickrApiFactory
import dev.claucookielabs.picstimeline.data.datasource.remote.RemoteFlickerDataSource
import dev.claucookielabs.picstimeline.data.repository.FlickrRepository
import dev.claucookielabs.picstimeline.data.repository.PicturesDataSource
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.PicturesRepository
import dev.claucookielabs.picstimeline.presentation.LocationPermissionsChecker
import dev.claucookielabs.picstimeline.presentation.MainActivity
import dev.claucookielabs.picstimeline.presentation.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.*

fun App.initKoin() {
    startKoin {
        androidLogger(Level.DEBUG)
        androidContext(this@initKoin)
        modules(listOf(scopedModules, dataModules, locationModules))
    }
}

private val dataModules = module {
    factory<PicturesRepository> { FlickrRepository(get(), get()) }
    factory<PicturesDataSource> { RemoteFlickerDataSource(get()) }
    factory<FlickrApi> { FlickrApiFactory.create() }
    single<SharedPreferences> {
        androidContext().getSharedPreferences(
            BuildConfig.APPLICATION_ID,
            Context.MODE_PRIVATE
        )
    }
    single<Moshi> { Moshi.Builder().build() }
    single { SharedPrefsDataSource(get(), get()) }
    single { GetPictureByLocation(get()) }
}

private val locationModules = module {
    single { Geocoder(androidContext(), Locale.getDefault()) }
    single { FusedLocationProviderClient(androidContext()) }
    single { LocationPermissionsChecker() }
}

private val scopedModules = module {
    scope(named<MainActivity>()) {
        viewModel { MainViewModel(get()) }
        scoped { GetPictureByLocation(get()) }
    }
}
