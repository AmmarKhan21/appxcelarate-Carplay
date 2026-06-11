package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemTripBinding
import com.car.play.android.app.db.TripEntity

class TripAdapter(
    private val items: MutableList<TripEntity>,
    private val deleteAction: (TripEntity) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<TripEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class TripViewHolder(private val binding: ItemTripBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TripEntity) {
            binding.tvDate.text = item.date
            binding.tvDistance.text = String.format("%.1f km", item.distance)

            val hours = item.duration / 3600000
            val minutes = (item.duration % 3600000) / 60000
            binding.tvDuration.text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

            binding.tvAvgSpeed.text = String.format("%.0f km/h", item.averageSpeed)

            val route = if (item.startAddress.isNotEmpty() && item.endAddress.isNotEmpty()) {
                "${item.startAddress} → ${item.endAddress}"
            } else {
                "Route not available"
            }
            binding.tvRoute.text = route

            binding.ivDelete.setOnClickListener { deleteAction(item) }
        }
    }
}
