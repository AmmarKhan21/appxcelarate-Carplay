package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemNearbyPlaceBinding

data class NearbyPlace(
    val name: String,
    val address: String,
    val distance: String,
    val rating: Float,
    val latitude: Double,
    val longitude: Double
)

class NearbyPlaceAdapter(
    private val items: MutableList<NearbyPlace>,
    private val onNavigateClick: (NearbyPlace) -> Unit
) : RecyclerView.Adapter<NearbyPlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemNearbyPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<NearbyPlace>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class PlaceViewHolder(private val binding: ItemNearbyPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NearbyPlace) {
            binding.tvName.text = item.name
            binding.tvAddress.text = item.address
            binding.tvDistance.text = item.distance
            binding.ratingBar.rating = item.rating
            binding.btnNavigate.setOnClickListener { onNavigateClick(item) }
        }
    }
}
