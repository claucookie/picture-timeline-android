package dev.claucookielabs.picstimeline.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _data = MutableLiveData<UIModel>()
    val data: LiveData<UIModel>
        get() = _data

    fun startTracking() {
        if (_data.value != null) return

        _data.value = UIModel.Loading
    }
}

sealed class UIModel {
    object Loading : UIModel()
    object Error : UIModel()
    class Content<T>(val data: T) : UIModel()
}