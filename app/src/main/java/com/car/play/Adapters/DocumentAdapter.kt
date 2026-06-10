package com.car.play.android.app.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.car.play.android.app.Utils.ImageModel
import com.car.play.android.app.databinding.ItemImageBinding

class DocumentAdapter(
    private val images: MutableList<ImageModel>,  // Mutable list for item deletion
    private val onItemClick: (ImageModel) -> Unit,  // Lambda for item click
    private val onDeleteClick: (Int) -> Unit  // Lambda for delete button click
) : RecyclerView.Adapter<DocumentAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Set up click listener for item click
            binding.root.setOnClickListener {
                onItemClick(images[adapterPosition])  // Handle item click
            }

            // Set up click listener for delete button
            binding.ivDelete.setOnClickListener {
                onDeleteClick(adapterPosition)  // Handle delete button click
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        Glide.with(holder.binding.root.context)
            .load(image.imagePath)  // Load image using Glide
            .into(holder.binding.imageView)
        holder.binding.tvTitle.text = image.title  // Set the title
        holder.binding.tvDate.text = image.date
    }

    override fun getItemCount(): Int = images.size

    // Function to delete an item from the list
    fun deleteItem(position: Int) {
        images.removeAt(position)  // Remove item from the list
        notifyItemRemoved(position)  // Notify adapter that the item has been removed
        notifyItemRangeChanged(position, itemCount)  // Update the position of remaining items
    }
}
