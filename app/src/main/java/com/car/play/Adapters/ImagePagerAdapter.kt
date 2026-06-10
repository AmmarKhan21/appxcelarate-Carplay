package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.car.play.android.app.databinding.ItemImagePagerBinding

class ImagePagerAdapter(private val images: List<String>) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    // ViewHolder for each item in ViewPager2
    inner class ImageViewHolder(val binding: ItemImagePagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // Inflate the item layout (item_image_pager.xml)
        val binding = ItemImagePagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = images[position]

        // Use Glide to load the image into the ImageView
        Glide.with(holder.binding.root.context)
            .load(imagePath)
            .into(holder.binding.imageView)
    }

    override fun getItemCount(): Int = images.size
}
