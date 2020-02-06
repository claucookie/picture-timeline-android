package dev.claucookielabs.picstimeline.presentation

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _image = MutableLiveData<Image>()
    val image: LiveData<Image>
        get() = _image

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean>
        get() = _tracking

    fun startTracking() {
        _tracking.value = true
        viewModelScope.launch {
            _image.value = Image(
                "https://farm6.staticflickr.com/5824/20548482625_1331124660_b.jpg"
            )
        }
        _tracking.value = false
    }

    fun stopTracking() {
        _tracking.value = false
    }
}

@Parcelize
data class Image(
    val url: String
) : Parcelable
