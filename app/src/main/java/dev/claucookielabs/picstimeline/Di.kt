package dev.claucookielabs.picstimeline

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
        modules(listOf(scopedModules))
    }
}

private val scopedModules = module {
    scope(named<MainActivity>()) {
        viewModel { MainViewModel() }
    }
}
