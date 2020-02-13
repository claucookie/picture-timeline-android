package dev.claucookielabs.picstimeline.presentation.ui

import android.location.Location
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.presentation.Image

@BindingAdapter("addImage")
fun RecyclerView.addImage(image: Image?) {
    image?.let {
        (adapter as ImagesAdapter).addImage(it)
    }
}

@BindingAdapter("loadImage")
fun ImageView.loadImage(imageUrl: String) {
    Glide.with(context)
        .load(imageUrl)
        .into(this)
}

@BindingAdapter("tracking")
fun View.setTrackingFeedback(isTracking: Boolean?) {
    visibility = if (isTracking == true) View.VISIBLE else View.GONE
}

@BindingAdapter("displayLocation")
fun TextView.displayLocation(location: Location?) {
    text =
        if (location == null) context.getString(R.string.current_location_loading)
        else String.format(
            context.getString(R.string.current_location),
            location.latitude.toString() + ", " + location.longitude.toString()
        )
}
