package com.car.play.android.app.Adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.car.play.android.app.R
import com.car.play.android.app.databinding.ItemGalleryImagBinding

class ImageAdapter(
    private val imagePaths: List<String>,
    private val selectedImages: Set<String>,  // Track selected images
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemGalleryImagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        val imageUri = Uri.parse(imagePath)

        // Load the image with Glide
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .into(holder.binding.imageView)

        // Handle CheckBox visibility and state
        if (selectedImages.contains(imagePath)) {
            holder.binding.cbSelect.visibility = View.VISIBLE
            holder.binding.cbSelect.isChecked = true
        } else {
            holder.binding.cbSelect.visibility = View.GONE
            holder.binding.cbSelect.isChecked = false
        }

        // Handle item click
        holder.itemView.setOnClickListener {
            onClick(imagePath)
        }
    }

    override fun getItemCount(): Int = imagePaths.size

    class ImageViewHolder(val binding: ItemGalleryImagBinding) : RecyclerView.ViewHolder(binding.root)
}
