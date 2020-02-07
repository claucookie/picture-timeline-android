package dev.claucookielabs.picstimeline

import dev.claucookielabs.picstimeline.data.datasource.remote.FlickrApi
import dev.claucookielabs.picstimeline.data.datasource.remote.FlickrApiFactory
import dev.claucookielabs.picstimeline.data.datasource.remote.RemoteFlickerDataSource
import dev.claucookielabs.picstimeline.data.repository.FlickrRepository
import dev.claucookielabs.picstimeline.data.repository.PicturesDataSource
import dev.claucookielabs.picstimeline.domain.GetPictureByLocation
import dev.claucookielabs.picstimeline.domain.PicturesRepository
import dev.claucookielabs.picstimeline.presentation.MainActivity
import dev.claucookielabs.picstimeline.presentation.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun App.initKoin() {
    startKoin {
        androidLogger(Level.DEBUG)
        androidContext(this@initKoin)
        modules(listOf(scopedModules, dataModules))
    }
}

private val dataModules = module {
    factory<PicturesRepository> { FlickrRepository(get()) }
    factory<PicturesDataSource> { RemoteFlickerDataSource(get()) }
    factory<FlickrApi> { FlickrApiFactory.create() }
}

private val scopedModules = module {
    scope(named<MainActivity>()) {
        viewModel { MainViewModel(get()) }
        scoped { GetPictureByLocation(get()) }
    }
}
