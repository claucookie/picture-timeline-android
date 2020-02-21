package dev.claucookielabs.picstimeline.domain.model

import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    var area: String = ""
) : Parcelable

fun Location.toDeviceLocation(): DeviceLocation =
    DeviceLocation(this.latitude, this.longitude)

fun DeviceLocation.toAndroidLocation(): Location {
    val location = Location("")
    location.latitude = this.latitude
    location.longitude = this.longitude
    return location
}
