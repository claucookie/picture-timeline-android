package dev.claucookielabs.picstimeline.presentation.ui

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
