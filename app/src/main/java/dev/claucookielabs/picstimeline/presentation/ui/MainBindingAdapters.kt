package dev.claucookielabs.picstimeline.presentation.ui

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.presentation.Image

@BindingAdapter("addImage")
fun RecyclerView.addImage(image: Image?) {
    image?.let {
        (adapter as ImagesAdapter).addImage(it)
        scrollToPosition(0)
    }
}

@BindingAdapter("loadImage")
fun ImageView.loadImage(imageUrl: String) {
    Glide.with(context)
        .load(imageUrl)
        .into(this)
}

@BindingAdapter("tracking")
fun FloatingActionButton.setTrackingFeedback(isTracking: Boolean?) {
    val animatedVectorDrawable = this.drawable as AnimatedVectorDrawable
    animatedVectorDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            animatedVectorDrawable.start()
        }
    })
    if (isTracking == true) animatedVectorDrawable.start()
    else animatedVectorDrawable.clearAnimationCallbacks()
}

@BindingAdapter("displayLocation")
fun TextView.displayLocation(location: Location?) {
    text =
        if (location == null) context.getString(R.string.current_location_unavailable)
        else String.format(
            context.getString(R.string.current_location),
            location.latitude.toString() + ", " + location.longitude.toString()
        )
}
