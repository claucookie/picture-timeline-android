package dev.claucookielabs.picstimeline.presentation.ui

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.domain.model.DeviceLocation
import dev.claucookielabs.picstimeline.presentation.Image

@BindingAdapter("loadImages")
fun RecyclerView.loadImages(images: List<Image>?) {
    images?.let {
        (adapter as ImagesAdapter).setImages(images)
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
    AnimatedVectorDrawableCompat.registerAnimationCallback(
        drawable,
        object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                postOnAnimation {
                    (drawable as Animatable).start()
                }
            }
        })

    if (isTracking == true) (drawable as Animatable).start()
    else AnimatedVectorDrawableCompat.clearAnimationCallbacks(drawable)
}

@BindingAdapter("loading")
fun View.setLoading(isLoading: Boolean?) {
    visibility = if (isLoading == true) VISIBLE else INVISIBLE
}

@BindingAdapter("displayLocation")
fun TextView.displayLocation(location: DeviceLocation?) {
    text =
        if (location?.area == null) context.getString(R.string.current_location_unavailable)
        else String.format(
            context.getString(R.string.current_location),
            location.area
        )

}
