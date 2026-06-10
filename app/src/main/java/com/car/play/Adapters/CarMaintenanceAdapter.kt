package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemCarmaintenenceBinding
import com.car.play.android.app.db.CarMaintenanceEntity

class CarMaintenanceAdapter(
    private val items: MutableList<CarMaintenanceEntity>,  // Mutable list for modification
    private val deleteAction: (CarMaintenanceEntity) -> Unit  // Lambda for delete action
) : RecyclerView.Adapter<CarMaintenanceAdapter.CarMaintenanceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarMaintenanceViewHolder {
        val binding = ItemCarmaintenenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarMaintenanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarMaintenanceViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    // Handle item deletion
    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class CarMaintenanceViewHolder(private val binding: ItemCarmaintenenceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CarMaintenanceEntity) {
            binding.text.text = item.serviceName
            binding.date.text = item.date
            binding.cost.text = "Amount: ${item.cost}"

            // Set click listener for delete button
            binding.ivDelete.setOnClickListener {
                deleteAction(item) // Trigger the delete action when delete icon is clicked
            }
        }
    }
}

