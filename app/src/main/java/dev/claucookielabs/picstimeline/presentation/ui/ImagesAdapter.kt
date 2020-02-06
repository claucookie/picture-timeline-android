package dev.claucookielabs.picstimeline.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dev.claucookielabs.picstimeline.R
import dev.claucookielabs.picstimeline.databinding.ItemViewPictureBinding
import dev.claucookielabs.picstimeline.presentation.Image

class ImagesAdapter : RecyclerView.Adapter<ImageViewHolder>() {

    private var images: MutableList<Image> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ImageViewHolder(
            DataBindingUtil.inflate(inflater, R.layout.item_view_picture, parent, false)
        )
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.binding.image = images[position]
    }

    fun addImage(image: Image) {
        images.add(image)
        notifyItemInserted(images.size - 1)
    }
}

class ImageViewHolder(val binding: ItemViewPictureBinding) : RecyclerView.ViewHolder(binding.root)
