package dev.claucookielabs.picstimeline.presentation

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.claucookielabs.picstimeline.domain.PicturesRepository
import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import kotlinx.android.parcel.Parcelize

class MainViewModel(
    private val flickrRepository: PicturesRepository
) : ViewModel() {
    private val _images = MutableLiveData<MutableList<Image>>()
    val images: LiveData<MutableList<Image>>
        get() = _images

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean>
        get() = _tracking

    private val _lastLocation = MutableLiveData<DeviceLocation>()
    val lastLocation: LiveData<DeviceLocation>
        get() = _lastLocation

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun toggleTracking() {
        _tracking.value = _tracking.value != true
    }

    fun fetchTimeline() {
        _tracking.value = flickrRepository.isTracking()
        _lastLocation.value = flickrRepository.getLastSavedLocation()
        _images.value = flickrRepository.getSavedImages().toMutableList()
    }
}

@Parcelize
data class Image(
    val url: String
) : Parcelable
